# 📋 Backend Upload Avatar - Copy-Paste Ready Code

## 🎯 Where to Paste This Code

**File**: `UserController.java` (in your backend Spring Boot project)
**Location**: Add this method to the existing `UserController` class

---

## 📝 Complete Endpoint Code

```java
@PostMapping("/upload-avatar")
public Map<String, Object> uploadAvatar(
        @RequestParam("id") String id,
        @RequestParam("file") MultipartFile file) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        // ===== 1. VALIDATE FILE =====
        if (file.isEmpty()) {
            response.put("status", "error");
            response.put("message", "File không được để trống");
            return response;
        }
        
        // ===== 2. CHECK FILE SIZE (MAX 5MB) =====
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            response.put("status", "error");
            response.put("message", "File quá lớn (max 5MB)");
            return response;
        }
        
        // ===== 3. CHECK FILE TYPE (MUST BE IMAGE) =====
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("status", "error");
            response.put("message", "Chỉ chấp nhận file ảnh (JPG, PNG, GIF, v.v.)");
            return response;
        }
        
        // ===== 4. GET USER FROM DATABASE =====
        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với ID: " + id));
        
        // ===== 5. CHECK AUTHORIZATION =====
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (!currentUsername.equals(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền upload ảnh cho người dùng khác");
        }
        
        // ===== 6. GENERATE FILENAME =====
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = System.currentTimeMillis() + extension;
        
        // ===== 7. CREATE UPLOAD DIRECTORY IF NOT EXISTS =====
        Path uploadDir = Paths.get("uploads/avatars");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // ===== 8. SAVE FILE =====
        Path filepath = uploadDir.resolve(filename);
        Files.write(filepath, file.getBytes());
        
        // ===== 9. UPDATE USER AVATAR URL IN DATABASE =====
        String avatarUrl = "/uploads/avatars/" + filename;
        user.setAvatar_url(avatarUrl);
        userRepository.save(user);
        
        // ===== 10. RETURN SUCCESS RESPONSE =====
        Map<String, Object> data = new HashMap<>();
        data.put("avatar_url", avatarUrl);
        
        response.put("status", "success");
        response.put("message", "Upload ảnh đại diện thành công");
        response.put("data", data);
        
        return response;
        
    } catch (ResponseStatusException rse) {
        response.put("status", "error");
        response.put("message", rse.getReason());
        return response;
        
    } catch (Exception e) {
        e.printStackTrace();
        response.put("status", "error");
        response.put("message", "Lỗi upload ảnh: " + e.getMessage());
        return response;
    }
}
```

---

## 📦 Required Imports

Add these to the top of `UserController.java`:

```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;
```

---

## ⚙️ Application Configuration

### Option 1: application.properties

Add these lines to `application.properties`:

```properties
# Multipart file upload configuration
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.location=/tmp

# File upload directory
upload.directory=uploads
```

### Option 2: application.yml

Add this to `application.yml`:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
      enabled: true
      location: /tmp

upload:
  directory: uploads
```

---

## 🌐 Static File Serving Configuration

Create new file: `WebConfig.java`

```java
package com.library_web.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configure static resource handlers for uploaded files
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

---

## 🔒 Security Configuration (Optional)

If using Spring Security, update your `SecurityConfig` to allow access to uploaded files:

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            // User profile endpoints - require authentication
            .antMatchers("/api/user/**").authenticated()
            
            // Upload endpoint - require authentication
            .antMatchers("/api/user/upload-avatar").authenticated()
            
            // Uploaded files - allow public access
            .antMatchers("/uploads/**").permitAll()
            
            // Login/Register - allow public
            .antMatchers("/api/auth/**", "/api/login", "/api/register").permitAll()
            
            // All other endpoints
            .anyRequest().authenticated()
            .and()
        .csrf().disable()
        .build();
}
```

---

## 🧪 Test the Endpoint

### Using cURL

```bash
# 1. First login to get token
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Copy the access_token from response

# 2. Upload avatar
curl -X POST http://localhost:8080/api/user/upload-avatar \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -F "id=1" \
  -F "file=@/path/to/your/image.jpg"
```

### Using Postman

1. **Create new POST request**: `http://localhost:8080/api/user/upload-avatar`
2. **Headers**:
   - `Authorization: Bearer {your_token}`
3. **Body** (form-data):
   - Key: `id` → Value: `1`
   - Key: `file` → Select file from computer
4. **Send**

### Expected Response (Success - 200)

```json
{
  "status": "success",
  "message": "Upload ảnh đại diện thành công",
  "data": {
    "avatar_url": "/uploads/avatars/1703012345678.jpg"
  }
}
```

### Expected Response (Error - 400/500)

```json
{
  "status": "error",
  "message": "File quá lớn (max 5MB)"
}
```

---

## 📋 Step-by-Step Integration

1. **Copy the endpoint code** from section above
2. **Paste into UserController.java** after existing methods
3. **Add imports** at the top of the file
4. **Create WebConfig.java** for static file serving
5. **Update application.properties/yml** with multipart config
6. **Create uploads/avatars/ directory** (or let Spring create it)
7. **Rebuild backend** (`mvn clean install` or `gradle build`)
8. **Test with cURL or Postman** first
9. **Test with Android app** - should work now!

---

## ✅ Verification Checklist

After implementing:

- [ ] Endpoint code added to UserController
- [ ] All imports added
- [ ] WebConfig.java created
- [ ] application.properties/yml updated
- [ ] Backend builds without errors
- [ ] Backend starts without errors
- [ ] Can test with cURL successfully
- [ ] Can test with Postman successfully
- [ ] Android app can upload image successfully
- [ ] Avatar URL saved in database
- [ ] Image file exists in uploads/avatars/
- [ ] Image URL accessible via browser (`http://localhost:8080/uploads/avatars/...`)

---

## 🐛 Troubleshooting

### Error: "File not found in uploads directory"
**Solution**: Create `uploads/` folder in project root, or let Spring create it automatically

### Error: "Access denied writing to uploads"
**Solution**: Check folder permissions, should be writable by app

### Error: "Token validation failed"
**Solution**: Make sure Bearer token is included in Authorization header

### Error: "User not found with ID"
**Solution**: Check that ID parameter matches actual user ID in database

### Error: "Endpoint not found" (404)
**Solution**: Make sure endpoint is added to UserController class and backend rebuilt

### Error: "File size exceeds max"
**Solution**: Increase `max-file-size` in application.properties

### Images not showing (404)
**Solution**: Make sure WebConfig static handler is configured correctly

---

## 💡 Tips

1. **Delete old avatars**: Implement cleanup logic to delete old files when new one uploaded
2. **Add file naming**: Current implementation uses timestamp - consider adding user ID for uniqueness
3. **Image compression**: Consider compressing image before saving to save storage
4. **Caching**: Add cache headers to `/uploads/**` for better performance
5. **CDN**: In production, upload to S3/CDN instead of local filesystem

---

## 📞 Need Help?

Check:
1. Backend logs for errors
2. Database user table - check avatar_url column exists
3. uploads/ directory has write permissions
4. Token is valid (not expired)
5. Spring Security config allows the endpoint

---

**Status**: Ready to integrate! 🚀


