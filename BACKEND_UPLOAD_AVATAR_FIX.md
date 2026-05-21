# Backend Fix Required: Upload Avatar Endpoint

## 🔴 Vấn đề
Lỗi 500 khi upload ảnh vì backend không có (hoặc có nhưng sai) endpoint `/api/user/upload-avatar`

## ✅ Solution: Thêm/Sửa Endpoint trong UserController.java

Hãy thêm hoặc update endpoint này vào `UserController.java` của backend:

```java
@PostMapping("/upload-avatar")
public Map<String, Object> uploadAvatar(
        @RequestParam("id") String id,
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {
    
    try {
        // 1. Validate file
        if (file.isEmpty()) {
            return Map.of(
                "status", "error",
                "message", "File không được để trống"
            );
        }
        
        // 2. Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Map.of(
                "status", "error",
                "message", "File quá lớn (max 5MB)"
            );
        }
        
        // 3. Check file type
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            return Map.of(
                "status", "error",
                "message", "Chỉ chấp nhận file ảnh"
            );
        }
        
        // 4. Get user
        User user = userRepository.findById(Long.parseLong(id))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Không tìm thấy người dùng với ID: " + id));
        
        // 5. Check authorization - ensure user can only upload their own avatar
        String currentUsername = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        if (!currentUsername.equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Bạn không có quyền upload ảnh cho người dùng khác");
        }
        
        // 6. Save file
        String originalFilename = file.getOriginalFilename();
        String filename = System.currentTimeMillis() + "_" + originalFilename;
        
        // Create upload directory if not exists
        Path uploadDir = Paths.get("uploads/avatars");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Save file
        Path filepath = uploadDir.resolve(filename);
        Files.write(filepath, file.getBytes());
        
        // 7. Update user avatar URL
        String avatarUrl = "/uploads/avatars/" + filename;
        user.setAvatar_url(avatarUrl);
        userRepository.save(user);
        
        // 8. Return success response
        Map<String, Object> data = new HashMap<>();
        data.put("avatar_url", avatarUrl);
        
        return Map.of(
            "status", "success",
            "message", "Upload ảnh đại diện thành công",
            "data", data
        );
        
    } catch (Exception e) {
        e.printStackTrace();
        return Map.of(
            "status", "error",
            "message", "Lỗi upload ảnh: " + e.getMessage()
        );
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

## 🔄 Android Changes (Đã hoàn tất)

✅ RetrofitClient: Thêm Authorization interceptor
✅ ProfileViewModel: Fix field name thành "file" 
✅ ProfileScreen: Fix Uri to File conversion
✅ MainActivity: Initialize RetrofitClient

---

**Sau khi thêm backend endpoint này, upload ảnh sẽ hoạt động!** 🚀

