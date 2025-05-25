package com.bookstore.Service.Impl;

import com.bookstore.Constant.Gender;
import com.bookstore.Constant.Role;
import com.bookstore.DTO.*;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.UserService;
import com.bookstore.Specification.UserSpecification;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final Cloudinary cloudinary;

    public static boolean validatePassword(String password) {
        if (password == null) {
            return true;
        }
        boolean lengthValid = password.length() >= 8 && password.length() <= 32;
        boolean letterValid = password.matches(".*[a-zA-Z].*");
        boolean numberValid = password.matches(".*[0-9].*");
        return !lengthValid || !letterValid || !numberValid;
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email) {

        // ban dau la List<User>
        try {
            if (isActive != 0 && isActive != 1) isActive = 2;
            if (isVerified != 0 && isVerified != 1) isVerified = 2;
            StringBuilder pattern = new StringBuilder();
            for (char c : email.toCharArray()) {
                pattern.append("%").append(c).append("%");
            }
            if (pattern.length() == 0) {
                pattern = new StringBuilder("%%");
            }
            Specification<User> spec = UserSpecification.withFilters(isActive, isVerified, pattern.toString());
            Page<User> userList = userRepository.findAll(spec, PageRequest.of(page - 1, size));
            List<Admin_Res_Get_Users> res = new ArrayList<>();
            for (User user : userList) {
                String gender = "NULL";
                if (user.getGender() != null) gender = user.getGender().name();
                res.add(new Admin_Res_Get_Users(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getDateOfBirth(),
                        gender,
                        user.isActive(),
                        user.isVerified(),
                        user.getRole().name()
                ));
            }

            Page<Admin_Res_Get_Users> dtoPage = new PageImpl<>(res, userList.getPageable(), userList.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all users successfully!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception e) {
            log.error("Lấy danh sách người dùng thất bại, lỗi : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_Req_Update_UserStatus adminUpdateUserDTO) {
        try {
            log.info("Bắt đầu cập nhật trạng thái Người dùng!");
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("User not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (adminUpdateUserDTO.getActive() == null || adminUpdateUserDTO.getVerified() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .message("isActive and isVerified must not ne null!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (adminUpdateUserDTO.getRole() == null || Arrays.stream(Role.values()).noneMatch(e -> e.name().equals(adminUpdateUserDTO.getRole()))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Role not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            User _user = user.get();
            _user.setActive(Boolean.parseBoolean(adminUpdateUserDTO.getActive()));
            _user.setVerified(Boolean.parseBoolean(adminUpdateUserDTO.getVerified()));
            _user.setRole(Role.valueOf(adminUpdateUserDTO.getRole()));
            userRepository.save(_user);
            log.info("Cập nhật trạng thái người dùng thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("User status updated successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật trạng thái người dùng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> register(Register registerRequest) {
        try {

            if (registerRequest.getFullName() == null || registerRequest.getEmail() == null || registerRequest.getPhoneNumber() == null || registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Thông tin không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (registerRequest.getFullName().length() > 50) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Họ và tên có độ dài tối đa 50 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (registerRequest.getEmail().length() > 255) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Email có độ dài tối đa 255 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (registerRequest.getPhoneNumber().length() < 10 || registerRequest.getPhoneNumber().length() > 11 || isNumericInput(registerRequest.getPhoneNumber())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Số điện thoại phải có từ 10-11 số!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }


            if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Độ dài của mật khẩu phải có từ 8-32 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            Optional<User> user = userRepository.findByEmailAndVerifiedIsTrue(registerRequest.getEmail());
            if (user.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Email đã tồn tại!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }

            Optional<User> userNotVerified = userRepository.findByEmailAndVerifiedIsFalse(registerRequest.getEmail());

            userNotVerified.ifPresent(userRepository::delete);

            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu và xác nhận mật khẩu không khớp!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (validatePassword(registerRequest.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu phải có độ dài từ 8-32 ký tự, có ít nhất 1 chữ cái, có ít nhất 1 chữ số!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            User new_user = new User();
            new_user.setFullName(registerRequest.getFullName());
            new_user.setEmail(registerRequest.getEmail());
            new_user.setUserId(UUID.randomUUID().toString().split("-")[0]);
            new_user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            new_user.setPhoneNumber(registerRequest.getPhoneNumber());
            new_user.setRole(Role.USER);

            Cart cart = new Cart();

            new_user.setCart(cart);

            userRepository.save(new_user);


            emailVerificationService.sendOtp(registerRequest.getEmail());

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Đăng ký thành công. Vui lòng kiểm tra Emaild dể nhận OTP xác thực tài khoản!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đăng ký thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getProfile(String userId) {
        try {
            Optional<User> user = userRepository.findByUserIdAndActiveIsTrue(userId);
            if (user.isEmpty() || !user.get().isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Tài khoản đang bị khoá hoặc chưa xác thực!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }
            Profile profile = new Profile();
            profile.setEmail(user.get().getEmail());
            profile.setPhoneNumber(user.get().getPhoneNumber());
            profile.setFullName(user.get().getFullName());
            profile.setAvatar(user.get().getAvatar());
            profile.setGender(user.get().getGender().name());
            profile.setDateOfBirth(user.get().getDateOfBirth());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy thông tin cá nhân thành công!")
                    .result(profile)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception e) {
            log.error("Lấy thông tin cá nhân thất bại, lỗi : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> validateVerificationAccount(Req_Verify_OTPRegister register) {
        try {
            String email = register.getEmail();
            String otp = register.getOtp();
            if (email == null) {
                email = "";
            }
            if (otp == null) {
                otp = "";
            }
            Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtpAndEmail(otp, email);
            if (emailVerification.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("OTP không chính xác, vui lòng thử lại!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            emailVerificationRepository.delete(emailVerification.get());
            assert (userRepository.findByEmail(emailVerification.get().getEmail()).isPresent());
            User user = userRepository.findByEmail(emailVerification.get().getEmail()).get();
            user.setVerified(true);
            user.setActive(true);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Xác thực tài khoản thành công, vui lòng đăng nhập!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Xác thực tài khoản thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> changePassword(String userId, Req_Update_Password reqUpdatePassword) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Tài khoản không tồn tại!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            if (validatePassword(reqUpdatePassword.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu mới phải có độ dài từ 8-32 ký tự, có ít nhất 1 chữ cái, có ít nhất 1 chữ số!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (reqUpdatePassword.getNewPassword().length() < 8 || reqUpdatePassword.getNewPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Độ dài của mật khẩu phải có từ 8-32 ký tự")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }



            User user = userOptional.get();

            if (!passwordEncoder.matches(reqUpdatePassword.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu hiện tại không chính xác!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (reqUpdatePassword.getPassword().equals(reqUpdatePassword.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu hiện tại và mật khẩu mới giống nhau!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }

            Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtpAndEmail(reqUpdatePassword.getOtp(), user.getEmail());

            if (emailVerification.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("OTP không chính xác , vui lòng thử lại!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }

            emailVerificationRepository.delete(emailVerification.get());

            user.setPassword(passwordEncoder.encode(reqUpdatePassword.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Đổi mật khẩu thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đổi mật khẩu thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> changeAvatar(MultipartFile file, String userId) {
        try {
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            String url = (String) data.get("url");
            Optional<User> user = userRepository.findById(userId);
            assert (user.isPresent());
            user.get().setAvatar(url);
            userRepository.save(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Thay đổi Ảnh đại diện thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(url)
                    .success(true)
                    .build());
        } catch (IOException io) {
            log.error("Cập nhật avatar thất bại, lỗi : " + io.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> changeProfile(String userId, Req_Update_Profile profile) {
        try {
            if (userRepository.findById(userId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Tài khoản không tồn tại!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            User user = userRepository.findById(userId).get();
            user.setFullName(profile.getFullName());
            user.setPhoneNumber(profile.getPhoneNumber());
            user.setGender(Gender.valueOf(profile.getGender()));
            user.setDateOfBirth(profile.getDateOfBirth());
            user = userRepository.save(user);
            Profile res = new Profile();
            res.setFullName(user.getFullName());
            res.setPhoneNumber(user.getPhoneNumber());
            res.setEmail(user.getEmail());
            res.setAvatar(user.getAvatar());
            res.setGender(user.getGender().toString());
            res.setDateOfBirth(user.getDateOfBirth());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Thay đổi thông tin cá nhân thành công!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Cập nhật thông tin cá nhân thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User findOrCreateUser(String email, String fullName) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) return user.get();
        User new_user = new User();
        new_user.setFullName(fullName);
        new_user.setEmail(email);
        new_user.setUserId(UUID.randomUUID().toString().split("-")[0]);
        new_user.setRole(Role.USER);
        new_user.setVerified(true);
        new_user.setActive(true);
        new_user.setLastLoginAt(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        Cart cart = new Cart();
        cart = cartRepository.save(cart);

        new_user.setCart(cart);

        new_user = userRepository.save(new_user);

        return new_user;
    }

    public boolean isNumericInput(String input) {
        return input == null || !input.matches("\\d+");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> resetPassword(Req_Reset_Password password) {
        try {

            if (password.getOtp() == null || password.getEmail() == null || password.getNewPassword() == null || password.getConfirmPassword() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Không được để trống thông tin!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (password.getOtp().length() != 6 || isNumericInput(password.getOtp())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("OTP có chính xác 6 chữ số, vui lòng thử lại!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            Optional<User> userOptional = userRepository.findByEmail(password.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Tài khoản không tồn tại!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            if (validatePassword(password.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu phải có độ dài từ 8-32 ký tự, có ít nhất 1 chữ cái, có ít nhất 1 chữ số!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (password.getNewPassword().length() < 8 || password.getNewPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Độ dài của mật khẩu phải có từ 8-32 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }


            if (!password.getNewPassword().equals(password.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu mới và xác nhận mật khẩu mới không khớp!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }


            User user = userOptional.get();

            Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtpAndEmail(password.getOtp(), user.getEmail());

            if (emailVerification.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("OTP Không chính xác, vui lòng thử lại!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            emailVerificationRepository.delete(emailVerification.get());

            user.setPassword(passwordEncoder.encode(password.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Đặt lại mật khẩu thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đặt lại mật khẩu thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getUserById(String userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            Admin_Res_Get_Users res = new Admin_Res_Get_Users();
            if (user.isPresent()) {
                res = new Admin_Res_Get_Users(
                        user.get().getUserId(),
                        user.get().getFullName(),
                        user.get().getEmail(),
                        user.get().getPhoneNumber(),
                        user.get().getDateOfBirth(),
                        user.get().getGender().toString(),
                        user.get().isActive(),
                        user.get().isVerified(),
                        user.get().getRole().name()
                );
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy thông tin người dùng thành công!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            log.error("Lấy thông tin người dùng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> countVerifiedUsersByMonth(int year) {
        try {
            List<Object[]> rawResult = userRepository.countVerifiedUsersByMonth(year);
            Map<Integer, Long> result = new HashMap<>();

            // Khởi tạo 12 tháng với giá trị 0
            for (int month = 1; month <= 12; month++) {
                result.put(month, 0L);
            }

            // Ghi đè các tháng có dữ liệu thực tế
            for (Object[] row : rawResult) {
                int month = ((Number) row[0]).intValue();
                long count = ((Number) row[1]).longValue();
                result.put(month, count);
            }

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy thống kê người dùng mới của năm thành công!")
                    .result(result)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Lấy thông kê người dùng mới thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

}
