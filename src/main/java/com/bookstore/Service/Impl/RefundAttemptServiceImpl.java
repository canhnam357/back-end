package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Res_Refund;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Res_Get_OrderStatusHistory;
import com.bookstore.Entity.OrderStatusHistory;
import com.bookstore.Entity.RefundAttempt;
import com.bookstore.Repository.RefundAttemptRepository;
import com.bookstore.Service.RefundAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RefundAttemptServiceImpl implements RefundAttemptService {

    @Autowired
    private RefundAttemptRepository refundAttemptRepository;

    @Override
    public ResponseEntity<GenericResponse> getAll(String orderId, int index, int size) {
        try {
            Page<RefundAttempt> refundAttempts;
            if (!orderId.isEmpty()) {
                refundAttempts = refundAttemptRepository.findByOrderIdOrderByAttemptTimeDesc(orderId, PageRequest.of(index - 1, size));
            }
            else {
                refundAttempts = refundAttemptRepository.findAllByOrderByAttemptTimeDesc(PageRequest.of(index - 1, size));
            }
            List<Admin_Res_Refund> res = new ArrayList<>();
            for (RefundAttempt refundAttempt : refundAttempts) {
                Admin_Res_Refund temp = new Admin_Res_Refund();
                temp.convert(refundAttempt);
                res.add(temp);
            }
            Page<Admin_Res_Refund> dtoPage = new PageImpl<>(res, refundAttempts.getPageable(), refundAttempts.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all refund status history successfully!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(false)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve refund status history, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
