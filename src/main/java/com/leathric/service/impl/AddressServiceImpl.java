package com.leathric.service.impl;

import com.leathric.dto.AddressDtos;
import com.leathric.entity.Address;
import com.leathric.entity.AddressType;
import com.leathric.entity.User;
import com.leathric.exception.BadRequestException;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.AddressRepository;
import com.leathric.repository.UserRepository;
import com.leathric.service.AddressService;
import com.leathric.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressDtos.AddressResponse> getMyAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDtos.AddressResponse getAddressById(Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDtos.AddressResponse getDefaultAddress() {
        User user = getCurrentUser();
        Address address = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));
        return toResponse(address);
    }

    @Override
    @Transactional
    public AddressDtos.AddressResponse createAddress(AddressDtos.AddressRequest request) {
        log.info("Creating address for user: {}", SecurityUtils.currentUserEmail());
        
        User user = getCurrentUser();

        // If this is set as default, clear other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            if (request.getType() == AddressType.BOTH) {
                addressRepository.clearAllDefaultByUserId(user.getId());
            } else {
                addressRepository.clearDefaultByUserIdAndType(user.getId(), request.getType());
            }
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .type(request.getType())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        Address saved = addressRepository.save(address);
        log.info("Address {} created successfully", saved.getId());
        
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AddressDtos.AddressResponse updateAddress(Long id, AddressDtos.AddressRequest request) {
        log.info("Updating address {}", id);
        
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // If setting as default, clear other defaults
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            if (request.getType() == AddressType.BOTH) {
                addressRepository.clearAllDefaultByUserId(user.getId());
            } else {
                addressRepository.clearDefaultByUserIdAndType(user.getId(), request.getType());
            }
        }

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setType(request.getType());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        Address saved = addressRepository.save(address);
        log.info("Address {} updated successfully", id);
        
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AddressDtos.AddressResponse setAsDefault(Long id) {
        log.info("Setting address {} as default", id);
        
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Clear other defaults based on type
        if (address.getType() == AddressType.BOTH) {
            addressRepository.clearAllDefaultByUserId(user.getId());
        } else {
            addressRepository.clearDefaultByUserIdAndType(user.getId(), address.getType());
        }

        address.setIsDefault(true);
        Address saved = addressRepository.save(address);
        
        log.info("Address {} set as default", id);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        log.info("Deleting address {}", id);
        
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
        log.info("Address {} deleted successfully", id);
    }

    // ==================== Private Helper Methods ====================

    private User getCurrentUser() {
        return userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AddressDtos.AddressResponse toResponse(Address address) {
        return AddressDtos.AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .type(address.getType())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
