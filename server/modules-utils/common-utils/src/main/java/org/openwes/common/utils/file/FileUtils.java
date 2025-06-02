package org.openwes.common.utils.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;

public class FileUtils {

    public static String saveFile(MultipartFile file, String fileDir) throws IOException {
        String normalizedDir = fileDir.replace("/", File.separator);
        if (!normalizedDir.endsWith(File.separator)) {
            normalizedDir += File.separator;
        }

        File directory = new File(normalizedDir);

        // Create directories with proper error handling
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }

        // Sanitize filename for both OS types
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String sanitizedFileName = originalFileName
                .replaceAll("[\\\\/:*?\"<>|\0]", "_")  // Remove invalid chars for both OSes
                .replaceAll("\\s+", "_");              // Replace whitespace

        String uniqueFileName = System.currentTimeMillis() + "_" + sanitizedFileName;

        // Create platform-safe file path
        File dest = new File(directory, uniqueFileName);

        // Save file with proper resource handling
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024 * 8];  // 8KB buffer
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        } catch (IOException e) {
            // Clean up if writing failed
            if (dest.exists()) {
                dest.delete();
            }
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }

        return dest.getAbsolutePath();
    }
}
