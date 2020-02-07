package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

class PrefManager(context: Context) {
    val preferences : SharedPreferences by lazy {
        context.getSharedPreferences("", MODE_PRIVATE)
    }

    fun clearAll() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}