package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.Admin_Req_Update_Category;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Category;
import com.bookstore.Repository.CategoryRepository;
import com.bookstore.Service.CategoryService;
import com.bookstore.Utils.Normalized;
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
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Category createCategory) {
        try {
            log.info("Bắt đầu tạo thể loại!");
            Category category = new Category();
            category.setCategoryName(createCategory.getCategoryName());
            category.setNameNormalized(Normalized.remove(category.getCategoryName()));
            log.info("Tạo thể loại thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo Thể loại mới thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(categoryRepository.save(category))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo thể loại thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getAll(String keyword) {
        try {
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách Thể loại thành công!")
                    .result(categories)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách thể loại thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> update(String categoryId, Admin_Req_Update_Category category) {
        try {
            log.info("Bắt đầu tạo thể loại!");
            Optional<Category> ele = categoryRepository.findById(categoryId);
            if (ele.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Thể loại!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            ele.get().setCategoryName(category.getCategoryName());
            ele.get().setNameNormalized(Normalized.remove(category.getCategoryName()));
            log.info("Tạo thể loại mới thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật thông tin Thể loại thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(categoryRepository.save(ele.get()))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo thể loại mới thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            List<Category> categories = categoryRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm Thể loại thành công!")
                    .result(categories)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tìm kiếm thể loại thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
