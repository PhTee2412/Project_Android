package com.example.smartlibrary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ServiceHoursCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Tương đương blue-50
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9CE5F4), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Thời gian phục vụ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(text = "Thứ 2 - Thứ 6: ", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "7:30 - 16:30", color = Color.Black)
                }
                Row {
                    Text(text = "Thứ 7: ", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "8:00 - 16:00", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Thư viện không phục vụ vào chủ nhật, ngày lễ, tết theo quy định và các ngày nghỉ đột xuất khác (có thông báo). Bạn đọc vui lòng theo dõi thông báo để sớm cập nhật những thông tin mới nhất !",
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.weight(1f))


        }
    }
}
