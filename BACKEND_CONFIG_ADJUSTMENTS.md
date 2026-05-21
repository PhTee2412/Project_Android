# 🔧 Backend Config - Điều Chỉnh Chi Tiết

## 📋 Tóm tắt các thay đổi cần làm

Bạn có **2 file config** nhưng cần **3 điều chỉnh chính**:

---

## 1️⃣ SecurityConfig.java - ĐIỀU CHỈNH

### ❌ Vấn đề hiện tại:
```java
.requestMatchers(
    "/api/auth/**",
    "/api/category/**",
    "/api/category-child/**",
    "/api/book/**",
    "/api/bookchild/**",
    "/api/cart/**",
    "/api/**",
    "/uploads/avatars/**"  // ❌ Quá cụ thể, chỉ public
).permitAll()
```

**Problem**:
- `/api/**` include toàn bộ endpoints nên `/api/user/upload-avatar` bị public (không kiểm tra auth)
- `/api/user/**` endpoints khi đó không bao giờ được execute

### ✅ FIX:

**File**: `SecurityConfig.java`

**Thay toàn bộ `authorizeHttpRequests` block này:**

```java
.authorizeHttpRequests(auth -> auth
    // Cho phép preflight CORS
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    
    // ✅ Public endpoints không cần authentication
    .requestMatchers(
        "/api/auth/**",
        "/api/category/**",
        "/api/category-child/**",
        "/api/book/**",
        "/api/bookchild/**",
        "/api/cart/**",
        "/uploads/**"  // ✅ Cho phép truy cập tất cả uploaded files
    ).permitAll()
    
    // ✅ Upload avatar endpoint - CẦN authentication
    .requestMatchers(HttpMethod.POST, "/api/user/upload-avatar").authenticated()
    
    // ✅ Endpoint quản lý admin
    .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
    
    // ✅ Endpoint người dùng đã login
    .requestMatchers("/api/user/**").hasAuthority("USER")
    
    // Các request khác yêu cầu xác thực
    .anyRequest().authenticated()
)
```

**Key changes:**
- ❌ Xóa `/api/**` (vì nó bao quát toàn bộ, gây conflict)
- ✅ `/api/user/upload-avatar` cần `.authenticated()` (phải có JWT token)
- ✅ `/uploads/**` public (ai cũng xem được ảnh)

---

## 2️⃣ WebConfig.java - THÊM RESOURCE HANDLER

### ❌ Vấn đề hiện tại:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Chỉ có CORS config
        registry.addMapping("/**")...
    }
    // ❌ THIẾU: Không serve uploaded files!
}
```

**Problem**: `/uploads/avatars/image.jpg` không serve được → 404

### ✅ FIX:

**File**: `WebConfig.java`

**Thêm method này vào class:**

```java
/**
 * ✅ Serve static files từ folder uploads/
 * Điều này cho phép truy cập: http://localhost:8080/uploads/avatars/image.jpg
 */
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Serve uploads folder as static resources
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
            .setCachePeriod(3600); // Cache 1 giờ

    // Cũng có thể serve từ classpath nếu cần
    registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600);
}
```

**Also update addCorsMappings:**
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://is-203-quan-ly-thu-vien-2e4f5aq1r.vercel.app")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")  // ✅ Thêm OPTIONS
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);  // ✅ Thêm cache duration
}
```

---

## 3️⃣ application.properties - THÊM MULTIPART CONFIG

### ❌ Vấn đề hiện tại:
```
Nếu application.properties không có multipart config → 
- Upload file > 1MB sẽ fail
- Max request size mặc định quá nhỏ
```

### ✅ FIX:

**File**: `application.properties` hoặc `application.yml`

**Thêm những dòng này:**

```properties
# ===== MULTIPART FILE UPLOAD CONFIGURATION =====
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2KB

# ===== UPLOAD DIRECTORY =====
upload.dir=uploads/
upload.avatars.dir=uploads/avatars/
```

**Hoặc nếu dùng YAML:**

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
      enabled: true
      file-size-threshold: 2KB

upload:
  dir: uploads/
  avatars:
    dir: uploads/avatars/
```

---

## 📊 Quy Trình Authorize Upload Avatar

```
1. Android gửi request:
   POST /api/user/upload-avatar
   Authorization: Bearer {JWT_TOKEN}
   Form-data: id + file

2. SecurityFilterChain check:
   .requestMatchers(HttpMethod.POST, "/api/user/upload-avatar").authenticated()
   ✅ JWT token hợp lệ → qua
   ❌ JWT token sai/hết hạn → 401

3. UserController.uploadAvatar() xử lý:
   ✅ Validate file
   ✅ Check authorization (user chỉ upload cho chính mình)
   ✅ Upload Cloudinary
   ✅ Save DB
   ✅ Return response

4. WebConfig serve file:
   GET http://localhost:8080/uploads/avatars/image.jpg
   → ResourceHandler return file từ disk
   ✅ 200 OK
```

---

## 🎯 CHECKLIST - Áp dụng thay đổi

- [ ] **SecurityConfig.java**: Cập nhật `authorizeHttpRequests` block
- [ ] **WebConfig.java**: Thêm `addResourceHandlers()` method
- [ ] **application.properties**: Thêm multipart + upload directory config
- [ ] **Restart Spring Boot** ứng dụng
- [ ] **Test upload avatar** bằng Android/Postman

---

## 🧪 Test Upload Avatar

### Postman
```
Method: POST
URL: http://localhost:8080/api/user/upload-avatar
Headers:
  - Authorization: Bearer {YOUR_JWT_TOKEN}
  - Content-Type: multipart/form-data

Body (Form-data):
  - id: 1 (text)
  - file: [chọn file ảnh]
```

### Expected Response
```json
{
  "status": "success",
  "message": "Upload ảnh đại diện thành công",
  "data": {
    "avatar_url": "https://res.cloudinary.com/.../image.jpg"
  }
}
```

---

## 🔗 Full Files (Complete Code)

Xem các file:
- `SecurityConfig_FIXED.java` - Config security hoàn chỉnh
- `WebConfig_FIXED.java` - WebConfig hoàn chỉnh
- `application_properties_multipart_config.properties` - Multipart config

