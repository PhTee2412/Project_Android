package com.example.smartlibrary.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    // ===== Google Sign-In với Android Client ID =====
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
            if (idToken.isBlank()) {
                Log.e("AuthScreen", "Google sign-in returned empty idToken: account=$account")
                viewModel.showMessage("Google sign-in không trả về idToken (code unknown)")
            } else {
                Log.d("AuthScreen", "Google sign-in success, got idToken")
                viewModel.loginWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            // Show detailed info to help debug DEVELOPER_ERROR (code 10)
            Log.e("AuthScreen", "Google sign-in failed", e)
            val code = e.statusCode
            viewModel.showMessage("Lỗi đăng nhập Google: code=$code ${e.localizedMessage ?: e.message}")
        }
    }

    // ===== Facebook Login =====
    val callbackManager = remember {
        (activity as? com.example.smartlibrary.MainActivity)?.callbackManager
            ?: CallbackManager.Factory.create()
    }
    val facebookLoginLauncher = remember { LoginManager.getInstance() }

    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken.token
                viewModel.loginWithFacebook(token)
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
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.activeTab == "login") "Đăng nhập" else "Đăng ký") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isOtpSent) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        TabButton("Đăng nhập", viewModel.activeTab == "login", Modifier.weight(1f)) {
                            viewModel.onTabSelected("login")
                        }
                        TabButton("Đăng ký", viewModel.activeTab == "signup", Modifier.weight(1f)) {
                            viewModel.onTabSelected("signup")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (viewModel.activeTab == "login") {
                        LoginForm(
                            viewModel = viewModel,
                            onGoogleClick = {
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            },
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
                } else {
                    OtpForm(viewModel)
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF30C9E8))
                }
            }
        }
    }
}

// Các Composable phụ trợ giữ nguyên từ code cũ của bạn
@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                if (isSelected) Color(0xFFE2E8F0) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (viewModel.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (viewModel.showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            shape = RoundedCornerShape(8.dp)
        )

        Text(
            text = "Quên mật khẩu?",
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp)
                .clickable { /* Chưa xử lý */ },
            color = Color(0xFF3B82F6),
            fontSize = 14.sp,
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30C9E8)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("Hoặc đăng nhập với", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Tiếp tục với Google", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onFacebookClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Tiếp tục với Facebook", color = Color(0xFF1877F2))
        }
    }
}

@Composable
fun RegisterForm(viewModel: AuthViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it },
            label = { Text("Tên người dùng") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.identifier,
            onValueChange = { viewModel.identifier = it },
            label = { Text("Email hoặc Số điện thoại") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.birthdate,
            onValueChange = { viewModel.birthdate = it },
            label = { Text("Ngày sinh (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Giới tính", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        val genderOptions = listOf("Nam", "Nữ", "Khác")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            genderOptions.forEach { text ->
                Row(
                    Modifier
                        .selectable(
                            selected = (text == viewModel.gender),
                            onClick = { viewModel.gender = text },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == viewModel.gender),
                        onClick = null
                    )
                    Text(text = text, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30C9E8)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OtpForm(viewModel: AuthViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Xác thực OTP", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            "Vui lòng nhập mã OTP đã được gửi đến email của bạn",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = viewModel.otp,
            onValueChange = { if (it.length <= 6) viewModel.otp = it },
            label = { Text("Mã OTP") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.verifyOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Xác nhận OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}