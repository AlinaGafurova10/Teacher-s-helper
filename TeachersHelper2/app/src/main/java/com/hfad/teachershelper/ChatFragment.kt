package com.hfad.teachershelper

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.teachershelper.retrofit.ChatAdapter
import com.hfad.teachershelper.retrofit.ChatMessage
import com.hfad.teachershelper.retrofit.MainAPI
import com.hfad.teachershelper.retrofit.MessageRequest
import com.hfad.teachershelper.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import kotlinx.coroutines.*
import retrofit2.converter.gson.GsonConverterFactory


class ChatFragment : Fragment() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var apiService: MainAPI
    private lateinit var MessageRequest: MessageRequest
    private lateinit var ChatMessage: ChatMessage

    private val client by lazy {
        Retrofit.Builder()
            .baseUrl(MainAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MainAPI::class.java)
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        setHasOptionsMenu(true) // если нужно меню
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val backButton = view.findViewById<ImageButton>(R.id.back_roflchat_to_home)
        val homeButton = view.findViewById<ImageButton>(R.id.home_chat)
        val searchButton = view.findViewById<ImageButton>(R.id.search_chat)
        val settingsButton = view.findViewById<ImageButton>(R.id.flow_chat)

        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_chatFragment2_to_homeFragment)
        }

        homeButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_chatFragment2_to_homeFragment)
        }

        searchButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_chatFragment2_to_searchFragment)
        }

        settingsButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_chatFragment2_to_settingsFragment)
        }



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerViewChat = view.findViewById<RecyclerView>(R.id.recyclerviewChat)

        // Настройка RecyclerView
        chatAdapter = ChatAdapter()
        recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        // Получаем токен
        val sharedPref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)
        val buttonSend = view.findViewById<Button>(R.id.sendMessage)
        val editTextMessage = view.findViewById<MultiAutoCompleteTextView>(R.id.textMessageToSend)

        if (token == null) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            // Можно показать Toast или уведомление
            return
        }

        buttonSend.setOnClickListener {
            val userMessage = editTextMessage.text.toString()
            if (userMessage.isNotBlank()) {
                chatAdapter.addMessage(ChatMessage(userMessage, true))
                editTextMessage.setText("")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.apiService.sendMessage(token, MessageRequest(userMessage))
                        withContext(Dispatchers.Main) {
                            chatAdapter.addMessage(ChatMessage(response.response, false))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }




}