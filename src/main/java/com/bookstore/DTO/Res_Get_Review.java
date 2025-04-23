package com.bookstore.DTO;

import com.bookstore.Entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Res_Get_Review {
    private String reviewId;
    private String userId;
    private String userReviewed;
    private String content;
    private int rating;
    // format : dd-MM-yyyy HH:mm:ss
    private Date createdAt;

}
