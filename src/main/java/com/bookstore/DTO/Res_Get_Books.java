package com.bookstore.DTO;

import com.bookstore.Constant.DiscountType;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.Category;
import com.bookstore.Entity.Image;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Res_Get_Books {
    private String bookId;
    private String bookName;
    private int inStock;
    private BigDecimal price;
    private String description;
    private int numberOfPage;
    // "dd-MM-yyyy HH:mm:ss"
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate publishedDate;
    private int weight;
    private Admin_Res_Get_Author author;
    private List<Admin_Res_Get_Category> categories;
    private Admin_Res_Get_Publisher publisher;
    private Admin_Res_Get_Distributor distributor;
    private Admin_Res_Get_BookType bookType;
    private String urlThumbnail;
    private List<String> images;
    private BigDecimal rating;
    private BigDecimal priceAfterSale;
    private int soldQuantity;
    private Boolean newArrival;

    public void convert(Book book, ZonedDateTime now) {
        newArrival = book.isNewArrival();
        soldQuantity = book.getSoldQuantity();
        bookId = book.getBookId();
        bookName = book.getBookName();
        inStock = book.getInStock();
        price = book.getPrice();
        description = book.getDescription();
        numberOfPage = book.getNumberOfPage();
        publishedDate = book.getPublishedDate();
        weight = book.getWeight();

        author = null;
        if (book.getAuthor() != null) {
            author = new Admin_Res_Get_Author(book.getAuthor().getAuthorId(), book.getAuthor().getAuthorName());
        }

        categories = new ArrayList<>();
        for (Category category : book.getCategories()) {
            categories.add(new Admin_Res_Get_Category(category.getCategoryId(), category.getCategoryName()));
        }

        publisher = null;
        if (book.getPublisher() != null) {
            publisher = new Admin_Res_Get_Publisher(book.getPublisher().getPublisherId(), book.getPublisher().getPublisherName());
        }

        distributor = null;
        if (book.getDistributor() != null) {
            distributor = new Admin_Res_Get_Distributor(book.getDistributor().getDistributorId(), book.getDistributor().getDistributorName());
        }

        bookType = null;
        if (book.getBookType() != null) {
            bookType = new Admin_Res_Get_BookType(book.getBookType().getBookTypeId(), book.getBookType().getBookTypeName());
        }

        urlThumbnail = null;
        if (book.getUrlThumbnail() != null) {
            urlThumbnail = book.getUrlThumbnail();
        }

        images = new ArrayList<>();
        for (Image image : book.getImages()) {
            images.add(image.getUrl());
        }

        if (book.getDiscount() != null && book.getDiscount().getStartDate().isBefore(now) && book.getDiscount().getEndDate().isAfter(now)) {
            if (book.getDiscount().getDiscountType() == DiscountType.FIXED) {
                priceAfterSale = book.getPrice().subtract(book.getDiscount().getDiscount());
            }
            else {
                priceAfterSale = book.getPrice()
                        .multiply(BigDecimal.valueOf(100L).subtract(book.getDiscount().getDiscount()))
                        .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);            }
        }
    }
}

