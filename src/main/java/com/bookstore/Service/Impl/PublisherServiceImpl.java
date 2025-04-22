package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Author;
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
            publisher.setNameNormalized(Normalized.removeVietnameseAccents(createPublisher.getPublisherName()));
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Publisher successfully!")
                            .result(publisherRepository.save(publisher))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Publisher failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Publisher> publishers = publisherRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Publisher Successfully!")
                            .result(publishers)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Publisher failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String s = Normalized.removeVietnameseAccents(keyword);
            String search_word = "";
            for (char c : s.toCharArray()) {
                search_word += "%" + c + "%";
            }

            if (search_word.length() == 0) {
                search_word = "%%";
            }

            System.err.println(search_word);

            Page<Publisher> publishers = publisherRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Publisher Successfully!")
                            .result(publishers)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Publisher failed!")
                            .result("")
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String publisherId, Admin_Req_Update_Publisher publisher) {
        try {
            Publisher _publisher = publisherRepository.findById(publisherId).get();
            _publisher.setPublisherName(publisher.getPublisherName());
            _publisher.setNameNormalized(Normalized.removeVietnameseAccents(publisher.getPublisherName()));
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Update Publisher successfully!")
                            .result(publisherRepository.save(_publisher))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Update Publisher failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String publisherId) {
        try {
            publisherRepository.deleteById(publisherId);
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Delete Publisher successfully!")
                            .result(null)
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete Publisher failed!!!")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String s = Normalized.removeVietnameseAccents(keyword);
            String search_word = "";
            for (char c : s.toCharArray()) {
                search_word += "%" + c + "%";
            }

            if (search_word.length() == 0) {
                search_word = "%%";
            }

            System.err.println(search_word);

            List<Publisher> publishers = publisherRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Publisher Successfully!")
                            .result(publishers)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Search Publisher failed!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
