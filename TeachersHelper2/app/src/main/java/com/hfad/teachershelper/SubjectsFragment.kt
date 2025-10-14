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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.hfad.teachershelper.Adapter.SubjectAdapter
import com.hfad.teachershelper.retrofit.CreateSubjectsRequest
import com.hfad.teachershelper.retrofit.GsonUtils
import com.hfad.teachershelper.retrofit.JsonUtils
import com.hfad.teachershelper.retrofit.JsonUtils.loadSubjectsFromJson
import com.hfad.teachershelper.retrofit.MainAPI
import com.hfad.teachershelper.retrofit.Quiz
import com.hfad.teachershelper.retrofit.Topic
import kotlinx.coroutines.launch
import org.json.JSONArray
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
            bundle.putInt("subjectId", subject.id)
            findNavController().navigate(R.id.action_subjectsFragment_to_topicsFragment, bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Кнопки навигации
        backToHomeButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_homeFragment) }
        homeButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_searchFragment) }

        // Инициализация Retrofit
        initializeRetrofit()

        // Загрузка данных
        loadSubjectsFromApi()

        return view
    }

    private fun navigateTo(actionId: Int) {
        view?.findNavController()?.navigate(actionId)
    }

    private fun getAuthToken(): String? {
        return requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    private fun initializeRetrofit() {
        if (mainAPI == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(MainAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            mainAPI = retrofit.create(MainAPI::class.java)
        }
    }

    private fun loadSubjectsFromApi() {
        lifecycleScope.launch {
            val token = getAuthToken()
            if (token == null) {
                // Если нет токена — грузим из локального JSON
                loadSubjectsFromJson()
                return@launch
            }

            try {
                val response = mainAPI?.getSubjectsJsonRaw(token);
                val jsonString = response?.body()?.string()
                if (jsonString != null) {
                    // Парсим вручную через GsonUtils (с поддержкой null → [])
                    val type = object : TypeToken<CreateSubjectsRequest>() {}.type
                    val result = GsonUtils.gson.fromJson<CreateSubjectsRequest>(jsonString, type)
                    val subjectsToShow = result.subjects

                    runOnUiThread {
                        adapter.submitList(subjectsToShow)
                    }
                } else {
                    JsonUtils.loadSubjectsFromJson(requireContext())
                }
                /*val response = mainAPI?.getSubjectsJson(token)
                if (response?.isSuccessful == true) {
                    val subjectsList = response.body()?.subjects ?: emptyList()
                    val subjectsToShow = subjectsList // Пропускаем первый элемент

                    runOnUiThread {
                        adapter.submitList(subjectsToShow)
                    }
                } else {
                    Log.e("API", "Ошибка загрузки: ${response?.code()}")
                    loadSubjectsFromJson() // fallback
                }*/
            } catch (e: Exception) {
                Log.e("API", "Исключение при загрузке", e)
                loadSubjectsFromJson() // fallback
            }
        }
    }

    private fun loadSubjectsFromJson() {
        try {
            val subjects = JsonUtils.loadSubjectsFromJson(requireContext())
            val subjectsToShow = subjects.drop(1) // Пропускаем первый и в fallback

            runOnUiThread {
                adapter.submitList(subjectsToShow)
            }
        } catch (e: Exception) {
            Log.e("JSON", "Ошибка загрузки локального JSON", e)
            showError("Ошибка загрузки данных")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, "Ошибка: $message", Toast.LENGTH_LONG).show()
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




