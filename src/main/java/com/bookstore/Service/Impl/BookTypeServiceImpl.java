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

import java.util.List;
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
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Book type created successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                     .result(bookTypeRepository.save(bookType))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to create book type, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            List<BookType> bookTypes = bookTypeRepository.findAll();
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all book types successfully!")
                    .result(bookTypes)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve all book types, message = " + ex.getMessage())
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book type not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            bookTypeRepository.deleteById(bookTypeId);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Book type deleted successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to delete book type, message = " + ex.getMessage())
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book type not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            bookType.get().setBookTypeName(bookTypeDto.getBookTypeName());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Book type updated successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(bookTypeRepository.save(bookType.get()))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to update book type, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
