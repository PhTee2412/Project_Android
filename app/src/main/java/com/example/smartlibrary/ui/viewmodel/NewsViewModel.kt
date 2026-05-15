package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartlibrary.data.model.News
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewsViewModel : ViewModel() {
    private val _newsList = MutableStateFlow<List<News>>(getMockNewsList())
    val newsList: StateFlow<List<News>> = _newsList.asStateFlow()

    private val _selectedNews = MutableStateFlow<News?>(null)
    val selectedNews: StateFlow<News?> = _selectedNews.asStateFlow()

    fun selectNews(news: News) {
        _selectedNews.value = news
    }

    private fun getMockNewsList(): List<News> {
        return listOf(
            News(
                title = "Ra mắt bản dịch tác phẩm 'Cuốn sách hoang dã' của tác giả Juan Villoro",
                date = "Thứ Ba, 06/05/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/494621307-1097261965774870-6550418258675179038-n.jpg?v=1746519986820",
                content = """
                    Tháng Năm này, Nhã Nam trân trọng giới thiệu tới bạn đọc cuốn sách "Cuốn sách hoang dã" của tác giả Juan Villoro.
                    
                    Thông tin sự kiện:
                    ⏰ Thời gian: 9:30 sáng thứ Sáu, ngày 9/5/2025
                    📍 Địa điểm: Phòng 102C - Trường Đại học Hà Nội
                """.trimIndent()
            ),
            News(
                title = "Sự kiện giới thiệu tác phẩm 'Dọc đường 2' và gặp gỡ nhà văn Nguyên",
                date = "Thứ Hai, 21/04/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/491279363-1088490436652023-2743767814237791180-n.jpg?v=1745208865993"
            ),
            News(
                title = "Hội sách Nhã Nam chào hè 2025",
                date = "Thứ Ba, 08/04/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/489006758-1079586880875712-661463280947496865-n.jpg?v=1744100699603"
            ),
            News(
                title = "Sự kiện: NHỮNG CÂU CHUYỆN NGHỀ PHÁP Y - Giới thiệu bộ sách pháp y",
                date = "Thứ Hai, 24/03/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/484799317-1063744625793271-7677298375345374751-n.jpg?v=1742792125167"
            ),
            News(
                title = "Trò chuyện về cuốn sách: Chuyện nhà Tí của nhà văn Phan Thị Vàng Anh",
                date = "Thứ Hai, 17/03/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/website-a-nh-da-i-die-n-ba-i-vie-t-17-51a80241-426c-4785-b5e1-9aedf8dff8c0.png?v=1742197752157"
            ),
            News(
                title = "Sự kiện giao lưu với tác giả và dịch giả 'Bố con cá gai'",
                date = "Thứ Hai, 03/03/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/480203269-1042894241211643-9180551152830458713-n.jpg?v=1740994652690"
            ),
            News(
                title = "'Quyền lực' của đất đai",
                date = "Chủ Nhật, 02/03/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/website-a-nh-da-i-die-n-ba-i-vie-t-14-42424747-4313-46a1-a9d9-0375b3214194.png?v=1740891085440"
            ),
            News(
                title = "Sự kiện: Ra mắt cuốn sách ĐẤT ĐAI - Ham muốn sở hữu định hình thế giới hiện đại",
                date = "Thứ Sáu, 21/02/2025",
                imageUrl = "https://bizweb.dktcdn.net/100/363/455/articles/1-13cd774c-b58f-461e-9a81-9e614842fb12.png?v=1740115538713"
            )
        )
    }
}
