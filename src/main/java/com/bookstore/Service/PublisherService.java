package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface PublisherService {
    ResponseEntity<GenericResponse> create(Admin_Req_Create_Publisher createPublisher);

    ResponseEntity<GenericResponse> getAll(int page, int size);
}
