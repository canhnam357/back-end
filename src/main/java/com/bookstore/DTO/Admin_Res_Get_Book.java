package com.bookstore.DTO;

import com.bookstore.Entity.Book;
import com.bookstore.Entity.Category;
import com.bookstore.Entity.Image;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin_Res_Get_Book {
    private String bookId;
    private String bookName;
    private int inStock;
    private BigDecimal price;
    private String description;
    private int numberOfPage;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate publishedDate;
    private int weight;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime updatedAt;
    private Boolean isDeleted;
    private Admin_Res_Get_Author author;
    private Admin_Res_Get_Publisher publisher;
    private Admin_Res_Get_Distributor distributor;
    private List<Admin_Res_Get_Category> categories = new ArrayList<>();
    private Admin_Res_Get_BookType bookType;
    private List<Admin_Res_Get_Image> images = new ArrayList<>();
    private String urlThumbnail;
    private Boolean newArrival;
    private int soldQuantity;

    public void convert(Book book) {
        this.soldQuantity = book.getSoldQuantity();
        this.bookId = book.getBookId();
        this.bookName = book.getBookName();
        this.inStock = book.getInStock();
        this.price = book.getPrice();
        this.description = book.getDescription();
        this.numberOfPage = book.getNumberOfPage();
        this.publishedDate = book.getPublishedDate();
        this.weight = book.getWeight();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
        this.isDeleted = book.getIsDeleted();
        this.author = new Admin_Res_Get_Author(book.getAuthorId(), book.getAuthorName());
        this.publisher = new Admin_Res_Get_Publisher(book.getPublisherId(), book.getPublisherName());
        this.distributor = new Admin_Res_Get_Distributor(book.getDistributorId(), book.getDistributorName());
        for (Category category : book.getCategories()) {
            this.categories.add(new Admin_Res_Get_Category(category.getCategoryId(), category.getCategoryName()));
        }
        this.bookType = new Admin_Res_Get_BookType(book.getBookType().getBookTypeId(), book.getBookType().getBookTypeName());
        for (Image image : book.getImages()) {
            this.images.add(new Admin_Res_Get_Image(image.getImageId(), image.getUrl()));
        }
        this.urlThumbnail = book.getUrlThumbnail();
        this.newArrival = book.getNewArrival();
    }
}
