package com.hfad.teachershelper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.hfad.teachershelper.retrofit.AuthRequestFullName
import com.hfad.teachershelper.retrofit.AuthRequestHashedPassword
import com.hfad.teachershelper.retrofit.AuthResponse
import com.hfad.teachershelper.retrofit.Login_parol
import com.hfad.teachershelper.retrofit.MainAPI
import com.hfad.teachershelper.retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginparolFragment : Fragment(R.layout.fragment_loginparol) {

    private lateinit var passwordEditText: EditText
    private lateinit var submitButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordEditText = view.findViewById(R.id.passwordEdit)
        submitButton = view.findViewById(R.id.okei_to_home)

        val username = arguments?.getString("USERNAME") ?: ""
        view.findViewById<TextView>(R.id.choose_account_spin).text = username

        val otmenaButtontologin = view.findViewById<Button>(R.id.otmena_reg)
        val zabilParolButton = view.findViewById<Button>(R.id.parolzabil)

        otmenaButtontologin.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_loginparolFragment2_to_loginFragment)
        }
        zabilParolButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_loginparolFragment2_to_zabilParolFragment)
        }


        val retrofit = Retrofit.Builder()
            .baseUrl(MainAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(MainAPI::class.java)

        submitButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            if (password.isEmpty()) return@setOnClickListener

            apiService.parol(password).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful && response.body() != null ) {
                        val token = response.body()!!.auth_token

                        // Сохранить токен
                        val sharedPref = activity?.getSharedPreferences("auth", Context.MODE_PRIVATE)
                        sharedPref?.edit()?.putString("auth_token", token)?.apply()

                        val bundle = Bundle().apply {
                        }

                        // Переход к третьему фрагменту
                        findNavController().navigate(
                            R.id.action_loginparolFragment2_to_homeFragment,
                            bundle
                        )
                    } else {
                        Toast.makeText(context, "Неверные данные", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}

//    private lateinit var passwordInput: EditText
//    private lateinit var okButton: Button
//    private lateinit var mainAPI: MainAPI
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_loginparol, container, false)
//
//        passwordInput = view.findViewById(R.id.passwordEdit)
//        okButton = view.findViewById(R.id.okei_to_home)
//
//        mainAPI = RetrofitClient.instance.create(MainAPI::class.java)
//
//        okButton.setOnClickListener {
//            val password = passwordInput.text.toString().trim()
//
//            val tempToken = arguments?.getString("temp_token")
//                ?: return@setOnClickListener.also {
//                    Toast.makeText(requireContext(),
//                        "Токен отсутствует", Toast.LENGTH_SHORT).show()
//                }
//
//            if (password.isEmpty()) return@setOnClickListener
//
//            mainAPI.sendPasswordAndToken(password, tempToken).enqueue(object : Callback<AuthResponse> {
//                override fun onResponse(
//                    call: Call<AuthResponse>,
//                    response: Response<AuthResponse>
//                ) {
//                    val temp = response.body()
//                    if (response.isSuccessful && response.body()?.access_token != null) {
//                        val accessToken = temp?.access_token
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Ошибка входа",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
//                    Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//
//        return view
//    }
//
//    private fun navigateToHomeFragment(token: String) {
//        val bundle = Bundle().apply {
//            putString("access_token", token)
//        }
//        findNavController().navigate(R.id.action_loginparolFragment2_to_homeFragment, bundle)
//    }
//}

