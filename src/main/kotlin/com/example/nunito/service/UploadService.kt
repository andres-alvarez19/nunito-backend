package com.example.nunito.service

import com.example.nunito.model.UploadResponse
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UploadService {

    private val logger = LoggerFactory.getLogger(UploadService::class.java)
    private val uploadDir = Paths.get("uploads")

    init {
        try {
            Files.createDirectories(uploadDir)
            logger.info("Upload directory initialized at: ${uploadDir.toAbsolutePath()}")
        } catch (e: Exception) {
            logger.warn("Could not initialize upload folder: ${e.message}")
        }
    }

    fun storeFile(file: MultipartFile): UploadResponse {
        val originalFilename = file.originalFilename ?: "unknown"
        val extension = originalFilename.substringAfterLast('.', "")
        val filename = "${UUID.randomUUID()}.$extension"
        val targetLocation = uploadDir.resolve(filename)

        try {
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            logger.info("File stored successfully: $filename (${file.size} bytes)")
        } catch (e: IOException) {
            logger.error("Failed to store file $filename", e)
            throw RuntimeException("Failed to store file $filename", e)
        }

        // URL relativa que será servida por Spring
        val url = "/uploads/$filename" 
        return UploadResponse(url, filename)
    }
}
