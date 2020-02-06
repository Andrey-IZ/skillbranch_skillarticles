package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

class PrefManager(context: Context) {
    val editor: SharedPreferences.Editor by lazy { preferences.edit()}
    val preferences : SharedPreferences by lazy {
        context.getSharedPreferences("", MODE_PRIVATE)
    }

    fun clearAll() {
        editor.clear()
    }
}