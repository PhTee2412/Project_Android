# 🔴 Upload Avatar - Các Vấn Đề và Fix

## 📋 Tóm tắt vấn đề

Bạn có code backend endpoint nhưng nó **sai ở 3 chỗ chính**:

### ❌ **Vấn đề 1: Parameter `@RequestParam("id") Long id` sai**

**Code hiện tại:**
```java
@PostMapping("/upload-avatar")
public Map<String, Object> uploadAvatar(
        @RequestParam("id") Long id,  // ❌ WRONG: Spring mong Long
        @RequestParam("file") MultipartFile file) {
```

**Android gửi:**
```kotlin
val idPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())
// gửi String dưới dạng RequestBody, không phải URL param
```

**Problem**: `@RequestParam("id")` Spring tìm trong URL query, không trong form-data. Và Android gửi id qua form-data, không URL.

**✅ FIX:**
```java
@PostMapping("/upload-avatar")
public Map<String, Object> uploadAvatar(
        @RequestParam("id") String id,  // ✅ CORRECT: nhận String
        @RequestParam("file") MultipartFile file) {
    try {
        // Convert String to Long
        Long userId = Long.parseLong(id);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ...));
        // ...
```

---

### ❌ **Vấn đề 2: Không check Authorization**

**Code hiện tại:**
```java
User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy User"));

user.setAvatar_url(secureUrl);
userRepository.save(user);
```

**Problem**: Bất kỳ user nào cũng có thể upload ảnh cho bất kỳ user nào khác → **Security hole**

**✅ FIX:**
```java
// Check authorization - User chỉ được upload ảnh cho chính mình
String currentUsername = SecurityContextHolder.getContext()
        .getAuthentication().getName();
String currentRole = SecurityContextHolder.getContext()
        .getAuthentication().getAuthorities().iterator().next().getAuthority();

// Admin có thể upload cho bất kỳ ai, User chỉ upload được cho chính mình
if (currentRole.equals("USER") && !currentUsername.equals(user.getUsername())) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
            "Bạn không có quyền upload ảnh cho người dùng khác");
}
```

---

### ❌ **Vấn đề 3: Không handle Exception đầy đủ**

**Code hiện tại:**
```java
try {
    // ...
    return Map.of(...);
} catch (Exception e) {
    return Map.of(
        "status", "error",
        "message", "Lỗi upload: " + e.getMessage(),
        "data", Map.of("reason", "Internal Server Error")
    );
}
```

**Problem**: 
- Không handle `NumberFormatException` khi convert ID
- Không log error chi tiết
- Response structure không consistent

**✅ FIX:**
```java
Map<String, Object> response = new HashMap<>();

try {
    // 1. Validate file
    if (file == null || file.isEmpty()) {
        response.put("status", "error");
        response.put("message", "File không được để trống");
        return response;
    }
    
    // 2. Validate file type
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        response.put("status", "error");
        response.put("message", "Chỉ chấp nhận file ảnh");
        return response;
    }
    
    // 3. Validate file size
    long maxSize = 5 * 1024 * 1024;
    if (file.getSize() > maxSize) {
        response.put("status", "error");
        response.put("message", "File quá lớn (max 5MB)");
        return response;
    }
    
    // 4. Rest of code...
    
} catch (NumberFormatException e) {
    response.put("status", "error");
    response.put("message", "ID không hợp lệ");
    return response;
} catch (ResponseStatusException rse) {
    response.put("status", "error");
    response.put("message", rse.getReason());
    return response;
} catch (Exception e) {
    e.printStackTrace();
    response.put("status", "error");
    response.put("message", "Lỗi upload: " + e.getMessage());
    return response;
}
```

---

## 🔧 COMPLETE FIXED CODE

**File:** `UserController.java`

**Add này vào class (sau `@PostMapping("/verify-email-update")` method):**

