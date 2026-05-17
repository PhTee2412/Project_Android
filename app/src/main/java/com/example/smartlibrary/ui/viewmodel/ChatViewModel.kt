package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.model.ChatMessage
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.ChatRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val apiService: ApiService) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        // Khởi tạo tin nhắn chào mừng từ Hehe
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

        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            text = text,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )

        _messages.value = _messages.value + userMessage
        _inputText.value = ""

        // Gọi API
        viewModelScope.launch {
            _isTyping.value = true
            try {
                // Thêm delay nhỏ để người dùng thấy hiệu ứng typing
                delay(1000)

                // Gọi API thật (nếu có)
                // val response = apiService.sendChatMessage(ChatRequest(userId = "user123", message = text))
                // val reply = response.reply

                // Hiện tại dùng mock response dựa trên nội dung (giả lập logic server)
                val reply = getMockReply(text)

                val aiMessage = ChatMessage(
                    id = System.currentTimeMillis() + 1,
                    text = reply,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    id = System.currentTimeMillis(),
                    text = "Rất tiếc, đã có lỗi xảy ra khi kết nối với máy chủ.",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
            } finally {
                _isTyping.value = false
            }
        }
    }

    private fun getMockReply(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("sách") || lower.contains("tìm") -> "Thư viện hiện có rất nhiều sách mới về CNTT, Kinh tế và Văn học. Bạn muốn tìm chủ đề nào cụ thể không?"
            lower.contains("giờ") || lower.contains("mở cửa") -> "Thư viện mở cửa từ 8:00 sáng đến 9:00 tối các ngày trong tuần, và từ 8:00 đến 5:00 chiều Chủ Nhật."
            lower.contains("mượn") -> "Để mượn sách, bạn vui lòng chọn cuốn sách yêu thích và nhấn 'Mượn sách' hoặc thêm vào giỏ hàng nhé."
            lower.contains("chào") || lower.contains("hi") -> "Chào bạn! Chúc bạn một ngày đọc sách vui vẻ. Tôi có thể giúp gì cho bạn?"
            else -> "Tôi đã nhận được câu hỏi của bạn. Để tôi kiểm tra thông tin và phản hồi sớm nhất nhé!"
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                id = System.currentTimeMillis(),
                text = "Xin chào! Tui là Hehe - trợ lý thư viện. Tui có thể giúp gì được cho bạnn? \n\nLưu ý: phiên làm việc sẽ được tự động làm mới khi bạn đăng xuất/ đóng ứng dụng nhé!!",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
