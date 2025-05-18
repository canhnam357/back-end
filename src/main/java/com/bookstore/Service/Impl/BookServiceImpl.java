package com.bookstore.Service.Impl;

import com.bookstore.DTO.*;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Service.BookService;
import com.bookstore.Specification.BookSpecification;
import com.bookstore.Utils.Normalized;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final DistributorRepository distributorRepository;
    private final BookTypeRepository bookTypeRepository;
    private final Cloudinary cloudinary;
    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;

    public Page<Admin_Res_Get_Book> convertPageToPage(Page<Book> books) {
        List<Admin_Res_Get_Book> res = new ArrayList<>();
        for (Book book : books) {
            Admin_Res_Get_Book ele = new Admin_Res_Get_Book();
            ele.convert(book);
            res.add(ele);
        }
        return new PageImpl<>(res, books.getPageable(), books.getTotalElements());
    }

    public Page<Res_Get_Books> convertPageToPageHaveTime(Page<Book> books, ZonedDateTime now) {
        List<Res_Get_Books> res = convertListToListHaveTime(books.getContent(), now);
        return new PageImpl<>(res, books.getPageable(), books.getTotalElements());
    }

    public List<Res_Get_Books> convertListToListHaveTime(List<Book> books, ZonedDateTime now) {
        List<Res_Get_Books> res = new ArrayList<>();
        for (Book book : books) {
            Res_Get_Books temp = new Res_Get_Books();
            temp.convert(book, now);
            temp.setRating(reviewRepository.findAverageRatingByBookId(book.getBookId()));
            res.add(temp);
        }
        return res;
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, String keyword) {
        try {

            String search_word = Normalized.removeVietnameseAccents(keyword);
            Page<Book> books = bookRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all books successfully!")
                    .result(convertPageToPage(books))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve all books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Cacheable("fetchAllBooks")
    public ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size, BigDecimal leftBound, BigDecimal rightBound, String authorId, String publisherId, String distributorId, String bookName, String sort, String categoryIds) {
        try {
            String pattern = Normalized.removeVietnameseAccents(bookName);
            List<String> authors = Arrays.asList(authorId.split(",", -1));
            List<String> publishers = Arrays.asList(publisherId.split(",", -1));
            List<String> distributors = Arrays.asList(distributorId.split(",", -1));
            List<String> categories = Arrays.asList(categoryIds.split(",", -1));

            if (authorId.isEmpty()) {
                authors = new ArrayList<>();
            }
            if (publisherId.isEmpty()) {
                publishers = new ArrayList<>();
            }
            if (distributorId.isEmpty()) {
                distributors = new ArrayList<>();
            }

            if (categoryIds.isEmpty()) {
                categories = new ArrayList<>();
            }

            Specification<Book> spec = BookSpecification.withFilters(leftBound, rightBound, authors, publishers, distributors, pattern, sort, categories);

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Page<Book> books = bookRepository.findAll(spec, PageRequest.of(page - 1, size));
            System.err.println("HAD DB QUERY!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all books successfully!")
                    .result(convertPageToPageHaveTime(books, now))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve all books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId) {
        try {
            if (bookRepository.findByBookIdAndDeletedIsFalse(bookId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book is deleted or does not exist!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Book book = bookRepository.findByBookIdAndDeletedIsFalse(bookId).get();
            Res_Get_Books res = new Res_Get_Books();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            res.convert(book, now);
            res.setRating(reviewRepository.findAverageRatingByBookId(bookId));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved book details successfully!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve book details, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "newArrivals", allEntries = true),
            @CacheEvict(value = "fetchAllBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Book book) {
        try {
            // Validate input
            if (book.getBookName() == null || book.getBookName().isBlank()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Book name must have at least 1 character and cannot be just spaces.!").statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).success(false).build());
            }
            if (authorRepository.findById(book.getAuthorId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Author must not be null!").statusCode(HttpStatus.NOT_FOUND.value()).success(false).build());
            }
            if (publisherRepository.findById(book.getPublisherId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Publisher must not be null!").statusCode(HttpStatus.NOT_FOUND.value()).success(false).build());
            }
            if (distributorRepository.findById(book.getDistributorId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Distributor must not be null!").statusCode(HttpStatus.NOT_FOUND.value()).success(false).build());
            }
            if (bookTypeRepository.findById(book.getBookTypeId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book type must not be null!").statusCode(HttpStatus.NOT_FOUND.value()).success(false).build());
            }

            // Tạo Book entity
            Book res = new Book();
            List<Category> categories = categoryRepository.findAllById(book.getCategoriesId());
            res.setCategories(categories);
            for (Category category : categories) {
                category.getBooks().add(res);
            }

            res.setBookName(book.getBookName());
            res.setInStock(book.getInStock());
            res.setPrice(book.getPrice());
            res.setDescription(book.getDescription());
            res.setNumberOfPage(book.getNumberOfPage());
            res.setPublishedDate(book.getPublishedDate());
            res.setWeight(book.getWeight());
            res.setAuthor(authorRepository.findById(book.getAuthorId()).get());
            res.setPublisher(publisherRepository.findById(book.getPublisherId()).get());
            res.setDistributor(distributorRepository.findById(book.getDistributorId()).get());
            res.setBookType(bookTypeRepository.findById(book.getBookTypeId()).get());
            res.setNewArrival(book.getNewArrival());
            res.setNameNormalized(Normalized.remove(book.getBookName()));

            res = bookRepository.save(res);

            List<MultipartFile> images = book.getImages();
            if (images == null) {
                images = new ArrayList<>();
            }
            int thumbnailIdx = book.getThumbnailIdx();
            List<String> uploadedUrls = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file == null || file.isEmpty()) {
                    continue;
                }
                try {
                    Map<?, ?> data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
                    String url = (String) data.get("url");
                    Image image = new Image();
                    image.setBook(res);
                    image.setUrl(url);
                    res.addImage(image);
                    uploadedUrls.add(url);
                } catch (IOException io) {
                    System.err.println("Upload failed for index " + i + ": " + io.getMessage());
                }
            }

            // Đặt thumbnail
            if (thumbnailIdx >= 0 && thumbnailIdx < uploadedUrls.size()) {
                res.setUrlThumbnail(uploadedUrls.get(thumbnailIdx));
            } else if (res.getUrlThumbnail() == null && !uploadedUrls.isEmpty()) {
                res.setUrlThumbnail(uploadedUrls.get(0));
            }

            // Lưu lại Book với images + thumbnail (nếu cần)
            res = bookRepository.save(res);
            System.err.println("CREATE BOOK");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Book created successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(res)
                    .success(true)
                    .build());

        } catch (Exception ex) {
            System.err.println("Error creating book: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to create book, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }



    @Override
    public ResponseEntity<GenericResponse> adminGetBooksOfAuthor(int page, int size, String authorId) {
        try {
            System.err.println(authorId);
            Page<Book> books = bookRepository.findAllByAuthorAuthorId(PageRequest.of(page - 1, size), authorId);
            return ResponseEntity.status(HttpStatus.OK).body(
                    GenericResponse.builder()
                            .message("Retrieved books of the author successfully!")
                            .result(convertPageToPage(books))
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GenericResponse.builder()
                            .message("Failed to retrieve books of the author, message = " + ex.getMessage())
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> adminGetBooksOfPublisher(int page, int size, String publisherId) {
        try {
            Page<Book> books = bookRepository.findAllByPublisherPublisherId(PageRequest.of(page - 1, size), publisherId);
            return ResponseEntity.status(HttpStatus.OK).body(
                    GenericResponse.builder()
                            .message("Retrieved books of the publisher successfully!")
                            .result(convertPageToPage(books))
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GenericResponse.builder()
                            .message("Failed to retrieve books of the publisher, message = " + ex.getMessage())
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> adminGetBooksOfDistributor(int page, int size, String distributorId) {
        try {
            Page<Book> books = bookRepository.findAllByDistributorDistributorId(PageRequest.of(page - 1, size), distributorId);
            return ResponseEntity.status(HttpStatus.OK).body(
                    GenericResponse.builder()
                            .message("Retrieved books of the distributor successfully!")
                            .result(convertPageToPage(books))
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GenericResponse.builder()
                            .message("Failed to retrieve books of the distributor, message = " + ex.getMessage())
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> adminGetBooksOfCategory(int page, int size, String categoryId) {
        try {
            Page<Book> books = bookRepository.findAllByCategoriesCategoryId(PageRequest.of(page - 1, size), categoryId);
            List<Admin_Res_Get_Book> res = new ArrayList<>();
            for (Book book : books) {
                Admin_Res_Get_Book ele = new Admin_Res_Get_Book();
                ele.convert(book);
                res.add(ele);
            }
            Page<Admin_Res_Get_Book> dtoPage = new PageImpl<>(res, books.getPageable(), books.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK).body(
                    GenericResponse.builder()
                            .message("Retrieved books of the category successfully!")
                            .result(dtoPage)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GenericResponse.builder()
                            .message("Failed to retrieve books of the category, message = " + ex.getMessage())
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getPriceRange() {
        try {
            List<BigDecimal> res = bookRepository.findAllDistinctPricesOrderByAsc();
            return ResponseEntity.status(HttpStatus.OK).body(
                    GenericResponse.builder()
                            .message("Retrieved price range successfully!")
                            .result(res)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GenericResponse.builder()
                            .message("Failed to retrieve price range, message = " + ex.getMessage())
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (book.get().isDeleted()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                        .message("Book has already been deleted.!")
                        .statusCode(HttpStatus.NO_CONTENT.value())
                        .success(false)
                        .build());
            }
            book.get().setDeleted(true);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Book deleted successfully.!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to delete book, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "newArrivals", allEntries = true),
            @CacheEvict(value = "discountBooks", allEntries = true),
            @CacheEvict(value = "highRatingBooks", allEntries = true),
            @CacheEvict(value = "mostPurchasedBooks", allEntries = true),
            @CacheEvict(value = "mostPurchasedCategories", allEntries = true),
            @CacheEvict(value = "fetchAllBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> update(String bookId, Admin_Req_Update_Book book) {
        try {
            Optional<Book> _book = bookRepository.findById(bookId);
            if (_book.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (book.getBookName() == null || book.getBookName().isBlank()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Book name must have at least 1 character and cannot be just spaces.!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false).build());
            }

            if (authorRepository.findById(book.getAuthorId()).isEmpty()
                    || publisherRepository.findById(book.getPublisherId()).isEmpty()
                    || distributorRepository.findById(book.getDistributorId()).isEmpty()
                    || bookTypeRepository.findById(book.getBookTypeId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Author/Publisher/Distributor/Book type not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Book res = _book.get();

            for (Category category : res.getCategories()) {
                category.getBooks().remove(res);
            }
            res.getCategories().clear();
            List<Category> newCategories = categoryRepository.findAllById(book.getCategoriesId());
            res.setCategories(newCategories);
            for (Category category : newCategories) {
                if (!category.getBooks().contains(res)) {
                    category.getBooks().add(res);
                }
            }

            res.setBookName(book.getBookName());
            res.setInStock(book.getInStock());
            res.setPrice(book.getPrice());
            res.setDescription(book.getDescription());
            res.setNumberOfPage(book.getNumberOfPage());
            res.setPublishedDate(book.getPublishedDate());
            res.setWeight(book.getWeight());
            res.setAuthor(authorRepository.findById(book.getAuthorId()).get());
            res.setPublisher(publisherRepository.findById(book.getPublisherId()).get());
            res.setDistributor(distributorRepository.findById(book.getDistributorId()).get());
            res.setBookType(bookTypeRepository.findById(book.getBookTypeId()).get());
            res.setNewArrival(book.getNewArrival());
            res.setDeleted(book.getIsDeleted());

            res.getImages().removeIf(image -> !book.getRemainImages().contains(image.getImageId()));

            // Upload ảnh mới
            List<MultipartFile> images = book.getImages();
            if (images == null) images = new ArrayList<>();

            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                try {
                    Map<?, ?> data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
                    String url = (String) data.get("url");
                    Image image = new Image();
                    image.setBook(res);
                    image.setUrl(url);
                    res.addImage(image);
                } catch (IOException io) {
                    System.err.println("Upload failed: " + io.getMessage());
                }
            }

            // Đặt thumbnail
            int thumbnailIdx = book.getThumbnailIdx();
            if (thumbnailIdx >= 0 && thumbnailIdx < res.getImages().size()) {
                res.setUrlThumbnail(res.getImages().get(thumbnailIdx).getUrl());
            } else if (res.getUrlThumbnail() == null && !res.getImages().isEmpty()) {
                res.setUrlThumbnail(res.getImages().get(0).getUrl());
            }

            res.setNameNormalized(Normalized.remove(book.getBookName()));

            // Lưu book
            res = bookRepository.save(res);
            Admin_Res_Get_Book result = new Admin_Res_Get_Book();
            result.convert(res);
            System.err.println("UPDATE BOOK!");

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Book updated successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(result)
                    .success(true)
                    .build());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to update book, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> search(String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);
            List<Book> books = bookRepository.search(search_word, 5);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Searched books successfully!")
                    .result(books)
                    .statusCode(HttpStatus.OK.value())
                    .success(false)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to search books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Cacheable("newArrivals")
    public ResponseEntity<GenericResponse> getNewArrivalsBook() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            List<Book> books = bookRepository.findAllByDeletedIsFalseAndNewArrivalIsTrue();
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved new arrival books successfully!")
                    .result(convertListToListHaveTime(books, now))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve new arrival books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Cacheable("discountBooks")
    public ResponseEntity<GenericResponse> getDiscountBook() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            List<Book> books = bookRepository.findBooksWithActiveDiscount(now);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved discount books successfully!")
                    .result(convertListToListHaveTime(books, now))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve discount books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Cacheable("highRatingBooks")
    public ResponseEntity<GenericResponse> getHighRatingBook() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            List<Book> books = bookRepository.findTopBooksByAverageRating(10);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved high-rating books successfully!")
                    .result(convertListToListHaveTime(books, now))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Filed to retrieve high-rating books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Cacheable("mostPurchasedBooks")
    public ResponseEntity<GenericResponse> getMostPopularBooks() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            List<Book> books = bookRepository.findTopBooksBySoldQuantity(10);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved most purchased books successfully!")
                    .result(convertListToListHaveTime(books, now))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieved most purchased books, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Cacheable("mostPurchasedCategories")
    public ResponseEntity<GenericResponse> getBooksInCategoriesMostSold() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            List<Category> categories = categoryRepository.findTopCategoriesBySoldQuantity(3);

            List<Res_Get_Category_MostSold> res = new ArrayList<>();

            for (Category category : categories) {
                Res_Get_Category_MostSold ele = new Res_Get_Category_MostSold();
                ele.setCategoryId(category.getCategoryId());
                ele.setCategoryName(category.getCategoryName());
                for (Book book : category.getBooks()) {
                    Res_Get_Books temp = new Res_Get_Books();
                    temp.convert(book, now);
                    temp.setRating(reviewRepository.findAverageRatingByBookId(book.getBookId()));
                    ele.getBooks().add(temp);
                }
                res.add(ele);
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved bestseller categories and their books successfully.!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve bestseller categories and their books successfully, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
