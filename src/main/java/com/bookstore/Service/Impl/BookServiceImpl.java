package com.bookstore.Service.Impl;

import com.bookstore.DTO.*;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Service.BookService;
import com.bookstore.Specification.BookSpecification;
import com.bookstore.Utils.Normalized;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private DistributorRepository distributorRepository;

    @Autowired
    private BookTypeRepository bookTypeRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ReviewRepository reviewRepository;


    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Book> books = bookRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Book Successfully!")
                    .result(books)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Book failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size, BigDecimal leftBound, BigDecimal rightBound, String authorId, String publisherId, String distributorId, String bookName, String sort) {
        try {
            String pattern = "";
            for (char c : bookName.toCharArray()) {
                pattern += "%" + c + "%";
            }
            if (pattern.isEmpty()) {
                pattern = "%%";
            }

            List<String> authors = Arrays.asList(authorId.split(",", -1));
            List<String> publishers = Arrays.asList(publisherId.split(",", -1));
            List<String> distributors = Arrays.asList(distributorId.split(",", -1));

            System.err.println("LIST AUTHOR_ID : ");
            for (String s : authors) System.err.println(s + " ");
            System.err.println("LIST PUBLISHER_ID : ");
            for (String s : publishers) System.err.println(s + " ");
            System.err.println("LIST DISTRIBUTOR_ID : ");
            for (String s : distributors) System.err.println(s + " ");
            if (authorId.isEmpty()) {
                authors = new ArrayList<>();
            }
            if (publisherId.isEmpty()) {
                publishers = new ArrayList<>();
            }
            if (distributorId.isEmpty()) {
                distributors = new ArrayList<>();
            }

            Specification<Book> spec = BookSpecification.withFilters(leftBound, rightBound, authors, publishers, distributors, pattern, sort);

            Page<Book> books = bookRepository.findAll(spec, PageRequest.of(page - 1, size));
            List<Res_Get_Books> res = new ArrayList<>();
            for (Book book : books) {
                Res_Get_Books temp = new Res_Get_Books();
                temp.convert(book);
                temp.setRating(reviewRepository.findAverageRatingByBookId(book.getBookId()));
                res.add(temp);
            }

            Page<Res_Get_Books> dtoPage = new PageImpl<>(res, books.getPageable(), books.getTotalElements());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Book Successfully!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Book failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId) {
        try {
            if (bookRepository.findByBookIdAndIsDeletedIsFalse(bookId).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Get Book Failed!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Book book = bookRepository.findByBookIdAndIsDeletedIsFalse(bookId).get();
            Res_Get_Books res = new Res_Get_Books();
            res.convert(book);
            res.setRating(reviewRepository.findAverageRatingByBookId(bookId));
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get Book Successfully!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get Book failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getNewArrivalsBook(int page, int size) {
        try {
            Page<Book> books = bookRepository.findAllByIsDeletedIsFalseAndNewArrivalIsTrue(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get New Arrival Book Successfully!")
                    .result(books)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get New Arrival Book failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Book createBook) {
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
            book.setDistributor(distributorRepository.findById(createBook.getDistributorId()).get());
            book.setBookType(bookTypeRepository.findById(createBook.getBookTypeId()).get());
            book.setNameNormalized(Normalized.removeVietnameseAccents(createBook.getBookName()));
            bookRepository.save(book);
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Book successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Book failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> upload(MultipartFile file, String bookId, int isThumbnail) {
        try {
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            String url = (String) data.get("url");
            Optional<Book> book = bookRepository.findById(bookId);
            Image image = new Image();
            image.setBook(book.get());
            image.setUrl(url);
            book.get().addImage(image);
            if (book.get().getUrlThumbnail() == null || isThumbnail == 1) {
                book.get().setUrlThumbnail(url);
            }
            book.get().setNameNormalized(Normalized.removeVietnameseAccents(book.get().getBookName()));
            bookRepository.save(book.get());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Upload Successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (IOException io) {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .message("Upload failed!")
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> adminGetBooksOfAuthor(int page, int size, String authorId) {
        try {
            System.err.println(authorId);
            Page<Book> books = bookRepository.findAllByAuthorAuthorId(PageRequest.of(page - 1, size), authorId);
            List<Admin_Res_Get_BooksOfAuthor> res = new ArrayList<>();
            for (Book book : books) {
                String thumbnail = null;
                if (book.getUrlThumbnail() != null) {
                    thumbnail = book.getUrlThumbnail();
                }
                res.add(new Admin_Res_Get_BooksOfAuthor(
                        book.getBookId(),
                        book.getBookName(),
                        book.getPrice(),
                        book.getNumberOfPage(),
                        book.getPublisherName(),
                        book.getDistributorName(),
                        book.getBookType().getBookTypeName(),
                        thumbnail
                ));
            }

            Page<Admin_Res_Get_BooksOfAuthor> dtoPage = new PageImpl<>(res, books.getPageable(), books.getTotalElements());
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get Books of author successfully!")
                            .result(dtoPage)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get Books of author failed!")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getPriceRange() {
        try {
            return ResponseEntity.status(200).body(
                    GenericResponse.builder()
                            .message("Get Price range success!")
                            .result(bookRepository.findAllDistinctPricesOrderByAsc())
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get Price range failed!")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String bookId) {
        try {
            Optional<Book> book = bookRepository.findById(bookId);
            if (book.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found book!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (book.get().getIsDeleted() == true) {
                return ResponseEntity.status(200).body(GenericResponse.builder()
                        .message("Book already deleted.!")
                        .statusCode(HttpStatus.OK.value())
                        .success(false)
                        .build());
            }
            book.get().setIsDeleted(true);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Delete Book success.!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete book failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String bookId, Admin_Req_Update_Book bookDto) {
        try {
            Optional<Book> book = bookRepository.findById(bookId);
            if (book.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found book!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build()
                );
            }
            Book ele = book.get();
            ele.setBookName(bookDto.getBookName());
            ele.setInStock(bookDto.getInStock());
            ele.setPrice(bookDto.getPrice());
            ele.setDescription(bookDto.getDescription());
            ele.setNumberOfPage(bookDto.getNumberOfPage());
            ele.setPublishedDate(bookDto.getPublishedDate());
            ele.setWeight(bookDto.getWeight());
            ele.setAuthor(authorRepository.findById(bookDto.getAuthorId()).get());
            ele.setPublisher(publisherRepository.findById(bookDto.getPublisherId()).get());
            ele.setDistributor(distributorRepository.findById(bookDto.getDistributorId()).get());
            ele.setBookType(bookTypeRepository.findById(bookDto.getBookTypeId()).get());
            ele.setUrlThumbnail(bookDto.getUrlThumbnail());
            List<Image> images = new ArrayList<>();
            for (Image image : ele.getImages()) {
                if (bookDto.getImages().contains(image.getImageId())) {
                    images.add(image);
                }
            }
            ele.setImages(images);
            bookRepository.save(ele);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Update book successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update book failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
