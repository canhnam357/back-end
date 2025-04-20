package com.bookstore.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin_Req_Create_Book {
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

    private String authorId;

    private String publisherId;

    private String distributorId;

    private String bookTypeId;
}
