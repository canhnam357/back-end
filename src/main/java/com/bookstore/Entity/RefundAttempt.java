package com.bookstore.Entity;

import com.bookstore.Constant.RefundStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refund_attempts")
public class RefundAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionNo;

    @Column(nullable = false)
    private String transactionDate;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime attemptTime;

    private String errorMessage;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @PrePersist
    void createdAt() {
        this.attemptTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}
