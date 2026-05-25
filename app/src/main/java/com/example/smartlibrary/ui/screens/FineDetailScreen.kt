package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.network.BorrowedBookBrief
import com.example.smartlibrary.ui.viewmodel.FinesViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FineDetailScreen(
    fineId: String,
    viewModel: FinesViewModel,
    onBack: () -> Unit
) {
    val fine by viewModel.selectedFine.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(fineId) {
        viewModel.loadFineDetail(fineId)
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết phiếu phạt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (fine != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    shadowElevation = 16.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    val isPaid = fine?.trangThai == "DA_THANH_TOAN"
                    Button(
                        onClick = { if (!isPaid) viewModel.payFine(context, fineId) },
                        enabled = !isPaid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9CE5F4),
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isPaid) {
                            Text("ĐÃ THANH TOÁN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        } else {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("THANH TOÁN NGAY", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF062D76))
            }
        } else if (fine == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy dữ liệu", color = Color.Gray)
            }
        } else {
            val currentFine = fine!!
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                item {
                    FineInfoCard(currentFine)
                }

                // Use local variable to allow smart casting for property with custom getter
                val card = currentFine.cardId
                val borrowedBooks = card?.borrowedBooks
                if (borrowedBooks != null && borrowedBooks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Thông tin sách liên quan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF062D76),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(borrowedBooks) { book ->
                        FineBookCard(book)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun FineInfoCard(fine: com.example.smartlibrary.network.FineDetailResponse) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Thông tin phiếu",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRowItem("Mã phiếu phạt", "#${fine.id}")
                InfoRowItem("Số tiền cần nộp", currencyFormatter.format(fine.soTien ?: 0.0), valueColor = Color(0xFFD32F2F))
                InfoRowItem("Lý do phạt", fine.noiDung ?: "N/A")
                
                // Captured in a local variable to allow smart casting
                val card = fine.cardId
                if (card != null) {
                    InfoRowItem("Mã phiếu mượn", "#${card.id}")
                }

                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                InfoRowItem("Người vi phạm", fine.tenND ?: fine.userId?.toString() ?: "N/A")
                if (fine.trangThai == "DA_THANH_TOAN") {
                    InfoRowItem("Ngày thanh toán", fine.ngayThanhToan ?: "N/A", valueColor = Color(0xFF388E3C))
                }
            }
        }
    }
}

@Composable
fun InfoRowItem(label: String, value: String, valueColor: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 15.sp, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FineBookCard(book: BorrowedBookBrief) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = book.image,
                contentDescription = book.name,
                modifier = Modifier
                    .size(width = 70.dp, height = 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = book.name ?: "N/A",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(text = "Tác giả: ${book.author ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)
                Text(text = "NXB: ${book.publisher ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
