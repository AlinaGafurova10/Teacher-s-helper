package com.hfad.teachershelper.retrofit
import android.content.Context
import com.hfad.teachershelper.R
import org.json.JSONObject
import org.json.JSONArray

object JsonUtils {

    fun loadSubjectsFromJson(context: Context): List<Subject> {
        val inputStream = context.resources.openRawResource(R.raw.data)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        return parseSubjects(jsonString)
    }

    private fun parseSubjects(jsonString: String): List<Subject> {
        val jsonObject = JSONObject(jsonString)
        val subjectsArray = jsonObject.getJSONArray("subjects")
        val subjects = mutableListOf<Subject>()

        for (i in 0 until subjectsArray.length()) {
            val subjectObj = subjectsArray.getJSONObject(i)
            val id = subjectObj.getInt("id")
            val name = subjectObj.getString("name")

            val topicsArray = subjectObj.getJSONArray("topics")
            val topics = mutableListOf<Topic>()

            for (j in 0 until topicsArray.length()) {
                val topicObj = topicsArray.getJSONObject(j)
                val topicId = topicObj.getInt("id")
                val title = topicObj.getString("title")
                val content = topicObj.getString("content")

                val quizObj = topicObj.getJSONObject("quiz")
                val question = quizObj.getString("question")
                val optionsArray = quizObj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (k in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(k))
                }
                val correctAnswerIndex = quizObj.getInt("correctAnswerIndex")

                val quiz = Quiz(question, options, correctAnswerIndex)
                val topic = Topic(topicId, title, content, quiz)
                topics.add(topic)
            }

            subjects.add(Subject(id, name, topics))
        }

        return subjects
    }
}