package com.hfad.teachershelper

import android.content.Context
import androidx.navigation.findNavController
import com.hfad.teachershelper.retrofit.JsonUtils
import com.hfad.teachershelper.retrofit.Subject
import com.hfad.teachershelper.retrofit.Topic
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.reflect.TypeToken
import com.hfad.teachershelper.retrofit.CreateSubjectsRequest
import com.hfad.teachershelper.retrofit.GsonUtils
import com.hfad.teachershelper.retrofit.MainAPI
import com.hfad.teachershelper.retrofit.Quiz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class TopicDetailFragment : Fragment() {

    private var mainAPI: MainAPI? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_topic_detail, container, false)

        val textContent = view.findViewById<TextView>(R.id.text_topic_content)
        val btnQuiz = view.findViewById<Button>(R.id.btn_quiz)

        val backToThemesButton = view.findViewById<ImageButton>(R.id.back_details_to_themes)
        val homeButton = view.findViewById<ImageButton>(R.id.home_details)
        val settingsButton = view.findViewById<ImageButton>(R.id.flow_details)
        val searchButton = view.findViewById<ImageButton>(R.id.search_details)

        homeButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_searchFragment) }
        backToThemesButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Получаем topicId из аргументов
        val topicId = arguments?.getInt("topicId") ?: -1
        if (topicId == -1) {
            Toast.makeText(context, "Нет ID темы", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return null
        }

        // Загружаем все предметы из локального JSON
        //val subjects: List<Subject> = JsonUtils.loadSubjectsFromJson(requireContext())

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(MainAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mainAPI = retrofit.create(MainAPI::class.java)


        lifecycleScope.launch {
            val token = getAuthToken()
            val subjects: List<Subject> = try {
                if (token != null) {
                    val response = mainAPI?.getSubjectsJsonRaw(token);
                    val jsonString = response?.body()?.string()
                    if (jsonString != null) {
                        // Парсим вручную через GsonUtils (с поддержкой null → [])
                        val type = object : TypeToken<CreateSubjectsRequest>() {}.type
                        val result =
                            GsonUtils.gson.fromJson<CreateSubjectsRequest>(jsonString, type)
                        result.subjects
                    } else {
                        JsonUtils.loadSubjectsFromJson(requireContext())
                    }
                } else {
                    JsonUtils.loadSubjectsFromJson(requireContext())
                }
            } catch (e: Exception) {
                Log.e("TopicsFragment", "Ошибка загрузки", e)
                JsonUtils.loadSubjectsFromJson(requireContext())
            }

            // Ищем тему по ID среди всех предметов
            val topic: Topic? = subjects
                .flatMap { it.topics }
                .find { it.id == topicId }

            if (topic == null) {
                Toast.makeText(context, "Тема не найдена", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
                return@launch
            }

            // Обновляем UI
            withContext(Dispatchers.Main) {

                // Отображаем контент темы
                textContent.text = topic.content

                //удалитть потом
                btnQuiz.setOnClickListener {
                    if (topic.quiz.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Викторина недоступна для этой темы",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val bundle = Bundle().apply {
                        putInt("topicId", topic.id) // ← только ID
                    }
                    findNavController().navigate(
                        R.id.action_topicDetailFragment_to_quizFragment,
                        bundle
                    )
                }

                // Кнопка "Проверь себя" — переход к викторине
//        btnQuiz.setOnClickListener {
//            val bundle = Bundle().apply {
//                putString("quiz_question", topic.quiz.question)
//                putStringArrayList("quiz_options", ArrayList(topic.quiz.options))
//                putInt("quiz_correct_index", topic.quiz.correctAnswerIndex)
//            }
//            findNavController().navigate(R.id.action_topicDetailFragment_to_quizFragment, bundle)
//        }
            }
        }

        return view
    }

    private fun navigateTo(actionId: Int) {
        view?.findNavController()?.navigate(actionId)
    }

    private fun getAuthToken(): String? {
        return requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }
}

//class TopicDetailFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_topic_detail, container, false)
//
//        val textContent = view.findViewById<TextView>(R.id.text_topic_content)
//        val btnQuiz = view.findViewById<Button>(R.id.btn_quiz)
//
//        val backToThemesButton = view.findViewById<ImageButton>(R.id.back_details_to_themes)
//        val homeButton = view.findViewById<ImageButton>(R.id.home_details)
//        val settingsButton = view.findViewById<ImageButton>(R.id.flow_details)
//        val searchButton = view.findViewById<ImageButton>(R.id.search_details)
//
//
//
//        // Получаем topicId
//        val topicId = arguments?.getInt("topicId") ?: -1
//        if (topicId == -1) {
//            Toast.makeText(context, "Нет ID темы", Toast.LENGTH_SHORT).show()
//            parentFragmentManager.popBackStack()
//            return null
//        }
//
//        // Загружаем данные
//        val subjects = JsonUtils.loadSubjectsFromJson(requireContext())
//        val topic = subjects
//            .flatMap { it.topics }
//            .find { it.id == topicId }
//
//        if (topic == null) {
//            Toast.makeText(context, "Тема не найдена", Toast.LENGTH_SHORT).show()
//            parentFragmentManager.popBackStack()
//            return null
//        }
//
//        // Показываем текст темы
//        textContent.text = topic.content
//
//
////        backToThemesButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_topicsFragment) }
//        homeButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_homeFragment) }
//        settingsButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_settingsFragment) }
//        searchButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_searchFragment) }
//        backToThemesButton.setOnClickListener {
//            findNavController().popBackStack() // ← Не navigate! Это возврат!
//        }
//
//        // Кнопка "Проверь себя"
//        btnQuiz.setOnClickListener {
//            val bundle = Bundle().apply {
//                putString("quiz_question", topic.quiz.question)
//                putStringArrayList("quiz_options", ArrayList(topic.quiz.options))
//                putInt("quiz_correct_index", topic.quiz.correctAnswerIndex)
//            }
//            findNavController().navigate(R.id.action_topicDetailFragment_to_quizFragment, bundle)
//        }
//
//        return view
//    }
//
//    private fun navigateTo(actionId: Int) {
//        view?.findNavController()?.navigate(actionId)
//    }
//
//    private fun showQuizDialog(quiz: QuizRequest) {
//        AlertDialog.Builder(requireContext())
//            .setTitle(quiz.question)  // ← Вот он — вопрос!
//            .setSingleChoiceItems(quiz.options.toTypedArray(), -1, null)
//            .setPositiveButton("Готово") { dialog, _ ->
//                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
//                if (selectedPosition == -1) {
//                    Toast.makeText(requireContext(), "Выберите ответ", Toast.LENGTH_SHORT).show()
//                } else {
//                    val isCorrect = selectedPosition == quiz.correctAnswerIndex
//                    AlertDialog.Builder(requireContext())
//                        .setMessage(if (isCorrect) "✅ Правильно!" else "❌ Неверно!")
//                        .setPositiveButton("ОК", null)
//                        .show()
//                }
//            }
//            .setNegativeButton("Отмена", null)
//            .show()
//    }
//}