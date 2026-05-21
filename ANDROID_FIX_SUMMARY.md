# Android Upload Avatar Fix - Complete Summary

## 🎯 Vấn đề Gốc
- Lỗi 500 khi upload ảnh
- Authorization header không được gửi (backend chặn do không có token)
- Field name không match backend (gửi "avatar" thay vì "file")
- Uri to File conversion sai cách

## ✅ Các Fix Đã Thực Hiện

### 1. **RetrofitClient.kt** - Thêm Authorization Interceptor
**Vấn đề**: Mọi API call không có Bearer token
**Fix**:
- Refactor thành function `getOkHttpClient()` 
- Thêm Authorization interceptor để inject `Bearer $token` vào mọi request
- Gọi `RetrofitClient.initialize(context)` để cấp SessionManager

```kotlin
// Trước
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor()...)
    .build()

// Sau
private fun getOkHttpClient(): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor()...)
    
    builder.addInterceptor { chain ->
        val token = sessionManager?.getAccessToken()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        // ...
    }
}
```

### 2. **MainActivity.kt** - Khởi tạo RetrofitClient
**Vấn đề**: RetrofitClient không có context để lấy token
**Fix**:
- Gọi `RetrofitClient.initialize(this)` trong `onCreate()`
- Này cho phép interceptor truy cập SessionManager

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize RetrofitClient with context for Authorization header
    RetrofitClient.initialize(this)
    
    // ... rest of code
}
```

### 3. **ProfileViewModel.kt** - Fix Upload Avatar
**Vấn đề**: 
- Field name sai ("avatar" thay vì "file")
- Error handling không tốt

**Fix**:
- Đổi `addFormDataPart("avatar", ...)` → `addFormDataPart("file", ...)`
- Cải thiện media type detection (PNG, JPG, GIF)
- Thêm error body trong message

```kotlin
fun uploadAvatar(file: File) {
    // Determine media type based on file extension
    val mediaType = when {
        file.extension.lowercase() == "png" -> "image/png".toMediaTypeOrNull()
        file.extension.lowercase() == "jpg" || file.extension.lowercase() == "jpeg" -> "image/jpeg".toMediaTypeOrNull()
        file.extension.lowercase() == "gif" -> "image/gif".toMediaTypeOrNull()
        else -> "image/*".toMediaTypeOrNull()
    }

    val requestFile = file.asRequestBody(mediaType)
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("id", userId)
        .addFormDataPart("file", file.name, requestFile)  // ✅ "file" not "avatar"
        .build()

    val response = apiService.uploadAvatar(body)
    
    if (response.isSuccessful) {
        val responseBody = response.body()
        if (responseBody?.data?.avatar_url != null) {
            _userInfo.value = _userInfo.value?.copy(avatarUrl = responseBody.data.avatar_url)
            _message.value = "Upload ảnh đại diện thành công"
        }
    } else {
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        _message.value = "Upload thất bại (${response.code()}): $errorBody"
    }
}
```

### 4. **ProfileScreen.kt** - Fix Uri to File Conversion
**Vấn đề**: 
- `File(uri.path)` trả về null hoặc invalid path
- ContentResolver không được dùng đúng cách

**Fix**:
- Lấy InputStream từ ContentResolver
- Copy sang file tạm trong cache directory
- Dùng proper stream handling

```kotlin
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        try {
            // ✅ Get input stream from content resolver
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Create temp file in cache directory
                val cacheDir = context.cacheDir
                val tempFile = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                
                // Copy stream to file
                inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Upload
                viewModel.uploadAvatar(tempFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```

## 📊 Request Flow (Trước → Sau)

### Trước
```
Android App
    ↓
Request: POST /api/user/upload-avatar
  - Headers: ❌ NO Authorization
  - Body: id + avatar (WRONG FIELD NAME)
    ↓
Backend (Spring)
    ↓
❌ 401 Unauthorized OR 400 Bad Request
    ↓
❌ 500 Error
```

### Sau
```
Android App
    ↓
RetrofitClient.initialize() injected token
    ↓
Request: POST /api/user/upload-avatar
  - Headers: ✅ Authorization: Bearer {token}
  - Body: id + file (CORRECT FIELD NAME)
    ↓
Backend (Spring)
    ↓
✅ Validate token
✅ Save file
✅ Update avatar_url
    ↓
✅ 200 Success + avatar_url response
```

## 🧪 Testing Checklist

- [ ] Build & run app
- [ ] Login successfully (token saved in SharedPreferences)
- [ ] Go to Profile → Sửa
- [ ] Click "Choose File" → Select image
- [ ] Click "Upload"
- [ ] Should see "Upload ảnh đại diện thành công"
- [ ] Avatar should update on screen

## 🔗 Related Files Modified

1. `D:\CODE\NT118\SmartLibrary\app\src\main\java\com\example\smartlibrary\network\RetrofitClient.kt`
2. `D:\CODE\NT118\SmartLibrary\app\src\main\java\com\example\smartlibrary\MainActivity.kt`
3. `D:\CODE\NT118\SmartLibrary\app\src\main\java\com\example\smartlibrary\ui\viewmodel\ProfileViewModel.kt`
4. `D:\CODE\NT118\SmartLibrary\app\src\main\java\com\example\smartlibrary\ui\screens\ProfileScreen.kt`

## ⚠️ Backend Fix Required

**NHỚ**: Backend cần có endpoint `/api/user/upload-avatar` với:
- Accept: `@RequestParam("id")` + `@RequestParam("file")`
- Save file to uploads directory
- Update user avatar_url in database
- Return: `{"status": "success", "data": {"avatar_url": "..."}}`

Chi tiết: Xem file `BACKEND_UPLOAD_AVATAR_FIX.md`

---

**Status**: ✅ Android fixes hoàn tất, chờ backend endpoint

