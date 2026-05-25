package com.example.smartlibrary.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.R
import com.example.smartlibrary.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as Activity
    val scrollState = rememberScrollState()

    // Log để debug
    LaunchedEffect(isOtpSent) {
        Log.d("AuthScreen", "isOtpSent changed to $isOtpSent")
    }

    // Tự động cuộn lên đầu khi chuyển sang form OTP hoặc Quên mật khẩu
    LaunchedEffect(isOtpSent, viewModel.forgotPasswordStep) {
        if (isOtpSent || viewModel.forgotPasswordStep != "none") {
            scrollState.animateScrollTo(0)
        }
    }

    // ===== Google Sign-In setup =====
    val androidClientId = "762941990924-gv4gegj70j1b46i3j4v6tlcsj4gmpn85.apps.googleusercontent.com"
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(androidClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(activity, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: ""
            if (idToken.isNotBlank()) {
                viewModel.loginWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            viewModel.showMessage("Lỗi đăng nhập Google: ${e.statusCode}")
        }
    }

    // ===== Facebook Login setup =====
    val callbackManager = remember {
        (activity as? com.example.smartlibrary.MainActivity)?.callbackManager
            ?: CallbackManager.Factory.create()
    }
    val facebookLoginLauncher = remember { LoginManager.getInstance() }

    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.loginWithFacebook(result.accessToken.token)
            }
            override fun onCancel() { viewModel.showMessage("Đăng nhập Facebook bị hủy") }
            override fun onError(error: FacebookException) { viewModel.showMessage("Lỗi Facebook: ${error.message}") }
        }
        facebookLoginLauncher.registerCallback(callbackManager, callback)
        onDispose { facebookLoginLauncher.unregisterCallback(callbackManager) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header: Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillWidth
                    )

                    IconButton(
                        onClick = {
                            when {
                                isOtpSent -> viewModel.cancelOtp()
                                viewModel.forgotPasswordStep != "none" -> viewModel.forgotPasswordStep = "none"
                                else -> onBack()
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-30).dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 32.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            isOtpSent -> {
                                OtpForm(viewModel)
                            }
                            viewModel.forgotPasswordStep == "request" -> {
                                ForgotPasswordRequestForm(viewModel)
                            }
                            viewModel.forgotPasswordStep == "reset" -> {
                                ForgotPasswordResetForm(viewModel)
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (viewModel.activeTab == "login") "Chào mừng trở lại!" else "Tạo tài khoản mới",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1F2937)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .background(Color(0xFFF3F4F6), RoundedCornerShape(27.dp))
                                            .padding(4.dp)
                                    ) {
                                        ModernTabButton(
                                            text = "ĐĂNG NHẬP",
                                            isSelected = viewModel.activeTab == "login",
                                            modifier = Modifier.weight(1f)
                                        ) { viewModel.onTabSelected("login") }

                                        ModernTabButton(
                                            text = "ĐĂNG KÝ",
                                            isSelected = viewModel.activeTab == "signup",
                                            modifier = Modifier.weight(1f)
                                        ) { viewModel.onTabSelected("signup") }
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))

                                    if (viewModel.activeTab == "login") {
                                        LoginForm(
                                            viewModel = viewModel,
                                            onGoogleClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                                            onFacebookClick = {
                                                facebookLoginLauncher.logInWithReadPermissions(
                                                    activity,
                                                    listOf("email", "public_profile")
                                                )
                                            }
                                        )
                                    } else {
                                        RegisterForm(viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading overlay – không hiển thị khi đang ở form OTP
            if (isLoading && !isOtpSent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(24.dp),
                            color = Color(0xFF30C9E8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(23.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF30C9E8) else Color.Gray
            )
        }
    }
}

