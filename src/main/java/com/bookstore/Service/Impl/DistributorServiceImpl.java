package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.Admin_Req_Update_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Distributor;
import com.bookstore.Repository.DistributorRepository;
import com.bookstore.Service.DistributorService;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributorServiceImpl implements DistributorService {
    private final DistributorRepository distributorRepository;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Distributor createDistributor) {
        try {
            log.info("Bắt đầu tạo nhà phát hành!");
            Distributor distributor = new Distributor();
            distributor.setDistributorName(createDistributor.getDistributorName());
            distributor.setNameNormalized(Normalized.remove(createDistributor.getDistributorName()));
            log.info("Tạo nhà phát hành thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo Nhà phát hành thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(distributorRepository.save(distributor))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo nhà phát hành thất bại, lỗi : " + ex.getMessage());
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
            Page<Distributor> contributors = distributorRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách Nhà phát hành thành công!")
                    .result(contributors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách nhà phát hành thất bại, lỗi : " + ex.getMessage());
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

            Page<Distributor> distributors = distributorRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm Nhà phát hành thành công!")
                    .result(distributors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tìm kiếm nhà phát hành thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> update(String distributorId, Admin_Req_Update_Distributor distributor) {
        try {
            log.info("Bắt đầu cập nhật thông tin nhà phát hành!");
            if (distributorRepository.findById(distributorId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Nhà phát hành!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Distributor _distributor = distributorRepository.findById(distributorId).get();
            _distributor.setDistributorName(distributor.getDistributorName());
            _distributor.setNameNormalized(Normalized.remove(distributor.getDistributorName()));
            log.info("Cập nhật thông tin nhà phát hành thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật thông tin Nhà phát hành thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(distributorRepository.save(_distributor))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật thông tin nhà phát hành thất bại, lỗi : " + ex.getMessage());
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

            List<Distributor> distributors = distributorRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm Nhà phát hành thành công!")
                    .result(distributors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách nhà phát hành thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
