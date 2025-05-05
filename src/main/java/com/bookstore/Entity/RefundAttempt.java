package com.bookstore.Entity;

import com.bookstore.Constant.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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
    private String orderId;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Date attemptTime;

    private String errorMessage;

    @ManyToOne
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Orders order;

    @PrePersist
    void createdAt() {
        this.attemptTime = new Date();
    }
}
