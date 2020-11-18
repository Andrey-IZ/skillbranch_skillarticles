package ru.skillbranch.kotlinexample

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import kotlin.text.StringBuilder


class User private constructor(
    private val firsName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any?>? = null
) {
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firsName, lastName)
            .joinToString(" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firsName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    var phone: String? = null
        set(value) {
            field = correctPhone(value)
        }

    private var _login: String? = null
    var login: String
        set(value) {
            _login = correctLogin(value) ?: throw IllegalArgumentException("Entered invalid login")
        }
        get() = _login!!

    private var _salt: String? = null
    private val salt: String by lazy {
        if (_salt == null) ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        else _salt!!
    }

    private var _passwordHash: String? = null
    private var passwordHash: String = ""
        get() {
            return _passwordHash ?: field
        }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    //    for email
    constructor(
        firsName: String,
        lastName: String?,
        email: String?,
        password: String
    ) : this(firsName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("secondary mail constructor")
        passwordHash = encrypt(password)
    }

    // for phone
    constructor(
        firsName: String,
        lastName: String?,
        rawPhone: String?
    ) : this(firsName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("secondary phone constructor")
        requestAccessCode(rawPhone)
    }

    init {
        println("First init block, primary constructor was called")

        check(!firsName.isBlank()) { "FirstName must be not blank" }
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()) { "Email or phone mustn't be blank" }

        phone = rawPhone
        login = email ?: phone ?: throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")

        userInfo = """
            firstName: $firsName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun setHashSalt(salt: String?, hash: String?) {
        this._salt = salt
        this._passwordHash = hash
    }

    fun requestAccessCode(rawPhone: String? = null) {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(rawPhone?: phone, code)
    }

    fun checkPassword(password: String): Boolean {
        return encrypt(password) == passwordHash
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The entered password doesn't match the current password")
    }

    private fun encrypt(password: String): String = salt.plus(password.md5()) // never do that !!!

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                possible.indices.random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private fun sendAccessCodeToUser(phone: String?, code: String) {
        println("..... sending access code: $code on $phone")
    }

    private fun String.md5(): String {
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(toByteArray())  // 16 byte

        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    companion object Factory {
        private val rePreparePhoneNumber = Regex("[^+\\d]")
        private val reCheckPhoneNumber = Regex("\\+\\d{11}")

        private val reCheckEmail = Regex("[A-Za-z_]+@[a-z]+\\.[a-z]+")

        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firsname, lastname) = fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank() -> User(firsname, lastname, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firsname, lastname,
                    email, password
                )
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        fun makeUserFromCsv(
            fullName: String,
            email: String? = null,
            salt: String? = null,
            passwordHash: String? = null,
            phone: String? = null,
            meta: Map<String, Any?>? = mapOf("src" to "csv")
        ): User {
            val (firsname, lastname) = fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank() -> User(firsname, lastname, phone)
                !email.isNullOrBlank() && !salt.isNullOrBlank() && !passwordHash.isNullOrEmpty() -> User(
                    firsname, lastname,
                    email, meta = meta
                ).apply {
                    setHashSalt(salt, passwordHash)
                }

                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        fun correctEmail(rawEmail: String?): String? {
            return rawEmail?.let {
                if (it.matches(reCheckEmail)) it.toLowerCase(Locale.US) else null
            }
        }

        fun correctPhone(rawPhone: String?): String? {
            return rawPhone?.replace(rePreparePhoneNumber, "")?.let { phoneNumberStr ->
                if (phoneNumberStr.matches(reCheckPhoneNumber)) phoneNumberStr
                else null
            }
        }

        fun correctLogin(login: String): String? {
            return correctEmail(login) ?: correctPhone(login)
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException(
                            "Fullname must contain only first " +
                                    "name and last name, current split result ${this@fullNameToPair}"
                        )
                    }
                }
        }
    }
}

