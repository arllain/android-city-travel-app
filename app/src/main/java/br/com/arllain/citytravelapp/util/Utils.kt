package br.com.arllain.citytravelapp.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

inline fun <reified T> fromJson(json: String): T {
    return Gson().fromJson(json, object: TypeToken<T>(){}.type)
}


fun getJsonDataFromFile(jsonFile: File): String? {
    val jsonString: String
    try {
        jsonString = jsonFile.bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}