package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoStorageService {

    // Define where photos will be saved on your server filesystem
    private final Path rootLocation = Paths.get("uploads/photos");

    // Max file size: 2MB in bytes
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    // Allowed image content types
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png");

    public PhotoStorageService() {
        try {
            // Automatically create the directory structure on your server if it doesn't exist
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory structure!", e);
        }
    }

    public String storePhoto(MultipartFile file) {
        // 1. Validation: Check if empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty profile photo file.");
        }

        // 2. Validation: Check file size (Max 2MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed threshold of 2MB.");
        }

        // 3. Validation: Check image format type (JPEG/PNG)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file format type! Only JPEG and PNG files are accepted.");
        }

        try {
            // 4. Generate unique name to prevent profile images from overwriting each other
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFileName))
                    .normalize().toAbsolutePath();

            // 5. Save the file stream securely onto the disk
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Return only the unique relative name to store inside your Profile table
            return uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file on disk layer due to an internal system I/O failure.", e);
        }
    }

    /**
     * Loads the raw bytes of a stored photo by filename.
     * Used by CardRenderService to embed photos as Base64 in the card template.
     *
     * @param fileName the stored filename (as returned by storePhoto)
     * @return raw file bytes
     * @throws IOException if the file cannot be read
     */
    public byte[] loadPhotoBytes(String fileName) throws IOException {
        Path filePath = rootLocation.resolve(fileName).normalize();
        return Files.readAllBytes(filePath);
    }

    /**
     * Deletes a stored photo file by filename. Silent no-op if the file does not exist.
     *
     * @param fileName the stored filename to remove
     */
    public void deletePhoto(String fileName) {
        try {
            Path filePath = rootLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log and continue — deletion failure is non-critical
            System.err.println("Warning: could not delete photo file: " + fileName);
        }
    }
}