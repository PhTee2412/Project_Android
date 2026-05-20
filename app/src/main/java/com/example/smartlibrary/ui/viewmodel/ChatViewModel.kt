package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.data.model.ChatMessage
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        _messages.value = listOf(
            ChatMessage(
                id = 1,
                text = "Xin chào! Tui là Hehe - trợ lý thư viện. Tui có thể giúp gì được cho bạn? \n\nLưu ý: Phiên làm việc sẽ được tự động làm mới khi bạn đăng xuất/ đóng ứng dụng nhé!!",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        if (userId.isEmpty()) {
            _messages.value = _messages.value + ChatMessage(
                id = System.currentTimeMillis(),
                text = "Bạn cần đăng nhập để sử dụng chat.",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            return
        }

        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            text = text,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _inputText.value = ""

        viewModelScope.launch {
            _isTyping.value = true
            try {
                val response = apiService.sendChatMessage(
                    ChatRequest(userId = userId, message = text)
                )
                _messages.value = _messages.value + ChatMessage(
                    id = System.currentTimeMillis() + 1,
                    text = response.reply,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    id = System.currentTimeMillis(),
                    text = "Hehe gặp sự cố: ${e.localizedMessage}",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                id = System.currentTimeMillis(),
                text = "Xin chào! Tui là Hehe - trợ lý thư viện. Tui có thể giúp gì được cho bạn? \n\nLưu ý: Phiên làm việc sẽ được tự động làm mới khi bạn đăng xuất/ đóng ứng dụng nhé!!",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}