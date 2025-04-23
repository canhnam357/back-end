package com.bookstore.DTO;

import com.bookstore.Entity.Book;
import com.bookstore.Entity.Image;
import com.bookstore.Entity.Review;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Date publishedDate;
    private int weight;
    private String authorName;
    private String publisherName;
    private String distributorName;
    private String bookType;
    private String urlThumbnail;
    private List<String> images;
    private BigDecimal rating;

    public void convert(Book book) {
        bookId = book.getBookId();
        bookName = book.getBookName();
        inStock = book.getInStock();
        price = book.getPrice();
        description = book.getDescription();
        numberOfPage = book.getNumberOfPage();
        publishedDate = book.getPublishedDate();
        weight = book.getWeight();

        authorName = null;
        if (book.getAuthor() != null) {
            authorName = book.getAuthor().getAuthorName();
        }

        publisherName = null;
        if (book.getPublisher() != null) {
            publisherName = book.getPublisher().getPublisherName();
        }

        distributorName = null;
        if (book.getDistributor() != null) {
            distributorName = book.getDistributor().getDistributorName();
        }

        bookType = null;
        if (book.getBookType() != null) {
            bookType = book.getBookType().getBookTypeName();
        }

        urlThumbnail = null;
        if (book.getUrlThumbnail() != null) {
            urlThumbnail = book.getUrlThumbnail();
        }

        images = new ArrayList<>();
        for (Image image : book.getImages()) {
            images.add(image.getUrl());
        }
    }
}

