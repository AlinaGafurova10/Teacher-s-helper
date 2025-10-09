package com.hfad.teachershelper

import android.graphics.Color
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController


class QuizFragment : Fragment() {

    private var question: String = ""
    private var options: List<String> = emptyList()
    private var correctIndex: Int = -1
    private var userAnswer: Int = -1
    private var isAnswerChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            question = it.getString("quiz_question") ?: ""
            options = it.getStringArrayList("quiz_options") ?: emptyList()
            correctIndex = it.getInt("quiz_correct_index", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_quiz, container, false)

        val questionText = view.findViewById<TextView>(R.id.text_question)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.options_container)
        val buttonCheck = view.findViewById<Button>(R.id.button_check)
        val backButton = view.findViewById<ImageButton>(R.id.back_quiz)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (question.isEmpty() || options.isEmpty() || correctIndex == -1) {
            Toast.makeText(context, "Ошибка загрузки вопроса", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return view
        }

        questionText.text = question

        // Создаём RadioButtons
        val radioGroup = RadioGroup(requireContext()).apply {
            orientation = RadioGroup.VERTICAL
        }

        options.forEachIndexed { index, option ->
            val radioButton = RadioButton(requireContext()).apply {
                text = option
                id = View.generateViewId()
                tag = index
            }
            radioGroup.addView(radioButton)
        }

        optionsContainer.addView(radioGroup)

        buttonCheck.setOnClickListener {
            if (!isAnswerChecked) {
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId == -1) {
                    Toast.makeText(context, "Выберите ответ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                userAnswer = radioGroup.findViewById<RadioButton>(selectedId).tag as Int
                isAnswerChecked = true
                buttonCheck.text = "Готово"

                // Подсветка
                for (i in 0 until radioGroup.childCount) {
                    val rb = radioGroup.getChildAt(i) as RadioButton
                    val index = rb.tag as Int

                    if (index == correctIndex) {
                        rb.setTextColor(Color.GREEN) // Правильный — зелёный
                    } else if (index == userAnswer && userAnswer != correctIndex) {
                        rb.setTextColor(Color.RED) // Неправильный выбор — красный
                    }
                }
            } else {
                findNavController().popBackStack()
            }
        }

        return view
    }
}