//    private lateinit var passwordInput: EditText
//    private lateinit var tokenView: TextView
//    private lateinit var okButton: Button
//    private lateinit var tokenInput: EditText
//    private var userLogin: String = ""
//    private lateinit var receivedTempToken: String
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Загружаем разметку для фрагмента
//        val view = inflater.inflate(R.layout.fragment_loginparol, container, false)
//
//        passwordInput = view.findViewById(R.id.passwordEdit)
//        tokenInput = view.findViewById(R.id.token_input)
//        tokenView = view.findViewById(R.id.token_name)
//        okButton = view.findViewById(R.id.okei_to_home)
//
//        arguments?.let {
//            userLogin = it.getString("login", "")
//            receivedTempToken = it.getString("temp_token", "")
//        }
//
//        view.findViewById<TextView>(R.id.choose_account_spin).text = userLogin
//
////        val receivedTempToken = arguments?.getString("TEMP_TOKEN") ?: ""
//
//        // Устанавливаем начальное значение токена
//        tokenInput.setText(receivedTempToken)
//
//        okButton.setOnClickListener {
//            val password = passwordInput.text.toString().trim()
//            val token = tokenInput.text.toString().trim()
//
//            if (password.isEmpty() || token.isEmpty()) return@setOnClickListener
//
//            val api = RetrofitClient.instance.create(MainAPI::class.java)
//            api.completeLogin(password, token)
//                .enqueue(object : Callback<AuthResponse> {
//                    override fun onResponse(
//                        call: Call<AuthResponse>,
//                        response: Response<AuthResponse>
//                    ) {
//                        if (response.isSuccessful && response.body()?.success == true) {
//                            view.findNavController()
//                                .navigate(R.id.action_loginparolFragment2_to_homeFragment)
//                        } else {
//                            // Обработка ошибки авторизации
//                        }
//                    }
//
//                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
//                        // Обработка ошибки сети
//                    }
//                })
//        }
//
//        return view
//
//    }
//    private lateinit var mainAPI: MainAPI
//    private lateinit var passwordEditText: EditText
//    private lateinit var loginButton: Button
//    private var userLogin: String = ""
//    private lateinit var temp_token: String
//
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        userLogin = arguments?.getString("login") ?: ""
////
////
////        // Инициализация Retrofit
////        val retrofit = Retrofit.Builder()
////            .baseUrl("http://10.0.2.2:8000/")
////            .addConverterFactory(GsonConverterFactory.create())
////            .build()
////        mainAPI = retrofit.create(MainAPI::class.java)
////    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_loginparol, container, false)
//
//        passwordEditText = view.findViewById(R.id.passwordEdit)
//        loginButton = view.findViewById(R.id.okei_to_home)
//
//        arguments?.let {
//            userLogin = it.getString("login", "")
//            temp_token = it.getString("temp_token", "")
//        }
//
//        val otmenaButtontologin = view.findViewById<Button>(R.id.otmena_reg)
//        val zabilParolButton = view.findViewById<Button>(R.id.parolzabil)
//
//        otmenaButtontologin.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_loginparolFragment2_to_loginFragment)
//        }
//        zabilParolButton.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_loginparolFragment2_to_zabilParolFragment)
//        }
//
//        // Показываем логин пользователю (опционально)
//        view.findViewById<TextView>(R.id.choose_account_spin).text = userLogin
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        mainAPI = retrofit.create(MainAPI::class.java)
//
//        loginButton.setOnClickListener {
//            val password = passwordEditText.text.toString().trim()
//
//            if (password.length < 6) {
//                showError("Пароль должен содержать минимум 6 символов")
//                return@setOnClickListener
//            }
//
//            if (password.isEmpty()) {
//                showError("Введите пароль")
//                return@setOnClickListener
//            }
//
//            authenticateUser(password)
//        }
//
//        return view
//    }
//
//    private fun authenticateUser(password: String) {
//        lifecycleScope.launch {
//            try {
//
//                loginButton.isEnabled = false
////                var temp = AuthRequestHashedPassword(password, temp_token)
//                val response = mainAPI.get(password, temp_token)
//
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful) {
//                        response.body()?.accessToken?.let { token ->
//                            saveToken(token)
//                            navigateToMainScreen()
//                        } ?: showError("Ошибка авторизации")
//                    } else {
//                        showError("Неверный логин или пароль")
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    showError("Ошибка соединения: ${e.message}")
//                }
//            }
//        }
//    }
//
//    private fun navigateToMainScreen() {
//        findNavController().navigate(R.id.action_loginparolFragment2_to_homeFragment)
//    }
//
//    private fun saveToken(token: String) {
//        // Сохранение токена в SharedPreferences или другом хранилище
//        val sharedPref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
//        with(sharedPref.edit()) {
//            putString("access_token", token)
//            apply()
//        }
//    }
//
//    private fun showError(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }


//class LoginparolFragment : Fragment() {
//    private lateinit var mainAPI: MainAPI
//    private lateinit var okButtonHome: Button
//    private lateinit var otmenaButton: Button
//    private lateinit var zabilParolButton: Button
//    private lateinit var passwordEdit: EditText
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_loginparol, container, false)
//
//        // Инициализация UI элементов
//        okButtonHome = view.findViewById(R.id.okei_to_home)
//        otmenaButton = view.findViewById(R.id.otmena_reg)
//        zabilParolButton = view.findViewById(R.id.parolzabil)
//        passwordEdit = view.findViewById(R.id.passwordEdit)
//
//        // Настройка Retrofit
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        mainAPI = retrofit.create(MainAPI::class.java)
//
//        // Блокировка кнопки по умолчанию
//        okButtonHome.isEnabled = false
//
//        // Проверка ввода пароля
//        passwordEdit.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                validateInput()
//            }
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//
//        // Обработчики нажатий
//        okButtonHome.setOnClickListener {
//            authenticateAndNavigate()
//        }
//
//        otmenaButton.setOnClickListener {
//            navigateTo(R.id.action_loginparolFragment2_to_loginFragment)
//        }
//
//        zabilParolButton.setOnClickListener {
//            navigateTo(R.id.action_loginparolFragment2_to_zabilParolFragment)
//        }
//
//        return view
//    }
//
//    private fun validateInput() {
//        okButtonHome.isEnabled = passwordEdit.text.toString().trim().isNotEmpty()
//    }
//
//    private fun authenticateAndNavigate() {
//        val password = passwordEdit.text.toString()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = mainAPI.autH(
//                    AuthRequestHashedPassword(
//                        password = password
//                    )
//                )
//
//                activity?.runOnUiThread {
//                    if (response != null) { // Успешная аутентификация
//                        navigateTo(R.id.action_loginparolFragment2_to_homeFragment)
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun navigateTo(actionId: Int) {
//        view?.findNavController()?.navigate(actionId)
//    }
//}


//class PasswordFragment : Fragment() {
//    private lateinit var mainAPI: MainAPI
//    private lateinit var passwordEditText: EditText
//    private lateinit var loginButton: Button
//    private var userLogin: String = ""
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            userLogin = PasswordFragmentArgs.fromBundle(it).login
//        }
//
//        // Инициализация Retrofit
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        mainAPI = retrofit.create(MainAPI::class.java)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_password, container, false)
//
//        passwordEditText = view.findViewById(R.id.password_input)
//        loginButton = view.findViewById(R.id.login_button)
//
//        // Показываем логин пользователю (опционально)
//        view.findViewById<TextView>(R.id.user_login_text).text = userLogin // тут запихнуть в items roller
//
//        loginButton.setOnClickListener {
//            val password = passwordEditText.text.toString().trim()
//
//            if (password.length < 6) {
//                showError("Пароль должен содержать минимум 6 символов")
//                return@setOnClickListener
//            }
//
//            authenticateUser(userLogin, password)
//        }
//
//        return view
//    }
//
//    private fun authenticateUser(login: String, password: String) {
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val response = mainAPI.getToken(
//                    username = login,
//                    password = password
//                )
//
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful) {
//                        response.body()?.accessToken?.let { token ->
//                            saveToken(token)
//                            navigateToMainScreen()
//                        } ?: showError("Ошибка авторизации")
//                    } else {
//                        showError("Неверный логин или пароль")
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    showError("Ошибка соединения: ${e.message}")
//                }
//            }
//        }
//    }
//
//    private fun navigateToMainScreen() {
//        findNavController().navigate(R.id.action_loginparolFragment2_to_homeFragment)
//    }
//
//    private fun saveToken(token: String) {
//        // Сохранение токена в SharedPreferences или другом хранилище
//    }
//
//    private fun showError(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }
//}


//class LoginparolFragment : Fragment() {
//
//    lateinit var binding: LoginFragmentBinding
//    private lateinit var adapter: SubjectAdapter
//
//    private fun runOnUiThread(action: () -> Unit) {
//        this ?: return
//        if (!isAdded) return
//        activity?.runOnUiThread(action)
//
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//        binding = LoginparolFragmentBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val view = inflater.inflate(R.layout.fragment_loginparol, container, false)
//        val okButtonhome = view.findViewById<Button>(R.id.okei_to_home)
//        val otmenaButtontologin = view.findViewById<Button>(R.id.otmena_reg)
//        val zabilParolButton = view.findViewById<Button>(R.id.parolzabil)
//
//        okButtonhome.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_loginparolFragment2_to_homeFragment)
//        }
//
//        otmenaButtontologin.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_loginparolFragment2_to_loginFragment)
//        }
//
//        zabilParolButton.setOnClickListener {
//            view.findNavController()
//                .navigate(R.id.action_loginparolFragment2_to_zabilParolFragment)
//        }
//
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")//тут должна быть ссылка родительская
//            //типо ссылка постоянная, а в SubjectAPI ее изменяемая часть
//            .addConverterFactory(GsonConverterFactory.create()).build()
//
//        val mainAPI = retrofit.create(MainAPI::class.java)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val user = mainAPI.auth(
//                AuthRequest(
//                    binding.passwordEdit.text.toString()
//                )
//            )// здесь можно без 77
//            //это типо вызов конкретного предмета, может помочь при выборе учителем предмета
//            runOnUiThread {
//                binding.apply{
//                    adapter.submitList(list)
//                }
////                    tv.text = subject.get(1).name
////                    var temp = ""
////                    for (i in 0..subject.size -1){
////                        temp += subject.get(i).name + " "
////                    }
////                    tv.text = temp
////                    //цикл
//            }
//
//        }
//
//        return view
//    }
//}