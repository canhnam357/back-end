package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface PublisherService {
    ResponseEntity<GenericResponse> create(Admin_Req_Create_Publisher createPublisher);

    ResponseEntity<GenericResponse> getAll(int page, int size);
    ResponseEntity<GenericResponse> search(int page, int size, String keyword);
    ResponseEntity<GenericResponse> update(String publisherId, Admin_Req_Update_Publisher publisher);

    ResponseEntity<GenericResponse> delete(String publisherId);

    ResponseEntity<GenericResponse> getAllNotPageable(String keyword);
}
