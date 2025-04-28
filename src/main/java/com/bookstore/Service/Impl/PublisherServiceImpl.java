package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Publisher;
import com.bookstore.Repository.PublisherRepository;
import com.bookstore.Service.PublisherService;
import com.bookstore.Utils.Normalized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublisherServiceImpl implements PublisherService {
    @Autowired
    private PublisherRepository publisherRepository;

    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Publisher createPublisher) {
        try {
            Publisher publisher = new Publisher();
            publisher.setPublisherName(createPublisher.getPublisherName());
            publisher.setNameNormalized(Normalized.remove(createPublisher.getPublisherName()));
            publisherRepository.save(publisher);
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Publisher successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Publisher failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Publisher> publishers = publisherRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Publisher Successfully!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Publisher failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            Page<Publisher> publishers = publisherRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Search Publisher Successfully!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Search Publisher failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String publisherId, Admin_Req_Update_Publisher publisher) {
        try {
            Publisher _publisher = publisherRepository.findById(publisherId).get();
            _publisher.setPublisherName(publisher.getPublisherName());
            _publisher.setNameNormalized(Normalized.remove(publisher.getPublisherName()));
            publisherRepository.save(_publisher);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Update Publisher successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update Publisher failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String publisherId) {
        try {
            publisherRepository.deleteById(publisherId);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Delete Publisher successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete Publisher failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            List<Publisher> publishers = publisherRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Search Publisher Successfully!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Search Publisher failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
