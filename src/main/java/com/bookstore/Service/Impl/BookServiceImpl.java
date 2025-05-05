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
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;


    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, String keyword) {
        try {

            String search_word = Normalized.removeVietnameseAccents(keyword);
            Page<Book> books = bookRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);
            List<Admin_Res_Get_Book> res = new ArrayList<>();
            for (Book book : books) {
                Admin_Res_Get_Book ele = new Admin_Res_Get_Book();
                ele.convert(book);
                res.add(ele);
            }
            Page<Admin_Res_Get_Book> dtoPage = new PageImpl<>(res, books.getPageable(), books.getTotalElements());
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
    public ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size, BigDecimal leftBound, BigDecimal rightBound, String authorId, String publisherId, String distributorId, String bookName, String sort, String categoryIds) {
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
            List<String> categories = Arrays.asList(categoryIds.split(",", -1));

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

            if (categoryIds.isEmpty()) {
                categories = new ArrayList<>();
            }

            Specification<Book> spec = BookSpecification.withFilters(leftBound, rightBound, authors, publishers, distributors, pattern, sort, categories);

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
    public ResponseEntity<GenericResponse> getNewArrivalsBook() {
        try {
            List<Book> books = bookRepository.findAllByIsDeletedIsFalseAndNewArrivalIsTrue();
            List<Res_Get_Books> res = new ArrayList<>();
            for (Book book : books) {
                Res_Get_Books temp = new Res_Get_Books();
                temp.convert(book);
                temp.setRating(reviewRepository.findAverageRatingByBookId(book.getBookId()));
                res.add(temp);
            }
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get New Arrival Book Successfully!")
                    .result(res)
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Book book) {
        try {
            System.err.println(book.getBookTypeId());
            System.err.println(book.getBookName());
            System.err.println(book.getPublishedDate().toString());

            // Validate input
            if (book.getBookName() == null || book.getBookName().isBlank()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Book name must have at least 1 character not space!!!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            if (authorRepository.findById(book.getAuthorId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Author must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (publisherRepository.findById(book.getPublisherId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Publisher must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (distributorRepository.findById(book.getDistributorId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Distributor must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (bookTypeRepository.findById(book.getBookTypeId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book type must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            // Tạo Book
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
            res.setPublishedDate(book.getPublishedDate()); // Sử dụng publishedDate từ DTO
            res.setWeight(book.getWeight());
            res.setAuthor(authorRepository.findById(book.getAuthorId()).get());
            res.setPublisher(publisherRepository.findById(book.getPublisherId()).get());
            res.setDistributor(distributorRepository.findById(book.getDistributorId()).get());
            res.setBookType(bookTypeRepository.findById(book.getBookTypeId()).get());
            res.setNewArrival(book.getNewArrival());

            // Upload ảnh
            List<MultipartFile> images = book.getImages();
            if (images == null) {
                images = new ArrayList<>();
            }
            String bookId = res.getBookId();
            int thumbnailIdx = book.getThumbnailIdx();
            List<Map<String, Object>> uploadResults = new ArrayList<>();
            List<String> uploadedUrls = new ArrayList<>();
            boolean hasError = false;

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                Map<String, Object> result = new HashMap<>();
                if (file == null || file.isEmpty()) {
                    result.put("index", i);
                    result.put("status", "failed");
                    result.put("error", "File is empty or null");
                    uploadResults.add(result);
                    hasError = true;
                    continue;
                }
                try {
                    Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
                    String url = (String) data.get("url");
                    Image image = new Image();
                    image.setBook(res);
                    image.setUrl(url);
                    res.addImage(image);
                    uploadedUrls.add(url);
                    result.put("index", i);
                    result.put("url", url);
                    result.put("status", "success");
                } catch (IOException io) {
                    hasError = true;
                    result.put("index", i);
                    result.put("status", "failed");
                    result.put("error", "Upload failed: " + io.getMessage());
                }
                uploadResults.add(result);
            }

            // Đặt thumbnail
            String thumbnailUrl = res.getUrlThumbnail();
            if (thumbnailIdx >= 0 && thumbnailIdx < uploadedUrls.size()) {
                thumbnailUrl = uploadedUrls.get(thumbnailIdx);
                res.setUrlThumbnail(thumbnailUrl);
            } else if (thumbnailUrl == null && !uploadedUrls.isEmpty()) {
                thumbnailUrl = uploadedUrls.get(0);
                res.setUrlThumbnail(thumbnailUrl);
            }

            // Chuẩn hóa tên sách
            res.setNameNormalized(Normalized.remove(book.getBookName()));

            // Lưu Book
            res = bookRepository.save(res);

            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Create Book success!!!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(res)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            System.err.println("Error creating book" + ex);
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Book failed: " + ex.getMessage())
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
    public ResponseEntity<GenericResponse> update(String bookId, Admin_Req_Update_Book book) {
        try {
            Optional<Book> _book = bookRepository.findById(bookId);
            if (_book.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found book!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (book.getBookName() == null || book.getBookName().isBlank()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Book name must have at least 1 character not space!!!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            if (authorRepository.findById(book.getAuthorId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Author must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (publisherRepository.findById(book.getPublisherId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Publisher must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (distributorRepository.findById(book.getDistributorId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Distributor must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (bookTypeRepository.findById(book.getBookTypeId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book type must not null!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            // Tạo Book
            Book res = _book.get();
            for (Category category : res.getCategories()) {
                category.getBooks().remove(res); // Remove the book from the category's book list
            }
            res.getCategories().clear(); // Clear all categories from the book

            List<Category> newCategories = categoryRepository.findAllById(book.getCategoriesId());
            res.setCategories(newCategories); // Set the new category list

            for (Category category : newCategories) {
                if (!category.getBooks().contains(res)) {
                    category.getBooks().add(res); // Add the book to the category's book list
                }
            }
            res.setBookName(book.getBookName());
            res.setInStock(book.getInStock());
            res.setPrice(book.getPrice());
            res.setDescription(book.getDescription());
            res.setNumberOfPage(book.getNumberOfPage());
            res.setPublishedDate(book.getPublishedDate()); // Sử dụng publishedDate từ DTO
            res.setWeight(book.getWeight());
            res.setAuthor(authorRepository.findById(book.getAuthorId()).get());
            res.setPublisher(publisherRepository.findById(book.getPublisherId()).get());
            res.setDistributor(distributorRepository.findById(book.getDistributorId()).get());
            res.setBookType(bookTypeRepository.findById(book.getBookTypeId()).get());
            res.setNewArrival(book.getNewArrival());
            res.setIsDeleted(book.getIsDeleted());

            res.getImages().removeIf(image -> !book.getRemainImages().contains(image.getImageId()));

            // Upload ảnh
            List<MultipartFile> images = book.getImages();
            if (images == null) {
                images = new ArrayList<>();
            }
            int thumbnailIdx = book.getThumbnailIdx();
            List<Map<String, Object>> uploadResults = new ArrayList<>();
            List<String> uploadedUrls = new ArrayList<>();
            boolean hasError = false;

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                Map<String, Object> result = new HashMap<>();
                if (file == null || file.isEmpty()) {
                    result.put("index", i);
                    result.put("status", "failed");
                    result.put("error", "File is empty or null");
                    uploadResults.add(result);
                    hasError = true;
                    continue;
                }
                try {
                    Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
                    String url = (String) data.get("url");
                    Image image = new Image();
                    image.setBook(res);
                    image.setUrl(url);
                    res.addImage(image);
                    uploadedUrls.add(url);
                    result.put("index", i);
                    result.put("url", url);
                    result.put("status", "success");
                } catch (IOException io) {
                    hasError = true;
                    result.put("index", i);
                    result.put("status", "failed");
                    result.put("error", "Upload failed: " + io.getMessage());
                }
                uploadResults.add(result);
            }

            // Đặt thumbnail
            String thumbnailUrl = res.getUrlThumbnail();
            if (thumbnailIdx >= 0 && thumbnailIdx < res.getImages().size()) {
                thumbnailUrl = res.getImages().get(thumbnailIdx).getUrl();
                res.setUrlThumbnail(thumbnailUrl);
            } else if (thumbnailUrl == null && !res.getImages().isEmpty()) {
                thumbnailUrl = res.getImages().get(0).getUrl();
                res.setUrlThumbnail(thumbnailUrl);
            }

            // Chuẩn hóa tên sách
            res.setNameNormalized(Normalized.remove(book.getBookName()));

            // Lưu Book
            res = bookRepository.save(res);

            Admin_Res_Get_Book result = new Admin_Res_Get_Book();
            result.convert(res);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Update book successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(result)
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
