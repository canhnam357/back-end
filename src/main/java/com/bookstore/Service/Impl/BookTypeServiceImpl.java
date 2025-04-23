package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_BookType;
import com.bookstore.DTO.Admin_Req_Update_BookType;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.BookType;
import com.bookstore.Repository.BookTypeRepository;
import com.bookstore.Service.BookTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookTypeServiceImpl implements BookTypeService {
    @Autowired
    private BookTypeRepository bookTypeRepository;

    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_BookType createBookType) {
        try {
            BookType bookType = new BookType();
            bookType.setBookTypeName(createBookType.getBookTypeName());
            bookTypeRepository.save(bookType);
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create BookType successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create BookType failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<BookType> bookTypes = bookTypeRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All BookType Successfully!")
                    .result(bookTypes)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All BookType failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String bookTypeId) {
        try {
            Optional<BookType> bookType = bookTypeRepository.findById(bookTypeId);
            if (bookType.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found BookType!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            bookTypeRepository.deleteById(bookTypeId);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Delete BookType successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete BookType failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String bookTypeId, Admin_Req_Update_BookType bookTypeDto) {
        try {
            Optional<BookType> bookType = bookTypeRepository.findById(bookTypeId);
            if (bookType.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found BookType!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            bookType.get().setBookTypeName(bookTypeDto.getBookTypeName());
            bookTypeRepository.save(bookType.get());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Update BookType successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update BookType failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
