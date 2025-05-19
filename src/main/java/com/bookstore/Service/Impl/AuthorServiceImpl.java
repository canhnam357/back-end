package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Author;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Admin_Req_Update_Author;
import com.bookstore.Entity.Author;
import com.bookstore.Repository.AuthorRepository;
import com.bookstore.Service.AuthorService;
import com.bookstore.Utils.Normalized;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Author createAuthor) {
        try {
            log.info("Bắt đầu tạo tác giả!");
            Author author = new Author();
            author.setAuthorName(createAuthor.getAuthorName());
            author.setNameNormalized(Normalized.remove(createAuthor.getAuthorName()));
            log.info("Tạo tác giả thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo tác giả mới thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(authorRepository.save(author))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo tác giả thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Author> authors = authorRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách tác giả thành công!")
                    .result(authors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách tác giả thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            Page<Author> authors = authorRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm tác giả thành công!")
                    .result(authors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tìm kiếm tác giả thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String authorId, Admin_Req_Update_Author updateAuthor) {
        try {
            log.info("Bắt đầu cập nhật tác giả!");
            if (authorRepository.findById(authorId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy tác giả!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Author author = authorRepository.findById(authorId).get();
            author.setAuthorName(updateAuthor.getAuthorName());
            author.setNameNormalized(Normalized.remove(updateAuthor.getAuthorName()));
            log.info("Cập nhật tác giả thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật thông tin tác giả thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(authorRepository.save(author))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật tác giả thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String authorId) {
        try {
            log.info("Bắt đầu xoá tác giả!");
            if (authorRepository.findById(authorId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy tác giả!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            authorRepository.deleteById(authorId);
            log.info("Xoá tác giả thành công!");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Xoá tác giả thành công!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Xoá tác giả thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            List<Author> authors = authorRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Tìm kiếm tác giả thành công!")
                    .result(authors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách tác giả thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }


}
