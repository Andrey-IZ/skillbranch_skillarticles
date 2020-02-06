package ru.skillbranch.skillarticles.viewmodels.base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewModelDelegate<T : ViewModel>(private val clazz: Class<T>, private val arg: Any?) :
    ReadOnlyProperty<FragmentActivity, T> {
    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>): T {
        if (arg is String) {
            val vmFactory = ViewModelFactory(arg)
            return ViewModelProvider(thisRef, vmFactory).get(clazz)
        }
        throw Exception("type of arg is not String that's not acceptable")
    }

}