@Composable
fun LoginForm(
    viewModel: AuthViewModel,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = viewModel.identifier,
            onValueChange = { viewModel.identifier = it },
            label = { Text("Email hoặc Số điện thoại") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF30C9E8)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30C9E8),
                focusedLabelColor = Color(0xFF30C9E8)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF30C9E8)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (viewModel.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (viewModel.showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30C9E8),
                focusedLabelColor = Color(0xFF30C9E8)
            )
        )

        Text(
            text = "Quên mật khẩu?",
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 12.dp)
                .clickable { viewModel.forgotPasswordStep = "request" },
            color = Color(0xFF30C9E8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30C9E8)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("ĐĂNG NHẬP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text(
                "Hoặc đăng nhập với",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 12.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialAuthButton(
                icon = Icons.Default.AccountCircle,
                text = "Google",
                modifier = Modifier.weight(1f),
                onClick = onGoogleClick
            )
            SocialAuthButton(
                icon = Icons.Default.Facebook,
                text = "Facebook",
                modifier = Modifier.weight(1f),
                onClick = onFacebookClick
            )
        }
    }
}

@Composable
fun SocialAuthButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (text == "Facebook") Color(0xFF1877F2) else Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterForm(viewModel: AuthViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        viewModel.birthdate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it },
            label = { Text("Tên người dùng") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF30C9E8)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30C9E8),
                focusedLabelColor = Color(0xFF30C9E8)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.identifier,
            onValueChange = { viewModel.identifier = it },
            label = { Text("Email hoặc Số điện thoại") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF30C9E8)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30C9E8),
                focusedLabelColor = Color(0xFF30C9E8)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF30C9E8)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30C9E8),
                focusedLabelColor = Color(0xFF30C9E8)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.birthdate,
            onValueChange = { },
            label = { Text("Ngày sinh (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF30C9E8)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            singleLine = true,
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledLeadingIconColor = Color(0xFF30C9E8)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Giới tính", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Nam", "Nữ", "Khác").forEach { text ->
                Row(
                    Modifier.selectable(
                        selected = (text == viewModel.gender),
                        onClick = { viewModel.gender = text },
                        role = Role.RadioButton
                    ).padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == viewModel.gender),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF30C9E8))
                    )
                    Text(text = text, modifier = Modifier.padding(start = 4.dp), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30C9E8)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("ĐĂNG KÝ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OtpForm(viewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Mail,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF30C9E8)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Xác thực OTP", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1F2937))
        Text(
            "Mã OTP đã được gửi đến: ${viewModel.identifier}",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = viewModel.otp,
            onValueChange = { if (it.length <= 6) viewModel.otp = it },
            label = { Text("Mã OTP") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF30C9E8),
                focusedLabelColor = Color(0xFF30C9E8)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.verifyOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30C9E8)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("XÁC NHẬN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.cancelOtp() }) {
            Text("Quay lại đăng ký", color = Color.Gray, textDecoration = TextDecoration.Underline)
        }
    }
}

@Composable
fun ForgotPasswordRequestForm(viewModel: AuthViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quên mật khẩu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = viewModel.identifier,
            onValueChange = { viewModel.identifier = it },
            label = { Text("Email hoặc số điện thoại") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.requestForgotPassword() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Gửi OTP", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ForgotPasswordResetForm(viewModel: AuthViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Đặt lại mật khẩu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Mã OTP đã gửi đến: ${viewModel.identifier}",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = viewModel.otp,
            onValueChange = { viewModel.otp = it },
            label = { Text("Nhập mã OTP") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.newPassword,
            onValueChange = { viewModel.newPassword = it },
            label = { Text("Mật khẩu mới") },
            visualTransformation = if (viewModel.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(if (viewModel.showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.resetForgotPassword() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
        ) {
            Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { viewModel.requestForgotPassword() },
            enabled = viewModel.resendTimeout <= 0
        ) {
            Text(
                if (viewModel.resendTimeout > 0) "Gửi lại OTP (${viewModel.resendTimeout}s)" else "Gửi lại OTP",
                color = Color(0xFF2563EB),
                textDecoration = TextDecoration.Underline
            )
        }
    }
}
