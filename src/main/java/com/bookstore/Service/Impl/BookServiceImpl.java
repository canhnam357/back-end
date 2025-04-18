package com.bookstore.Service.Impl;

import com.bookstore.DTO.CreateBook;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Author;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.Image;
import com.bookstore.Repository.*;
import com.bookstore.Service.BookService;
import com.bookstore.Service.CloudinaryService;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private ContributorRepository contributorRepository;

    @Autowired
    private BookTypeRepository bookTypeRepository;

    @Autowired
    private Cloudinary cloudinary;


    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Book> books = bookRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Book Successfully!")
                            .result(books)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Book failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size) {
        try {
            Page<Book> books = bookRepository.findAllByIsDeletedIsFalse(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Book Successfully!")
                            .result(books)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Book failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId) {
        try {
            Optional<Book> book = bookRepository.findByBookIdAndIsDeletedIsFalse(bookId);
            if (!book.isPresent()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Get Book Failed!!!")
                                .result("")
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get Book Successfully!")
                            .result(book.get())
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Book failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getNewArrivalsBook(int page, int size) {
        try {
            Page<Book> books = bookRepository.findAllByIsDeletedIsFalseAndNewArrivalIsTrue(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get New Arrival Book Successfully!")
                            .result(books)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get New Arrival Book failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> create(CreateBook createBook) {
        try {
            Book book = new Book();
            book.setBookName(createBook.getBookName());
            book.setInStock(createBook.getInStock());
            book.setPrice(createBook.getPrice());
            book.setDescription(createBook.getDescription());
            book.setNumberOfPage(createBook.getNumberOfPage());
            book.setPublishedDate(createBook.getPublishedDate());
            book.setWeight(createBook.getWeight());
            book.setAuthor(authorRepository.findById(createBook.getAuthorId()).get());
            book.setPublisher(publisherRepository.findById(createBook.getPublisherId()).get());
            book.setContributor(contributorRepository.findById(createBook.getContributorId()).get());
            book.setBookType(bookTypeRepository.findById(createBook.getBookTypeId()).get());
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Book successfully!")
                            .result(bookRepository.save(book))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Book failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> upload(MultipartFile file, String bookId) {
        try {
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            String url = (String) data.get("url");
            Optional<Book> book = bookRepository.findById(bookId);
            Image image = new Image();
            image.setBook(book.get());
            image.setUrl(url);
            book.get().addImage(image);
            if (book.get().getUrlThumbnail() == null) {
                book.get().setUrlThumbnail(url);
            }
            bookRepository.save(book.get());
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Upload Successfully!")
                            .result(data)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (IOException io) {
            return ResponseEntity.badRequest().body(
                    GenericResponse.builder()
                            .message("Upload failed!")
                            .result("")
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .success(false)
                            .build()
            );
        }
    }
}
