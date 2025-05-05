package com.bookstore.Entity;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Constant.RefundStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderDetails = new ArrayList<>();

    private String address;

    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date orderAt;

    @Column(precision = 12, scale = 3)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    private Date expireDatePayment;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    private int refundTimesRemain = 3;

    private Date lastCallRefund;

    private Date refundAt;

    private String transactionNo;

    private String transactionDate;

    private String TxnRef;

    @PrePersist
    void createdAt() {
        this.orderAt = new Date();
    }


}
