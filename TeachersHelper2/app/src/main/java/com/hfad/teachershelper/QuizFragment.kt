package com.hfad.teachershelper

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.reflect.TypeToken
import com.hfad.teachershelper.retrofit.CreateSubjectsRequest
import com.hfad.teachershelper.retrofit.GsonUtils
import com.hfad.teachershelper.retrofit.JsonUtils
import com.hfad.teachershelper.retrofit.MainAPI
import com.hfad.teachershelper.retrofit.Quiz
import com.hfad.teachershelper.retrofit.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class QuizFragment : Fragment() {

    private var questions: List<Quiz> = emptyList()
    private var currentQuestionIndex = 0
    private var userAnswers = mutableMapOf<Int, Int>()
    private var isAnswerChecked = false
    private var mainAPI: MainAPI? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_quiz, container, false)

        val topicId = arguments?.getInt("topicId") ?: -1
        if (topicId == -1) {
            Toast.makeText(context, "Ошибка: нет ID темы", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return view
        }

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(MainAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mainAPI = retrofit.create(MainAPI::class.java)

        // Загружаем ВСЕ предметы из JSON
        //val allSubjects = JsonUtils.loadSubjectsFromJson(requireContext())
        lifecycleScope.launch {
            val token = getAuthToken()
            val allSubjects: List<Subject> = try {
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
        // Ищем тему по ID
        val topic = allSubjects
            .flatMap { it.topics }
            .find { it.id == topicId }

        if (topic == null || topic.quiz.isEmpty()) {
            Toast.makeText(context, "Викторина не найдена", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return@launch
        }

        questions = topic.quiz

        val questionText = view.findViewById<TextView>(R.id.text_question)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.options_container)
        val buttonCheck = view.findViewById<Button>(R.id.button_check)
        val backButton = view.findViewById<ImageButton>(R.id.back_quiz)
        val toolbarTitle = view.findViewById<TextView>(R.id.text_quiz_title)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
       // Обновляем UI
            withContext(Dispatchers.Main) {
                toolbarTitle.text = "Вопрос ${currentQuestionIndex + 1} из ${questions.size}"
                displayQuestion(questions[currentQuestionIndex], questionText, optionsContainer)

                buttonCheck.setOnClickListener {
                    if (!isAnswerChecked) {
                        val radioGroup = optionsContainer.getChildAt(0) as? RadioGroup
                        if (radioGroup == null) {
                            Toast.makeText(
                                context,
                                "Ошибка: группа ответов не найдена",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        val selectedId = radioGroup.checkedRadioButtonId
                        if (selectedId == -1) {
                            Toast.makeText(context, "Выберите ответ", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val selectedOptionIndex =
                            radioGroup.indexOfChild(radioGroup.findViewById(selectedId))
                        userAnswers[currentQuestionIndex] = selectedOptionIndex
                        isAnswerChecked = true
                        buttonCheck.text = "Далее"

                        // Подсветка
                        for (i in 0 until radioGroup.childCount) {
                            val rb = radioGroup.getChildAt(i) as RadioButton
                            val correctIndex = questions[currentQuestionIndex].correctAnswerIndex
                            when {
                                i == correctIndex -> rb.setTextColor(Color.GREEN)
                                i == selectedOptionIndex && selectedOptionIndex != correctIndex -> rb.setTextColor(
                                    Color.RED
                                )
                            }
                        }
                    } else {
                        if (currentQuestionIndex < questions.lastIndex) {
                            currentQuestionIndex++
                            isAnswerChecked = false
                            buttonCheck.text = "Проверить"
                            toolbarTitle.text =
                                "Вопрос ${currentQuestionIndex + 1} из ${questions.size}"
                            displayQuestion(
                                questions[currentQuestionIndex],
                                questionText,
                                optionsContainer
                            )
                        } else {
                            // Результат
                            val correctCount = questions.count {
                                userAnswers[questions.indexOf(it)] == it.correctAnswerIndex
                            }
                            Toast.makeText(
                                context,
                                "Результат: $correctCount из ${questions.size}",
                                Toast.LENGTH_LONG
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun displayQuestion(
        question: Quiz,
        questionText: TextView,
        container: LinearLayout
    ) {
        questionText.text = question.question
        container.removeAllViews()

        val radioGroup = RadioGroup(requireContext()).apply {
            orientation = RadioGroup.VERTICAL
        }

        question.options.forEach { option ->
            RadioButton(requireContext()).apply {
                text = option
                id = View.generateViewId()
            }.also { radioGroup.addView(it) }
        }

        container.addView(radioGroup)
    }

    private fun getAuthToken(): String? {
        return requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }
}


