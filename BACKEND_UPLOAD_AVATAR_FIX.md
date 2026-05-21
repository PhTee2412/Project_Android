# Backend Fix Required: Upload Avatar Endpoint

## 🔴 Vấn đề
Lỗi 500 khi upload ảnh vì backend không có (hoặc có nhưng sai) endpoint `/api/user/upload-avatar`

## ✅ Các Import Cần Thiết

Thêm những import này vào `UserController.java`:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
```

## ✅ Solution: Thêm/Sửa Endpoint trong UserController.java

Hãy thêm hoặc update endpoint này vào `UserController.java` của backend (sau method `@PostMapping("/verify-email-update")`):
- **Đảm bảo** `@Autowired private Cloudinary cloudinary;` đã khai báo ở class level

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
        
        // Admin có thể upload cho bất kỳ ai, User chỉ upload được cho chính mình
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
        // ID không phải số
        response.put("status", "error");
        response.put("message", "ID không hợp lệ");
        return response;
        
    } catch (ResponseStatusException rse) {
        // Authorization hoặc Not Found
        response.put("status", "error");
        response.put("message", rse.getReason());
        return response;
        
    } catch (Exception e) {
        // Lỗi khác
        e.printStackTrace();
        response.put("status", "error");
        response.put("message", "Lỗi upload: " + e.getMessage());
        return response;
    }
}
```

## 📝 Cấu hình thêm cần thiết

### 1. application.properties hoặc application.yml
Thêm cấu hình cho multipart upload:

```properties
# application.properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
```

hoặc

```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
      enabled: true
```

### 2. Serve static files (uploads folder)

Tạo file `WebConfig.java`:

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
    }
}
```

## 🔒 Security Configuration

Nếu dùng Spring Security, thêm endpoint vào whitelist:

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/api/user/upload-avatar").authenticated()  // Require authentication
        .antMatchers("/uploads/**").permitAll()  // Allow public access to uploaded files
        // ... other configs
}
```

## 🧪 Test Upload Avatar

```bash
curl -X POST http://localhost:8080/api/user/upload-avatar \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "id=1" \
  -F "file=@/path/to/image.jpg"
```

## ✅ Expected Response

**Success (200)**:
```json
{
  "status": "success",
  "message": "Upload ảnh đại diện thành công",
  "data": {
    "avatar_url": "/uploads/avatars/1234567890_image.jpg"
  }
}
```

**Error (400/500)**:
```json
{
  "status": "error",
  "message": "Lỗi upload ảnh: [error details]"
}
```

## 📦 Dependencies cần thiết

Đảm bảo `pom.xml` hoặc `build.gradle` có:

```xml
<!-- Maven -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## ⚠️ LỖI PHỔ BIẾN & CÁCH FIX

### ❌ Lỗi 1: `@RequestParam("id") Long id` (Parameter type sai)
```
❌ SAIXA đưa ID dưới dạng RequestBody, không phải URL param
❌ Spring mong Long, nhưng nhận String
```
**✅ FIX**: Đổi thành `@RequestParam("id") String id` và convert sang Long

### ❌ Lỗi 2: Endpoint không save file
```
❌ Chỉ upload lên Cloudinary nhưng không lưu vào database
```
**✅ FIX**: Gọi `user.setAvatar_url(secureUrl)` rồi `userRepository.save(user)`

### ❌ Lỗi 3: Thiếu contentType check
```
❌ Chấp nhận file không phải ảnh
```
**✅ FIX**: Luôn check `contentType.startsWith("image/")`

### ❌ Lỗi 4: Không handle Authorization
```
❌ User có thể upload ảnh cho người khác
```
**✅ FIX**: Check `SecurityContextHolder` và so sánh username

## 🔍 CÁCH DEBUG

Nếu upload vẫn fail:

1. **Check logs** - Xem error message chi tiết
2. **Postman test** - Test endpoint bằng Postman trước
   - POST: `http://localhost:8080/api/user/upload-avatar`
   - Form-data: 
     - `id` = `1` (text)
     - `file` = chọn file ảnh
   - Headers: `Authorization: Bearer {your_token}`

3. **Validate Cloudinary config** - Kiểm tra Cloudinary credentials đã đúng chưa

## 🔄 Android Changes (Đã hoàn tất)

✅ RetrofitClient: Thêm Authorization interceptor
✅ ProfileViewModel: Fix field name thành "file" 
✅ ProfileScreen: Fix Uri to File conversion
✅ MainActivity: Initialize RetrofitClient

---

**Sau khi thêm backend endpoint này, upload ảnh sẽ hoạt động!** 🚀

