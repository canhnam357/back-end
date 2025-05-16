package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Publisher;
import com.bookstore.Repository.PublisherRepository;
import com.bookstore.Service.PublisherService;
import com.bookstore.Utils.Normalized;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;

    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Publisher createPublisher) {
        try {
            Publisher publisher = new Publisher();
            publisher.setPublisherName(createPublisher.getPublisherName());
            publisher.setNameNormalized(Normalized.remove(createPublisher.getPublisherName()));
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Publisher created successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(publisherRepository.save(publisher))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to create publisher, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Publisher> publishers = publisherRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all publishers successfully!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve all publishers, message = " + ex.getMessage())
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

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Searched publishers successfully!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to search publishers, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String publisherId, Admin_Req_Update_Publisher publisher) {
        try {
            if (publisherRepository.findById(publisherId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Publisher not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Publisher _publisher = publisherRepository.findById(publisherId).get();
            _publisher.setPublisherName(publisher.getPublisherName());
            _publisher.setNameNormalized(Normalized.remove(publisher.getPublisherName()));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Publisher updated successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(publisherRepository.save(_publisher))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to update publisher, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String publisherId) {
        try {
            publisherRepository.deleteById(publisherId);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Publisher deleted successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to delete publisher, message = " + ex.getMessage())
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

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Searched publishers successfully!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to search publishers, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
