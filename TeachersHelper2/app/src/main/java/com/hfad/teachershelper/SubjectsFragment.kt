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
//import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.teachershelper.Adapter.SubjectAdapter
import com.hfad.teachershelper.retrofit.MainAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
//import com.hfad.teachershelper.databinding.FragmentSubjectsBinding
import retrofit2.converter.gson.GsonConverterFactory



class SubjectsFragment : Fragment() {

    private lateinit var adapter: SubjectAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mainAPI: MainAPI

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
            Toast.makeText(context, "Предмет: ${subject.name}", Toast.LENGTH_SHORT).show()
            // navigateTo(R.id.action_subjectsFragment_to_topicsFragment)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Кнопки навигации
        backToHomeButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_homeFragment) }
        homeButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_homeFragment) }
        settingsButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_settingsFragment) }
        searchButton.setOnClickListener { navigateTo(R.id.action_subjectsFragment_to_searchFragment) }

        // Настройка Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Локальный сервер (Android Emulator)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mainAPI = retrofit.create(MainAPI::class.java)

        // Загрузка предметов
        loadSubjects()

        return view
    }

    private fun navigateTo(actionId: Int) {
        view?.findNavController()?.navigate(actionId)
    }

    private fun getToken(): String? {
        return context?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("auth_token", null)
    }