```java
@PostMapping("/upload-avatar")
public Map<String, Object> uploadAvatar(
        @RequestParam("id") String id,
        @RequestParam("file") MultipartFile file) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        // ✅ 1. Validate file không rỗng
        if (file == null || file.isEmpty()) {
            response.put("status", "error");
            response.put("message", "File không được để trống");
            return response;
        }
        
        // ✅ 2. Validate file type (phải là ảnh)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("status", "error");
            response.put("message", "Chỉ chấp nhận file ảnh");
            return response;
        }
        
        // ✅ 3. Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            response.put("status", "error");
            response.put("message", "File quá lớn (max 5MB)");
            return response;
        }
        
        // ✅ 4. Get user
        Long userId = Long.parseLong(id);  // Convert String to Long
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Không tìm thấy người dùng với ID: " + id));
        
        // ✅ 5. Check authorization - User chỉ được upload ảnh cho chính mình
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        String currentRole = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().iterator().next().getAuthority();
        
        if (currentRole.equals("USER") && !currentUsername.equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền upload ảnh cho người dùng khác");
        }
        
        // ✅ 6. Upload to Cloudinary (giống UploadService)
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.emptyMap());
        
        String secureUrl = (String) uploadResult.get("secure_url");
        
        // ✅ 7. Update avatar_url trong database
        user.setAvatar_url(secureUrl);
        userRepository.save(user);
        
        // ✅ 8. Return success response (đúng format mà Android chờ)
        Map<String, Object> data = new HashMap<>();
        data.put("avatar_url", secureUrl);
        
        response.put("status", "success");
        response.put("message", "Upload ảnh đại diện thành công");
        response.put("data", data);
        
        return response;
        
    } catch (NumberFormatException e) {
        response.put("status", "error");
        response.put("message", "ID không hợp lệ");
        return response;
    } catch (ResponseStatusException rse) {
        response.put("status", "error");
        response.put("message", rse.getReason());
        return response;
    } catch (Exception e) {
        e.printStackTrace();
        response.put("status", "error");
        response.put("message", "Lỗi upload: " + e.getMessage());
        return response;
    }
}
```

---

## ✅ Imports cần thiết

Đảm bảo class `UserController` có những imports này:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
```

Và `@Autowired private Cloudinary cloudinary;` đã khai báo ở class level.

---

## 🧪 Cách test

### 1. Postman
- **Method**: POST
- **URL**: `http://localhost:8080/api/user/upload-avatar`
- **Headers**: 
  - `Authorization: Bearer {your_access_token}`
  - `Content-Type: multipart/form-data` (tự động)
- **Body** (Form-data):
  - Key: `id`, Value: `1` (text)
  - Key: `file`, Value: chọn file ảnh
  
### 2. Logs
```
Nếu thất bại, kiểm tra console logs để xem error chi tiết
```

### 3. Database
```
Sau khi upload, check field avatar_url của user trong DB
Phải có Cloudinary URL: https://res.cloudinary.com/.../image.jpg
```

---

## 📊 Request/Response Flow

```
Android App
  ↓
POST /api/user/upload-avatar
  ├─ Headers: Authorization: Bearer {token}
  ├─ Form-data:
  │  ├─ id = "1"
  │  └─ file = [image file]
  ↓
Backend UserController.uploadAvatar()
  ├─ ✅ Validate file
  ├─ ✅ Check authorization
  ├─ ✅ Get user from DB
  ├─ ✅ Upload to Cloudinary
  ├─ ✅ Update DB
  ↓
Response:
{
  "status": "success",
  "message": "Upload ảnh đại diện thành công",
  "data": {
    "avatar_url": "https://res.cloudinary.com/.../image.jpg"
  }
}
  ↓
Android App
  ├─ Update UI with new avatar
  └─ Show success message
```

---

## 🎯 Tóm tắt

| Vấn đề | Nguyên nhân | Fix |
|--------|-----------|-----|
| Parameter sai | `@RequestParam("id") Long id` | Đổi thành `String id` và convert sang Long |
| Không check auth | Bất kỳ user nào có thể upload cho ai | Thêm `SecurityContextHolder` check |
| Exception không tốt | Không handle `NumberFormatException` | Thêm các catch block riêng |
| Không validate file | Chấp nhận mọi file | Thêm check: size, type, not empty |

**Status**: ✅ Tất cả vấn đề đã được xác định và có fix rồi!

