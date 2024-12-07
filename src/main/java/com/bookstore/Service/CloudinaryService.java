package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    ResponseEntity<GenericResponse> upload(MultipartFile file);
}
