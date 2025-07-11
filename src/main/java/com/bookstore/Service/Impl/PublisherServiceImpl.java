package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Publisher;
import com.bookstore.Repository.PublisherRepository;
import com.bookstore.Service.PublisherService;
import com.bookstore.Utils.Normalized;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Publisher createPublisher) {
        try {
            log.info("Bắt đầu tạo nhà xuất bản!");
            Publisher publisher = new Publisher();
            publisher.setPublisherName(createPublisher.getPublisherName());
            publisher.setNameNormalized(Normalized.remove(createPublisher.getPublisherName()));
            log.info("Tạo nhà xuất bản thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo Nhà xuất bản mới thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(publisherRepository.save(publisher))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo nhà xuất bản thất bại, lỗi : " + ex.getMessage());
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
            Page<Publisher> publishers = publisherRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách Nhà xuất bản thành công!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách nhà xuất bản thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            Page<Publisher> publishers = publisherRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm Nhà xuất bản thành công!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tìm kiếm nhà xuất bản thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> update(String publisherId, Admin_Req_Update_Publisher publisher) {
        try {
            log.info("Bắt đầu cập nhật nhà xuất bản!");
            if (publisherRepository.findById(publisherId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Nhà xuất bản!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Publisher _publisher = publisherRepository.findById(publisherId).get();
            _publisher.setPublisherName(publisher.getPublisherName());
            _publisher.setNameNormalized(Normalized.remove(publisher.getPublisherName()));
            log.info("Cập nhật nhà xuất bản thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật thông tin Nhà xuất bản thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(publisherRepository.save(_publisher))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật nhà xuất bản thất bại, lỗi : " + ex.getMessage());
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

            List<Publisher> publishers = publisherRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm Nhà xuất bản thành công!")
                    .result(publishers)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách nhà xuất bản thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
