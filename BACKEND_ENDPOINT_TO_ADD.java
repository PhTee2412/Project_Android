// ===== THÊM METHOD NÀY VÀO UserController.java =====
// Đặt sau @PostMapping("/verify-email-update") method

@PostMapping("/upload-avatar")
public Map<String, Object> uploadAvatar(
        @RequestParam("id") String id,
        @RequestParam("file") MultipartFile file) {

    Map<String, Object> response = new HashMap<>();

    try {
        // ===== VALIDATE FILE =====
        if (file.isEmpty()) {
            response.put("status", "error");
            response.put("message", "File không được để trống");
            return response;
        }

        // ===== CHECK FILE SIZE (MAX 5MB) =====
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            response.put("status", "error");
            response.put("message", "File quá lớn (max 5MB)");
            return response;
        }

        // ===== CHECK FILE TYPE =====
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("status", "error");
            response.put("message", "Chỉ chấp nhận file ảnh");
            return response;
        }

        // ===== GET USER =====
        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với ID: " + id));

        // ===== CHECK AUTHORIZATION =====
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        String currentRole = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();

        if (currentRole.equals("USER") && !currentUsername.equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Bạn không có quyền upload ảnh cho người dùng khác");
        }

        // ===== SAVE FILE =====
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = System.currentTimeMillis() + extension;

        // Create upload directory
        Path uploadDir = Paths.get("uploads/avatars");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Save file
        Path filepath = uploadDir.resolve(filename);
        Files.write(filepath, file.getBytes());

        // ===== UPDATE USER IN DATABASE =====
        String avatarUrl = "/uploads/avatars/" + filename;
        user.setAvatar_url(avatarUrl);
        userRepository.save(user);

        // ===== RETURN SUCCESS =====
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
        response.put("message", "Lỗi upload: " + e.getMessage());
        return response;
    }
}

