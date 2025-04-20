package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin_Res_Get_BooksOfAuthor {
    private String bookId;
    private String bookName;
    private BigDecimal price;
    private int numberOfPage;
    private String publisherName;
    private String contributorName;
    private String bookType;
    private String urlThumbnail;
}
