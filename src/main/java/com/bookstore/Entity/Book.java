package com.bookstore.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    @JoinColumn(name = "authorId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonIgnore
    private Author author;

    @ManyToOne
    @JoinColumn(name = "publisherId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonIgnore
    private Publisher publisher;

    @ManyToOne
    @JoinColumn(name = "distributorId")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonIgnore
    private Distributor distributor;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItem = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "discountId", referencedColumnName = "discountId")
    private Discount discount;

    @ManyToMany(mappedBy = "books")
    @JsonManagedReference
    private List<Category> categories = new ArrayList<>();;

    @ManyToOne
    @JoinColumn(name = "bookTypeId", nullable = false)
    @JsonIgnore
    private BookType bookType;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    private String urlThumbnail;

    @Column(columnDefinition = "boolean default false")
    private Boolean newArrival;

    private String nameNormalized;

    private int soldQuantity = 0;

    public void addImage(Image image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);
        image.setBook(this); // Đảm bảo thiết lập quan hệ hai chiều
    }

    @PrePersist
    void createdAt() {
        this.createdAt = new Date();
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = new Date();
    }

    public String getAuthorName() {
        if (author == null) return null;
        return author.getAuthorName();
    }

    public String getDistributorName() {
        if (distributor == null) return null;
        return distributor.getDistributorName();
    }

    public String getPublisherName() {
        if (publisher == null) return null;
        return publisher.getPublisherName();
    }

    public String getAuthorId() {
        if (author == null) return null;
        return author.getAuthorId();
    }

    public String getDistributorId() {
        if (distributor == null) return null;
        return distributor.getDistributorId();
    }

    public String getPublisherId() {
        if (publisher == null) return null;
        return publisher.getPublisherId();
    }

}
