package com.bookstore.DTO;

import com.bookstore.Constant.DiscountType;
import com.bookstore.Entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Res_Get_BookList {
    private String bookId;
    private String bookName;
    private int inStock;
    private BigDecimal price;
    private Admin_Res_Get_Author author;
    private Admin_Res_Get_Publisher publisher;
    private Admin_Res_Get_Distributor distributor;
    private String urlThumbnail;
    private BigDecimal priceAfterSale;
    private Boolean newArrival;

    public void convert(Book book, ZonedDateTime now) {
        newArrival = book.isNewArrival();
        bookId = book.getBookId();
        bookName = book.getBookName();
        inStock = book.getInStock();
        price = book.getPrice();

        author = null;
        if (book.getAuthor() != null) {
            author = new Admin_Res_Get_Author(book.getAuthor().getAuthorId(), book.getAuthor().getAuthorName());
        }

        publisher = null;
        if (book.getPublisher() != null) {
            publisher = new Admin_Res_Get_Publisher(book.getPublisher().getPublisherId(), book.getPublisher().getPublisherName());
        }

        distributor = null;
        if (book.getDistributor() != null) {
            distributor = new Admin_Res_Get_Distributor(book.getDistributor().getDistributorId(), book.getDistributor().getDistributorName());
        }

        urlThumbnail = null;
        if (book.getUrlThumbnail() != null) {
            urlThumbnail = book.getUrlThumbnail();
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
