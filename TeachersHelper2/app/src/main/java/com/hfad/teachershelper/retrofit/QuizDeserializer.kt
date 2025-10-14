package com.hfad.teachershelper.retrofit

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class QuizListDeserializer : JsonDeserializer<List<Quiz>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Quiz> {
        return when {
            json == null || json.isJsonNull -> emptyList()
            json.isJsonObject -> listOf(context?.deserialize(json, Quiz::class.java) ?: return emptyList())
            json.isJsonArray -> context?.deserialize(json, TypeToken.getParameterized(List::class.java, Quiz::class.java).type) ?: emptyList()
            else -> emptyList()
        }
    }
}