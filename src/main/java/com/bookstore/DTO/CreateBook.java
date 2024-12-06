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
public class CreateBook {
    String bookName;

    @PositiveOrZero(message = "inStock must be greater than or equal 0")
    int inStock;

    @Positive(message = "Price must be greater than 0")
    @Column(precision = 12, scale = 3)
    BigDecimal price;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    @Positive(message = "Number of page must be greater than 0")
    int numberOfPage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    Date publishedDate;

    @Positive(message = "Weight must be greater than 0")
    int weight;

    String authorId;

    String publisherId;

    String contributorId;

    String bookTypeId;
}
