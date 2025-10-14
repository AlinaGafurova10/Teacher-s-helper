package com.hfad.teachershelper.retrofit

// app/src/main/java/.../GsonUtils.kt
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

object GsonUtils {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            TypeToken.getParameterized(List::class.java, Quiz::class.java).type,
            QuizListDeserializer()
        )
        .create()
}

class NullToEmptyListFactory : com.google.gson.TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: com.google.gson.reflect.TypeToken<T>): com.google.gson.TypeAdapter<T>? {
        val rawType = type.rawType
        return if (rawType == List::class.java || (rawType.isArray && rawType.componentType == List::class.java)) {
            object : TypeAdapter<T>() {
                override fun write(out: JsonWriter, value: T) {
                    // Сериализация — не используется в вашем случае
                    gson.getDelegateAdapter(this@NullToEmptyListFactory, type).write(out, value)
                }

                override fun read(`in`: JsonReader): T {
                    return if (`in`.peek() == JsonToken.NULL) {
                        `in`.nextNull()
                        // Возвращаем пустой список
                        emptyList<Any>() as T
                    } else {
                        gson.getDelegateAdapter(this@NullToEmptyListFactory, type).read(`in`)
                    }
                }
            }
        } else null
    }
}