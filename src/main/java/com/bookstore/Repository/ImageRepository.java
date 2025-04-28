package com.bookstore.Repository;

import com.bookstore.Entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ImageRepository extends JpaRepository<Image, String> {
    List<Image> findByUrlIn(List<String> urls);
    void deleteByImageIdIn(List<String> imageIds);

    List<Image> findByBookBookId(String bookId);
}
