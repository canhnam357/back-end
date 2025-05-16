package com.bookstore.DTO;

import com.bookstore.Entity.Review;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime createdAt;

    public void convert(Review review) {
        reviewId = review.getReviewId();
        userId = review.getUser().getUserId();
        userReviewed = review.getUser().getFullName();
        content = review.getContent();
        rating = review.getRating();
        createdAt = review.getCreatedAt();
    }

}
