package com.bookstore.Service.Impl;

import com.bookstore.DTO.*;
import com.bookstore.Entity.Address;
import com.bookstore.Repository.AddressRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<GenericResponse> getAll(String userId) {
        try {
            List<Address> addresses = addressRepository.findAllByUserUserId(userId);
            List<Res_Get_Address> res = new ArrayList<>();
            for (Address address : addresses) {
                res.add(new Res_Get_Address(
                        address.getAddressId(),
                        address.getFullName(),
                        address.getPhoneNumber(),
                        address.getAddressInformation(),
                        address.getOtherDetail(),
                        address.isDefaultAddress()
                ));
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách tác giả thành công!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách địa chỉ thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> create(Req_Create_Address createAddress, String userId) {
        try {
            log.info("Bắt đầu tạo địa chỉ!");
            if (addressRepository.findAllByUserUserId(userId).size() >= 10) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .message("Bạn chỉ có thể lưu tối đa 10 địa chỉ, vui lòng xoá địa chỉ khác trước khi tạo địa chỉ mới!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .build());
            }

            Address tempAddress = new Address();
            tempAddress.setFullName(createAddress.getFullName());
            tempAddress.setPhoneNumber(createAddress.getPhoneNumber());
            tempAddress.setAddressInformation(createAddress.getAddressInformation());
            tempAddress.setOtherDetail(createAddress.getOtherDetail());
            assert(userRepository.findById(userId).isPresent());
            tempAddress.setUser(userRepository.findById(userId).get());
            if (addressRepository.countByUserUserId(userId) == 0) {
                tempAddress.setDefaultAddress(true);
            }
            Address address = addressRepository.save(tempAddress);
            log.info("Tạo địa chỉ thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo địa chỉ mới thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(new Res_Get_Address(
                            address.getAddressId(),
                            address.getFullName(),
                            address.getPhoneNumber(),
                            address.getAddressInformation(),
                            address.getOtherDetail(),
                            address.isDefaultAddress()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo địa chỉ thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String addressId, String userId) {
        try {
            log.info("Bắt đầu xoá địa chỉ!");
            Optional<Address> address = addressRepository.findByAddressIdAndUserUserId(addressId, userId);
            if (address.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy địa chỉ!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (address.get().isDefaultAddress()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .message("Không thể xoá địa chỉ mặc định, vui lòng chọn địa chỉ khác làm mặc định trước khi xoá địa chỉ này!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .build());
            }

            addressRepository.delete(address.get());
            log.info("Xoá địa chỉ thành công!");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Xoá địa chỉ thành công!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Xoá đa chỉ thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                .message("Lỗi hệ thống!")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .success(false)
                .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(Req_PatchUpdate_Address tempAddress, String userId, String addressId) {
        try {
            log.info("Bắt đầu cập nhật địa chỉ!");
            Optional<Address> _address = addressRepository.findByAddressIdAndUserUserId(addressId, userId);
            if (_address.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy địa chỉ!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (tempAddress.getFullName() != null) {
                _address.get().setFullName(tempAddress.getFullName());
            }

            if (tempAddress.getPhoneNumber() != null) {
                _address.get().setPhoneNumber(tempAddress.getPhoneNumber());
            }

            if (tempAddress.getAddressInformation() != null) {
                _address.get().setAddressInformation(tempAddress.getAddressInformation());
            }

            if (tempAddress.getOtherDetail() != null) {
                _address.get().setOtherDetail(tempAddress.getOtherDetail());
            }

            Address address = addressRepository.save(_address.get());
            log.info("Cập nhật địa chỉ thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật địa chỉ thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Res_Get_Address(
                            address.getAddressId(),
                            address.getFullName(),
                            address.getPhoneNumber(),
                            address.getAddressInformation(),
                            address.getOtherDetail(),
                            address.isDefaultAddress()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật địa chỉ thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> setDefault(String userId, String addressId) {
        try {
            log.info("Bắt đầu cập nhật địa chỉ làm mặc định!");
            if (addressRepository.findByAddressIdAndUserUserId(addressId, userId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy địa chỉ!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (addressRepository.findDefaultAddressOfUser(userId).isPresent()) {
                if (addressRepository.findDefaultAddressOfUser(userId).get().getAddressId().equals(addressId)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .message("Địa chỉ đã là mặc định rồi!")
                            .statusCode(HttpStatus.CONFLICT.value())
                            .success(false)
                            .build());
                }
                Address address = addressRepository.findDefaultAddressOfUser(userId).get();
                address.setDefaultAddress(false);
                addressRepository.save(address);
            }
            assert (addressRepository.findByAddressId(addressId).isPresent());
            Address address = addressRepository.findByAddressId(addressId).get();
            address.setDefaultAddress(true);
            addressRepository.save(address);
            log.info("Cập nhật địa chỉ làm mặc định thành công!");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Đặt làm địa chỉ mặc định thành công!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật địa chỉ làm mặc định thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
