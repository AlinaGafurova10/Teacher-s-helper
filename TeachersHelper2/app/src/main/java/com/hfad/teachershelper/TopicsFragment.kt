package com.hfad.teachershelper

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope

import com.hfad.teachershelper.retrofit.Subject
import com.hfad.teachershelper.retrofit.Topic
import com.hfad.teachershelper.retrofit.Quiz

import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.hfad.teachershelper.Adapter.TopicAdapter
import com.hfad.teachershelper.retrofit.CreateSubjectsRequest
import com.hfad.teachershelper.retrofit.GsonUtils
import com.hfad.teachershelper.retrofit.JsonUtils
import com.hfad.teachershelper.retrofit.MainAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TopicsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TopicAdapter
    private lateinit var textTitle: TextView
    private var mainAPI: MainAPI? = null

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

        homeButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_topicsFragment_to_searchFragment) }
        backToSubbButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val subjectId = arguments?.getInt("subjectId") ?: -1
        if (subjectId == -1) {
            Toast.makeText(context, "Нет ID предмета", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return null
        }

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(MainAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mainAPI = retrofit.create(MainAPI::class.java)

        // Настройка адаптера (создаём заранее)
        adapter = TopicAdapter { topic ->
            val bundle = Bundle()
            bundle.putInt("topicId", topic.id)
            findNavController().navigate(
                R.id.action_topicsFragment_to_topicDetailFragment,
                bundle
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Загрузка данных
        lifecycleScope.launch {

            val token = getAuthToken()
            val subjects: List<Subject> = try {
                if (token != null) {
                    val response = mainAPI?.getSubjectsJsonRaw(token);
                    val jsonString = response?.body()?.string()
                    if (jsonString != null) {
                        // Парсим вручную через GsonUtils (с поддержкой null → [])
                        val type = object : TypeToken<CreateSubjectsRequest>() {}.type
                        val result = GsonUtils.gson.fromJson<CreateSubjectsRequest>(jsonString, type)
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

            // Ищем предмет
            val subject = subjects.find { it.id == subjectId }

            if (subject == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Предмет не найден", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                return@launch
            }

            // Обновляем UI
            withContext(Dispatchers.Main) {
                textTitle.text = "Темы по предмету: ${subject.name}"
                adapter.submitList(subject.topics)
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