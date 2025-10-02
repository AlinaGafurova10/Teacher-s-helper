package com.hfad.teachershelper

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.teachershelper.Adapter.SubjectAdapter
import com.hfad.teachershelper.retrofit.MainAPI
import com.hfad.teachershelper.retrofit.Quiz
import com.hfad.teachershelper.retrofit.Subject
import com.hfad.teachershelper.retrofit.Topic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
//import com.hfad.teachershelper.databinding.FragmentSubjectsBinding
import retrofit2.converter.gson.GsonConverterFactory



class SubjectsFragment : Fragment() {

    private lateinit var adapter: SubjectAdapter
    private lateinit var recyclerView: RecyclerView
    private var mainAPI: MainAPI? = null

    private fun runOnUiThread(action: () -> Unit) {
        if (!isAdded) return
        activity?.runOnUiThread(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_subjects, container, false)

        // Инициализация UI
        recyclerView = view.findViewById(R.id.rview)
        val backToHomeButton = view.findViewById<ImageButton>(R.id.back_subject_to_home)
        val homeButton = view.findViewById<ImageButton>(R.id.home_subb)
        val settingsButton = view.findViewById<ImageButton>(R.id.flow_subb)
        val searchButton = view.findViewById<ImageButton>(R.id.search_subb)

        // Настройка RecyclerView
        adapter = SubjectAdapter { subject ->
            val bundle = Bundle()
            bundle.putInt("subjectId", subject.id)  // ← ключ "subjectId"

            findNavController().navigate(
                R.id.action_subjectsFragment_to_topicsFragment,
                bundle
            )
//            Toast.makeText(context, "Предмет: ${subject.name}", Toast.LENGTH_SHORT).show()
//            // navigateTo(R.id.action_subjectsFragment_to_topicsFragment)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Кнопки навигации
        backToHomeButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_homeFragment) }
        homeButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_searchFragment) }

//        // Настройка Retrofit
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/") // Локальный сервер (Android Emulator)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build() -> код ретрофит когда сервер будет сделан раскомментить

//        mainAPI = retrofit.create(MainAPI::class.java) -> код ретрофит когда сервер будет сделан раскомментить

        // Загрузка предметов
//        loadSubjects() -> код ретрофит когда сервер будет сделан раскомментить

        loadSubjectsFromJson()

        return view
    }

    private fun navigateTo(actionId: Int) {
        view?.findNavController()?.navigate(actionId)
    }

//    private fun getToken(): String? {
//        return context?.getSharedPreferences("auth", Context.MODE_PRIVATE)
//            ?.getString("auth_token", null)
//    }


    private fun showError(message: String) {
        Toast.makeText(context, "Ошибка: $message", Toast.LENGTH_LONG).show()
    }

    private fun loadSubjectsFromJson() {
        try {
            // Читаем JSON из ресурсов
            val inputStream = requireContext().resources.openRawResource(R.raw.data)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            // Парсим JSON вручную или через Gson
            val jsonObject = JSONObject(jsonString)
            val subjectsArray = jsonObject.getJSONArray("subjects")

            val subjects = mutableListOf<Subject>()

            for (i in 0 until subjectsArray.length()) {
                val obj = subjectsArray.getJSONObject(i)
                val id = obj.getInt("id")
                val name = obj.getString("name")
                val topicsArray = obj.getJSONArray("topics")  // из JSONObject предмета
                val topics: List<Topic> = parseTopics(topicsArray)

                // Темы пока не нужны здесь, но можем их пропустить
                subjects.add(
                    Subject(
                        id,
                        name,
                        topics
                    )
                ) // Предполагаем, что у тебя есть data class Subject(id: Int, name: String)
            }

            // Обновляем адаптер в UI потоке
            runOnUiThread {
                adapter.submitList(subjects)
            }

        } catch (e: Exception) {
            Log.e("JSON_ERROR", "Ошибка при чтении JSON", e)
            runOnUiThread {
                showError("Ошибка загрузки данных: ${e.message}")
            }
        }
    }

    private fun parseTopics(topicsArray: JSONArray): List<Topic> {
        val topics = mutableListOf<Topic>()

        for (i in 0 until topicsArray.length()) {
            val topicObj = topicsArray.getJSONObject(i)

            val id = topicObj.getInt("id")
            val title = topicObj.getString("title")
            val content = topicObj.getString("content")

            val quizObj = topicObj.getJSONObject("quiz")
            val question = quizObj.getString("question")
            val correctAnswerIndex = quizObj.getInt("correctAnswerIndex")

            // Парсим массив options
            val optionsArray = quizObj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optionsArray.length()) {
                options.add(optionsArray.getString(j))
            }

            val quiz = Quiz(question, options, correctAnswerIndex)
            val topic = Topic(id, title, content, quiz)
            topics.add(topic)
        }

        return topics
    }
}
//    private fun loadSubjects() {
//        val token = getToken()
//        Log.d("SUBJECTS", "Полученный токен: $token")
//
//        if (token == null) {
//            runOnUiThread {
//                showError("Токен не найден. Нужна авторизация.")
//            }
//            return
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                Log.d("SUBJECTS", "Отправляем запрос к серверу...")
//                val response = mainAPI.getSubjects(token)
//
//                Log.d("SUBJECTS", "Код ответа: ${response.code()}")
//                Log.d("SUBJECTS", "Тело ответа: ${response.body()}")
//
//                if (response.isSuccessful) {
//                    val subjects = response.body() ?: emptyList()
//                    Log.d("SUBJECTS", "Загружено предметов: ${subjects.size}")
//
//                    for (subject in subjects) {
//                        Log.d("SUBJECTS", "Предмет: id=${subject.id}, name='${subject.name}'")
//                    }
//
//                    runOnUiThread {
//                        adapter.submitList(subjects)
//                    }
//                } else {
//                    runOnUiThread {
//                        showError("Ошибка сервера: ${response.code()}")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("SUBJECTS_ERROR", "Исключение", e)
//                runOnUiThread {
//                    showError("Ошибка сети: ${e.message}")
//                }
//            }
//        }
//    } -> код ретрофит когда сервер будет сделан раскомментить




