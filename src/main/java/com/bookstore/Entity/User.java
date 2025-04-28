package com.bookstore.Entity;


import com.bookstore.Constant.Gender;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @JsonBackReference
    private String password;

    private String phoneNumber;

    @Email
    private String email;

    @Nationalized
    private String fullName;

    private String avatar;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.OTHER;

    @Column(columnDefinition = "boolean default true")
    private boolean isActive;

    private Date createdAt;

    private Date updatedAt;

    private Date lastLoginAt;

    @Column(nullable = true, columnDefinition = "boolean default false")
    private boolean isVerified;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    @ToString.Exclude
    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cartId", referencedColumnName = "cartId")
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Address> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Orders> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "changedBy")
    @JsonIgnore
    private List<OrderStatusHistory> changedOrders;

    @PrePersist
    void createdAt() { this.createdAt = new Date();}

    @PreUpdate
    void updatedAt() {
        this.updatedAt = new Date();
    }

}
