package com.bookstore.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin_Req_Update_Book {
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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date publishedDate;

    @Positive(message = "Weight must be greater than 0")
    private int weight;

    private String authorId;

    private List<String> categoriesId = new ArrayList<>();

    private String publisherId;

    private String distributorId;

    private String bookTypeId;

    private List<String> remainImages = new ArrayList<>();

    private List<MultipartFile> images = new ArrayList<>();

    private int thumbnailIdx = 0;

    private Boolean newArrival = false;

    private Boolean isDeleted = false;

}
