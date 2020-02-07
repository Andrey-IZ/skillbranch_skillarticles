package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {
    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        return when (defaultValue) {
            is Boolean -> thisRef.preferences.getBoolean(property.name, defaultValue) as T
            is String -> thisRef.preferences.getString(property.name, defaultValue) as T
            is Float -> thisRef.preferences.getFloat(property.name, defaultValue) as T
            is Int -> thisRef.preferences.getInt(property.name, defaultValue) as T
            is Long -> thisRef.preferences.getLong(property.name, defaultValue) as T
            else -> null
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        val editor = thisRef.preferences.edit()
        val wasEdited = when (value) {
            is Boolean -> editor.putBoolean(property.name, value as Boolean)
            is String -> editor.putString(property.name, value as String)
            is Float -> editor.putFloat(property.name, value as Float)
            is Int -> editor.putInt(property.name, value as Int)
            is Long -> editor.putLong(property.name, value as Long)
            else -> null
        }
        wasEdited?.let {
            editor?.apply()
        }
    }
}