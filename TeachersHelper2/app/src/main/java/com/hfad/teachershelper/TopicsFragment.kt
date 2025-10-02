package com.hfad.teachershelper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.teachershelper.Adapter.TopicAdapter
import com.hfad.teachershelper.retrofit.JsonUtils

class TopicsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TopicAdapter
    private lateinit var textTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_topics, container, false)

        textTitle = view.findViewById(R.id.text_topic_title)
        recyclerView = view.findViewById(R.id.recycler_view_topics)

        val backToSubbButton = view.findViewById<ImageButton>(R.id.back_themes_to_subb)
        val homeButton = view.findViewById<ImageButton>(R.id.home_themes)
        val settingsButton = view.findViewById<ImageButton>(R.id.flow_themes)
        val searchButton = view.findViewById<ImageButton>(R.id.search_themes)

//        backToSubbButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_subjectsFragment) }
        homeButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_searchFragment) }
        backToSubbButton.setOnClickListener {
            findNavController().popBackStack() // Убирает текущий фрагмент из стека
        }

        // Получаем subjectId вручную
        val subjectId = arguments?.getInt("subjectId") ?: -1
        if (subjectId == -1) {
            Toast.makeText(context, "Нет ID предмета", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return null
        }

        // Загружаем данные из JSON
        val subjects = JsonUtils.loadSubjectsFromJson(requireContext())
        val subject = subjects.find { it.id == subjectId }

        if (subject == null) {
            Toast.makeText(context, "Предмет не найден", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return null
        }

        // Обновляем заголовок
        textTitle.text = "Темы по предмету: ${subject.name}"

        // Настройка RecyclerView
        adapter = TopicAdapter { topic ->
            // Переход к деталям — без Directions
            val bundle = Bundle()
            bundle.putInt("topicId", topic.id)

            findNavController().navigate(
                R.id.action_topicsFragment_to_topicDetailFragment,
                bundle
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        adapter.submitList(subject.topics)

        return view
    }
    private fun navigateTo(actionId: Int) {
        view?.findNavController()?.navigate(actionId)
    }
}