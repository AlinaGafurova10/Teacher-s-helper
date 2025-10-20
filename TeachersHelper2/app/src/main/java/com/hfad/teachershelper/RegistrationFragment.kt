package com.hfad.teachershelper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.hfad.teachershelper.retrofit.Login_parol
import com.hfad.teachershelper.retrofit.MainAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


class RegistrationFragment : Fragment() {
    private lateinit var editText1: EditText
    private lateinit var editText2: EditText
    private lateinit var buttonSend: Button
    private lateinit var mainAPI: MainAPI

    // Retrofit клиент
    private val retrofit = Retrofit.Builder()
        .baseUrl(MainAPI.BASE_URL)  // Замените на ваш базовый URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        mainAPI = retrofit.create(MainAPI::class.java)

        editText1 = view.findViewById(R.id.login_to_regesration)
        editText2 = view.findViewById(R.id.parol_parol_registration)
        buttonSend = view.findViewById(R.id.okei_to_registration)

        buttonSend.setOnClickListener {
            val string1 = editText1.text.toString()
            val string2 = editText2.text.toString()

            // Запуск корутины (асинхронный запрос)
//            CoroutineScope(Dispatchers.Main).launch {
//                try {
//                    val response = withContext(Dispatchers.IO) {
//                        mainAPI.signup(Login_parol(string1, string2))
//                    }
//                    Toast.makeText(requireContext(), "Успешно отправлено!", Toast.LENGTH_SHORT).show()
//                } catch (e: Exception) {
//                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
        }

        return view
    }
}

//    private lateinit var mainAPI: MainAPI
//
//    private lateinit var login_to_regesration: EditText
//    private lateinit var parol_parol_registration: EditText
//    private lateinit var okei_to_registration: Button
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_registration, container, false)
//        // Inflate the layout for this fragment
//        val login = view.findViewById<EditText>(R.id.login_to_regesration)
//        val parol = view.findViewById<EditText>(R.id.parol_parol_registration)
//        val buttonSend = view.findViewById<Button>(R.id.okei_to_registration)
//
//        buttonSend.setOnClickListener {
//            val string1 = login_to_regesration.text.toString()
//            val string2 = parol_parol_registration.text.toString()
//        }
//
//        Thread {
//            sendPostRequest(string1, string2)
//        }.start()
//
//
//
//
//        return view
//    }
//    private fun sendPostRequest(string1: String, string2: String){
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")  // Базовый URL (должен заканчиваться на "/")
//            .addConverterFactory(GsonConverterFactory.create())  // Конвертер JSON
//            .build()
//
//        mainAPI = retrofit.create(MainAPI::class.java)
//
//
//        val call = mainAPI.sendLoginPaorol(Login_parol("Привет", "Мир!"))
//
//    // Запускаем в фоновом потоке (например, через Coroutines)
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = mainAPI.sendLoginPaorol(Login_parol("Привет", "Мир!")).execute()
//                if (response.isSuccessful) {
//                    println("Успешно: ${response.body()?.message}")
//                } else {
//                    println("Ошибка: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                println("Ошибка: ${e.message}")
//            }
//        }
//
//
//
//}