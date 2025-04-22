package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Author;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Admin_Req_Update_Author;
import com.bookstore.Entity.Author;
import com.bookstore.Entity.Book;
import com.bookstore.Repository.AuthorRepository;
import com.bookstore.Service.AuthorService;
import com.bookstore.Utils.Normalized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorServiceImpl implements AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Author createAuthor) {
        try {
            Author author = new Author();
            author.setAuthorName(createAuthor.getAuthorName());
            author.setNameNormalized(Normalized.removeVietnameseAccents(createAuthor.getAuthorName()));
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Author successfully!")
                            .result(authorRepository.save(author))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Author failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Author> authors = authorRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Author Successfully!")
                            .result(authors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Author failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String s = Normalized.removeVietnameseAccents(keyword);
            String search_word = "";
            for (char c : s.toCharArray()) {
                search_word += "%" + c + "%";
            }

            if (search_word.length() == 0) {
                search_word = "%%";
            }

            System.err.println(search_word);

            Page<Author> authors = authorRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Author Successfully!")
                            .result(authors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Search Author failed!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String authorId, Admin_Req_Update_Author updateAuthor) {
        try {
            Author author = authorRepository.findById(authorId).get();
            author.setAuthorName(updateAuthor.getAuthorName());
            author.setNameNormalized(Normalized.removeVietnameseAccents(updateAuthor.getAuthorName()));
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Update Author successfully!")
                            .result(authorRepository.save(author))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Update Author failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String authorId) {
        try {
            authorRepository.deleteById(authorId);
            return ResponseEntity.status(200).body(
                    GenericResponse.builder()
                            .message("Delete Author successfully!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Delete Author failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String s = Normalized.removeVietnameseAccents(keyword);
            String search_word = "";
            for (char c : s.toCharArray()) {
                search_word += "%" + c + "%";
            }

            if (search_word.length() == 0) {
                search_word = "%%";
            }

            System.err.println(search_word);

            List<Author> authors = authorRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Author Successfully!")
                            .result(authors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Search Author failed!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }


}
