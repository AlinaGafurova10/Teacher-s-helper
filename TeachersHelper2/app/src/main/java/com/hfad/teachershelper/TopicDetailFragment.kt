package com.hfad.teachershelper

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hfad.teachershelper.retrofit.JsonUtils
import com.hfad.teachershelper.retrofit.Quiz

class TopicDetailFragment : Fragment() {

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



        // Получаем topicId
        val topicId = arguments?.getInt("topicId") ?: -1
        if (topicId == -1) {
            Toast.makeText(context, "Нет ID темы", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return null
        }

        // Загружаем данные
        val subjects = JsonUtils.loadSubjectsFromJson(requireContext())
        val topic = subjects
            .flatMap { it.topics }
            .find { it.id == topicId }

        if (topic == null) {
            Toast.makeText(context, "Тема не найдена", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return null
        }

        // Показываем текст темы
        textContent.text = topic.content


//        backToThemesButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_topicsFragment) }
        homeButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_topicDetailFragment_to_searchFragment) }
        backToThemesButton.setOnClickListener {
            findNavController().popBackStack() // ← Не navigate! Это возврат!
        }

        // Кнопка "Проверь себя"
        btnQuiz.setOnClickListener {
            showQuizDialog(topic.quiz)
        }

        return view
    }

    private fun navigateTo(actionId: Int) {
        view?.findNavController()?.navigate(actionId)
    }

    private fun showQuizDialog(quiz: Quiz) {
        AlertDialog.Builder(requireContext())
            .setTitle(quiz.question)  // ← Вот он — вопрос!
            .setSingleChoiceItems(quiz.options.toTypedArray(), -1, null)
            .setPositiveButton("Готово") { dialog, _ ->
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (selectedPosition == -1) {
                    Toast.makeText(requireContext(), "Выберите ответ", Toast.LENGTH_SHORT).show()
                } else {
                    val isCorrect = selectedPosition == quiz.correctAnswerIndex
                    AlertDialog.Builder(requireContext())
                        .setMessage(if (isCorrect) "✅ Правильно!" else "❌ Неверно!")
                        .setPositiveButton("ОК", null)
                        .show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}