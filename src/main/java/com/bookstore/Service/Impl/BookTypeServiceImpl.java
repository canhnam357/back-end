package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_BookType;
import com.bookstore.DTO.Admin_Req_Update_BookType;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.BookType;
import com.bookstore.Repository.BookTypeRepository;
import com.bookstore.Service.BookTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookTypeServiceImpl implements BookTypeService {
    private final BookTypeRepository bookTypeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_BookType createBookType) {
        try {
            log.info("Bắt đầu tạo loại sách!");
            BookType bookType = new BookType();
            bookType.setBookTypeName(createBookType.getBookTypeName());
            log.info("Tạo loại sách thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo Loại sách mới thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                     .result(bookTypeRepository.save(bookType))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo loại sách thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            List<BookType> bookTypes = bookTypeRepository.findAll();
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách Loại sách thành công!")
                    .result(bookTypes)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách loại sách thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> update(String bookTypeId, Admin_Req_Update_BookType bookTypeDto) {
        try {
            log.info("Bắt đầu cập nhật loại sách!");
            Optional<BookType> bookType = bookTypeRepository.findById(bookTypeId);
            if (bookType.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Loại sách không tìm thấy!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            bookType.get().setBookTypeName(bookTypeDto.getBookTypeName());
            log.info("Cập nhật loại sách thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật thông tin Loại sách thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(bookTypeRepository.save(bookType.get()))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật loại sách thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