//    private fun getToken(): String? {
//        return context?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//            ?.getString("auth_token", null)
//    }


    private fun loadSubjects() {
        val token = getToken()
        Log.d("SUBJECTS", "Полученный токен: $token")

        if (token == null) {
            runOnUiThread {
                showError("Токен не найден. Нужна авторизация.")
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SUBJECTS", "Отправляем запрос к серверу...")
                val response = mainAPI.getSubjects(token)

                Log.d("SUBJECTS", "Код ответа: ${response.code()}")
                Log.d("SUBJECTS", "Тело ответа: ${response.body()}")

                if (response.isSuccessful) {
                    val subjects = response.body() ?: emptyList()
                    Log.d("SUBJECTS", "Загружено предметов: ${subjects.size}")

                    for (subject in subjects) {
                        Log.d("SUBJECTS", "Предмет: id=${subject.id}, name='${subject.name}'")
                    }

                    runOnUiThread {
                        adapter.submitList(subjects)
                    }
                } else {
                    runOnUiThread {
                        showError("Ошибка сервера: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SUBJECTS_ERROR", "Исключение", e)
                runOnUiThread {
                    showError("Ошибка сети: ${e.message}")
                }
            }
        }
    }


//    private fun loadSubjects() {
//        val token = getToken() ?: run {
//            showError("Необходима авторизация")
//            return
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val call = mainAPI.getSubjects(token)
//                val response = call.execute() // <-- execute() здесь обязателен
//
//                if (response.isSuccessful) {
//                    val subjects = response.body() ?: emptyList()
//                    runOnUiThread {
//                        adapter.submitList(subjects)
//                    }
//                } else {
//                    runOnUiThread {
//                        showError("Ошибка: ${response.code()}")
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                runOnUiThread {
//                    showError("Ошибка сети: ${e.message}")
//                }
//            }
//        }
//    }


    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}


//class SubjectsFragment : Fragment() {
//    private lateinit var adapter: SubjectAdapter
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var mainAPI: MainAPI
//
//    private fun runOnUiThread(action: () -> Unit) {
//        if (!isAdded) return
//        activity?.runOnUiThread(action)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_subjects, container, false)
//
//        // Инициализация UI элементов
//        recyclerView = view.findViewById(R.id.rview)
//        val backToHomeButton = view.findViewById<ImageButton>(R.id.back_subject_to_home)
//        val homeButton = view.findViewById<ImageButton>(R.id.home_subb)
//        val settingsButton = view.findViewById<ImageButton>(R.id.flow_subb)
//        val searchButton = view.findViewById<ImageButton>(R.id.search_subb)
//
//        // Настройка RecyclerView
//        adapter = SubjectAdapter { subject ->
//            Toast.makeText(context, "Выбран: ${subject.name}", Toast.LENGTH_SHORT).show()
//
//            // Пример перехода к темам:
//            // navigateTo(R.id.action_subjectsFragment_to_topicsFragment)
//        }
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = adapter
//
//        // Обработчики нажатий
//        backToHomeButton.setOnClickListener {
//            navigateTo(R.id.action_subjectsFragment_to_homeFragment)
//        }
//
//        homeButton.setOnClickListener {
//            navigateTo(R.id.action_subjectsFragment_to_homeFragment)
//        }
//
//        settingsButton.setOnClickListener {
//            navigateTo(R.id.action_subjectsFragment_to_settingsFragment)
//        }
//
//        searchButton.setOnClickListener {
//            navigateTo(R.id.action_subjectsFragment_to_searchFragment)
//        }
//
//        // Настройка Retrofit
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        mainAPI = retrofit.create(MainAPI::class.java)
//
//        // Загрузка данных
//        loadSubjects()
//
//        return view
//    }
//
//    private fun navigateTo(actionId: Int) {
//        view?.findNavController()?.navigate(actionId)
//    }
//
//    private fun getToken(): String? {
//        return context?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//            ?.getString("auth_token", null)
//    }
//
//
//    private fun loadSubjects() {
//    }
//}



//        val token = getToken() // Получаем токен (см. ниже)
//            ?: return showError("Требуется авторизация")
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = mainAPI.getSubjects(token) // GET /subjects/?auth_token=...
//                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
//                    val subjects = response.body()!!
//                    runOnUiThread {
//                        adapter.submitList(subjects)
//                    }
//                } else {
//                    runOnUiThread {
//                        showError("Нет данных или ошибка: ${response.code()}")
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                runOnUiThread {
//                    showError("Ошибка сети: ${e.message}")
//                }
//            }
//        }



//class SubjectsFragment : Fragment() {
//    private lateinit var adapter: SubjectAdapter
//    lateinit var binding: FragmentSubjectsBinding
//
//    private fun runOnUiThread(action: () -> Unit) {
//        this ?: return
//        if (!isAdded) return
//        activity?.runOnUiThread(action)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?): View? {
//
//        binding = SubjectsFragmentBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_subjects, container, false)
//        val backsubbtohome = view.findViewById<ImageButton>(R.id.back_subject_to_home)
//        val homefromsubbButton = view.findViewById<ImageButton>(R.id.home_subb)
//        val settfromsubbButton = view.findViewById<ImageButton>(R.id.flow_subb)
//        val searchfromsubbButton = view.findViewById<ImageButton>(R.id.search_subb)
////        val mathSubjectButton = view.findViewById<Button>(R.id.math_subb)
//
//        backsubbtohome.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_subjectsFragment_to_homeFragment)
//        }
//
//        homefromsubbButton.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_subjectsFragment_to_homeFragment)
//        }
//
//        settfromsubbButton.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_subjectsFragment_to_settingsFragment)
//        }
//
//        searchfromsubbButton.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_subjectsFragment_to_searchFragment)
//        }
//
////        mathSubjectButton.setOnClickListener {
////            view.findNavController()
////                .navigate(R.id.action_subjectsFragment_to_mathSubjectsFragment)
////        }
//        adapter = SubjectAdapter()
//        binding.rview.layoutManager = LinearLayoutManager(this)
//        binding.rview.adapter = adapter
//        adapter.submitList()
//
////        val tv = view.findViewById<TextView>(R.id.trix) //вместо 55 должно быть айди куда показывать
////        val b = view.findViewById<Button>(R.id.button) //вместо 66 должно быть айди или что-нибудь
//        //что вызывает показ списка предметов, возможно и типо буттон нужно поменять
//        //а так же наверное такое стоит наверное писать в верхнем онкреате
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")//тут должна быть ссылка родительская
//            //типо ссылка постоянная, а в SubjectAPI ее изменяемая часть
//            .addConverterFactory(GsonConverterFactory.create()).build()
//
//        val mainAPI = retrofit.create(MainAPI::class.java)
//
//
//        CoroutineScope(Dispatchers.IO).launch {
//                val list = mainAPI.getAllItems() // здесь можно без 77
//                //это типо вызов конкретного предмета, может помочь при выборе учителем предмета
//                runOnUiThread {
//                    binding.apply{
//                        adapter.submitList(list)
//                    }
////                    tv.text = subject.get(1).name
////                    var temp = ""
////                    for (i in 0..subject.size -1){
////                        temp += subject.get(i).name + " "
////                    }
////                    tv.text = temp
////                    //цикл
//                }
//
//        }
//
//        return view
//    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.fragment_subjects)
//
//        val tv = findViewById<TextView>(R.id.55) //вместо 55 должно быть айди куда показывать
//        val b = findViewById<Button>(R.id.66) //вместо 66 должно быть айди или что-нибудь
//        //что вызывает показ списка предметов, возможно и типо буттон нужно поменять
//        //а так же наверное такое стоит наверное писать в верхнем онкреате
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://")//тут должна быть ссылка родительская
//            //типо ссылка постоянная, а в SubjectAPI ее изменяемая часть
//            .addConverterFactory(GsonConverterFactory.create()).build()
//
//        val subjectAPI = retrofit.create(SubjectAPI::class.java)
//
//        b.setOnClickListener{
//            CoroutineScope(Dispatchers.IO).launch {
//                val subject = subjectAPI.getSubjectById(77) // здесь можно без 77
//                //это типо вызов конкретного предмета, может помочь при выборе учителем предмета
//                runOnUiThread {
//                    tv.text = Subject.title
//
//                }
//
//            }
//
//
//        }


//    }


