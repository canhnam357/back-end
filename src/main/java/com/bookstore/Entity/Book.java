package com.bookstore.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bookId;

    private String bookName;

    @PositiveOrZero(message = "inStock must be greater than or equal 0")
    private int inStock;

    @Positive(message = "Price must be greater than 0")
    @Column(precision = 12, scale = 3)
    private BigDecimal price;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Positive(message = "Number of page must be greater than 0")
    private int numberOfPage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date publishedDate;

    @Positive(message = "Weight must be greater than 0")
    private int weight;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updatedAt;

    @Column(columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "authorId", nullable = false)
    private Author author;

    @ManyToOne
    @JoinColumn(name = "publisherId", nullable = false)
    private Publisher publisher;

    @ManyToOne
    @JoinColumn(name = "contributorId", nullable = false)
    private Contributor contributor;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItem;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Voucher> vouchers;

    @ManyToMany(mappedBy = "books")
    @JsonIgnore
    private List<Category> categories;

    @ManyToOne
    @JoinColumn(name = "bookTypeId", nullable = false)
    private BookType bookType;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    private String urlThumbnail;

    public void addImage(Image image) {
        images.add(image);
    }

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = new Date();
    }


}
