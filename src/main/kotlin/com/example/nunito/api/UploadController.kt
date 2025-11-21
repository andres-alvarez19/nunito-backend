package com.example.nunito.api

import com.example.nunito.model.UploadResponse
import com.example.nunito.service.UploadService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/upload")
class UploadController(
    private val uploadService: UploadService
) {

    @PostMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(@RequestParam("file") file: MultipartFile): UploadResponse {
        return uploadService.storeFile(file)
    }

    @PostMapping("/audio", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAudio(@RequestParam("file") file: MultipartFile): UploadResponse {
        return uploadService.storeFile(file)
    }
}
