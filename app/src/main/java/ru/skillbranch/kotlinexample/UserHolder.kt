package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import ru.skillbranch.kotlinexample.User.Factory.makeUserFromCsv

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email.trim(), password.trim())
            .also { user ->
                if (map.containsKey(user.login))
                    throw IllegalArgumentException("A user with this email already exists")
                map[user.login] = user
            }
    }

    fun loginUser(login: String, password: String): String? {
        return User.correctLogin(login)?.let {
            map[it]?.run {
                if (checkPassword(password)) this.userInfo
                else null
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    fun registerUserByPhone(fullName: String, rawPhone: String): User {

        return User.makeUser(fullName, phone = rawPhone).also { user ->
            if (map.containsKey(user.login))
                throw IllegalArgumentException("A user with this phone already exists")
            map[user.login] = user
        }
    }

    fun requestAccessCode(login: String) {
        User.correctLogin(login)?.let{
            map[it]?.requestAccessCode()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun importUsers(listString: List<String>): List<User> {
        val propertySize = 5
        return listString.filterNot { it.isEmpty() }.map { userStr ->
            var fullName: String? = null
            var email : String? = null
            var salt : String? = null
            var passwordHash : String? = null
            var phoneNumber : String? = null

            val props = userStr.split(";")
            if (props.size != propertySize) return@map null
            for (i in props.indices) {
                val property = if (props[i].isBlank()) null else props[i].trim()
                when (i) {
                    0 -> fullName = property
                    1 -> email = property
                    2 -> {
                        property?.let {
                            val password = property.split(":")
                            if (password.size == 2) {
                                salt = password[0]
                                passwordHash = password[1]
                            }
                        }
                    }
                    4 -> phoneNumber = property
                }
            }
            return@map makeUserFromCsv(
                fullName = fullName?: "",
                email = email,
                phone = phoneNumber,
                passwordHash = passwordHash,
                salt = salt)
        }.filterNotNull()
    }
}
