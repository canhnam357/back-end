package com.bookstore.Service.Impl;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.CloudinaryService;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public ResponseEntity<GenericResponse> upload(MultipartFile file) {
        try {
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            String url = (String) data.get("url");
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Upload Successfully!")
                            .result(data)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (IOException io) {
            return ResponseEntity.badRequest().body(
                    GenericResponse.builder()
                            .message("Upload failed!")
                            .result(null)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .success(false)
                            .build()
            );
        }
    }
}
