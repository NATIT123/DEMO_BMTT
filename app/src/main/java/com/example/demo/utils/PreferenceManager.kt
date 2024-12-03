package com.example.demo.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.demo.models.User
import com.example.demo.utils.Constants.Companion.KEY_PREFERENCE_NAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PreferenceManager(private val context: Context) {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun instance() {
        sharedPreferences =
            context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun putLong(key: String, value: Long) {
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun putList(key: String, value: List<User>) {
        val gson = Gson()
        val json = gson.toJson(value)
        editor.putString(key, json)
        editor.apply()
    }

    fun getList(key: String): List<User> {
        var listProduct = listOf<User>()
        val serializedObject = sharedPreferences.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<User?>?>() {}.type
            listProduct = gson.fromJson(serializedObject, type)
        }
        return listProduct
    }


    fun clear() {
        editor.clear()
        editor.apply()
    }
}