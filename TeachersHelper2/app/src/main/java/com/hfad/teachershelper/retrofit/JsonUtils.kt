package com.hfad.teachershelper.retrofit
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hfad.teachershelper.R
import org.json.JSONObject
import com.hfad.teachershelper.retrofit.Subject
import com.hfad.teachershelper.retrofit.Topic
import com.hfad.teachershelper.retrofit.Quiz
import org.json.JSONArray


object JsonUtils {

    fun loadSubjectsRequestFromJson(context: Context): CreateSubjectsRequest {
        val jsonString = context.resources.openRawResource(R.raw.data).bufferedReader().use { it.readText() }
        return Gson().fromJson(jsonString, CreateSubjectsRequest::class.java)
    }

    fun loadSubjectsFromJson(context: Context): List<Subject> {
        return try {
            val json = context.resources.openRawResource(R.raw.data).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Subject>>() {}.type
            GsonUtils.gson.fromJson(json, type) // ← Используем наш Gson
        } catch (e: Exception) {
            Log.e("JsonUtils", "Ошибка парсинга JSON", e)
            throw e
        }
    }

//    fun loadSubjectsFromJson(context: Context): List<Subject> {
//        return try {
//            val jsonString = context.resources.openRawResource(R.raw.data).bufferedReader().use { it.readText() }
//            val jsonObject = JSONObject(jsonString)
//            val subjectsArray = jsonObject.getJSONArray("subjects")
//
//            val subjects = mutableListOf<Subject>()
//
//            for (i in 0 until subjectsArray.length()) {
//                val obj = subjectsArray.getJSONObject(i)
//                val id = obj.getInt("id")
//                val name = obj.getString("name")
//                val topicsArray = obj.getJSONArray("topics")
//                val topics = parseTopics(topicsArray)
//
//                subjects.add(Subject(id, name, topics)) // ← Subject, не SubjectRequest!
//            }
//
//            subjects
//        } catch (e: Exception) {
//            Log.e("JsonUtils", "Ошибка", e)
//            emptyList()
//        }
//    }

//    private fun parseTopics(topicsArray: JSONArray): List<Topic> {
//        val topics = mutableListOf<Topic>()
//
//        for (i in 0 until topicsArray.length()) {
//            val topicObj = topicsArray.getJSONObject(i)
//
//            val id = topicObj.getInt("id")
//            val title = topicObj.getString("title")
//            val content = topicObj.getString("content")
//
//            // 🔑 Проверяем, есть ли quiz
//            val quiz: Quiz = if (topicObj.isNull("quiz")) {
//                Quiz.EMPTY // ← пустая викторина
//            } else {
//                val quizObj = topicObj.getJSONObject("quiz")
//                val question = quizObj.getString("question")
//                val correctAnswerIndex = quizObj.getInt("correctAnswerIndex")
//
//                val optionsArray = quizObj.getJSONArray("options")
//                val options = mutableListOf<String>()
//                for (j in 0 until optionsArray.length()) {
//                    options.add(optionsArray.getString(j))
//                }
//
//                Quiz(question, options, correctAnswerIndex)
//            }
//
//            topics.add(Topic(id, title, content, quiz))
//        }
//
//        return topics
//    }

//    private fun parseTopics(topicsArray: JSONArray): List<Topic> {
//        val topics = mutableListOf<Topic>()
//        for (i in 0 until topicsArray.length()) {
//            val topicObj = topicsArray.getJSONObject(i)
//            val id = topicObj.getInt("id")
//            val title = topicObj.getString("title")
//            val content = topicObj.getString("content")
//
//            val quizObj = topicObj.getJSONObject("quiz")
//            val question = quizObj.getString("question")
//            val correctAnswerIndex = quizObj.getInt("correctAnswerIndex")
//
//            val options = mutableListOf<String>()
//            val optionsArray = quizObj.getJSONArray("options")
//            for (j in 0 until optionsArray.length()) {
//                options.add(optionsArray.getString(j))
//            }
//
//            val quiz = Quiz(question, options, correctAnswerIndex)
//            topics.add(Topic(id, title, content, quiz)) // ← Topic, не TopicRequest!
//        }
//        return topics
//    } = правильный
}

//object JsonUtils {
//
//    fun loadSubjectsFromJson(context: Context): List<Subject> {
//        val inputStream = context.resources.openRawResource(R.raw.data)
//        val jsonString = inputStream.bufferedReader().use { it.readText() }
//        return parseSubjects(jsonString)
//    }
//    fun loadSubjectsRequestFromJson(context: Context): CreateSubjectsRequest {
//        val jsonString = context.resources.openRawResource(R.raw.data).bufferedReader().use { it.readText() }
//        return Gson().fromJson(jsonString, CreateSubjectsRequest::class.java)
//    }
//
//    private fun parseSubjects(jsonString: String): List<Subject> {
//        val jsonObject = JSONObject(jsonString)
//        val subjectsArray = jsonObject.getJSONArray("subjects")
//        val subjects = mutableListOf<Subject>()
//
//        for (i in 0 until subjectsArray.length()) {
//            val subjectObj = subjectsArray.getJSONObject(i)
//            val id = subjectObj.getInt("id")
//            val name = subjectObj.getString("name")
//
//            val topicsArray = subjectObj.getJSONArray("topics")
//            val topics = mutableListOf<Topic>()
//
//            for (j in 0 until topicsArray.length()) {
//                val topicObj = topicsArray.getJSONObject(j)
//                val topicId = topicObj.getInt("id")
//                val title = topicObj.getString("title")
//                val content = topicObj.getString("content")
//
//                val quizObj = topicObj.getJSONObject("quiz")
//                val question = quizObj.getString("question")
//                val optionsArray = quizObj.getJSONArray("options")
//                val options = mutableListOf<String>()
//                for (k in 0 until optionsArray.length()) {
//                    options.add(optionsArray.getString(k))
//                }
//                val correctAnswerIndex = quizObj.getInt("correctAnswerIndex")
//
//                val quiz = QuizRequest(question, options, correctAnswerIndex)
//                val topic = TopicRequest(topicId, title, content, quiz)
//                topics.add(topic)
//            }
//
//            subjects.add(SubjectRequest(id, name, topics))
//        }
//
//        return subjects
//    }
//}