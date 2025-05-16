package com.bookstore.DTO;

import com.bookstore.Entity.RefundAttempt;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin_Res_Refund {
    private Long id;
    private String orderId;
    private BigDecimal amount;
    private String createdBy;
    private String refundStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime attemptTime;
    private String errorMessage;

    public void convert(RefundAttempt refund) {
        id = refund.getId();
        orderId = refund.getOrderId();
        amount = BigDecimal.valueOf(refund.getAmount()).divide(BigDecimal.valueOf(100L));
        createdBy = refund.getCreatedBy();
        refundStatus = refund.getStatus().name();
        attemptTime = refund.getAttemptTime();
        errorMessage = refund.getErrorMessage();
    }
}
