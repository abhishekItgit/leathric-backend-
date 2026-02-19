package com.leathric.controller;

import com.leathric.dto.AddressDtos;
import com.leathric.dto.ApiResponse;
import com.leathric.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * Get all addresses for current user
     * GET /api/addresses
     */
    @GetMapping
    public ApiResponse<List<AddressDtos.AddressResponse>> getMyAddresses() {
        return ApiResponse.<List<AddressDtos.AddressResponse>>builder()
                .success(true)
                .message("Addresses retrieved successfully")
                .data(addressService.getMyAddresses())
                .build();
    }

    /**
     * Get address by ID
     * GET /api/addresses/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<AddressDtos.AddressResponse> getAddressById(@PathVariable Long id) {
        return ApiResponse.<AddressDtos.AddressResponse>builder()
                .success(true)
                .message("Address retrieved successfully")
                .data(addressService.getAddressById(id))
                .build();
    }

    /**
     * Get default address
     * GET /api/addresses/default
     */
    @GetMapping("/default")
    public ApiResponse<AddressDtos.AddressResponse> getDefaultAddress() {
        return ApiResponse.<AddressDtos.AddressResponse>builder()
                .success(true)
                .message("Default address retrieved successfully")
                .data(addressService.getDefaultAddress())
                .build();
    }

    /**
     * Create new address
     * POST /api/addresses
     */
    @PostMapping
    public ApiResponse<AddressDtos.AddressResponse> createAddress(
            @Valid @RequestBody AddressDtos.AddressRequest request) {
        return ApiResponse.<AddressDtos.AddressResponse>builder()
                .success(true)
                .message("Address created successfully")
                .data(addressService.createAddress(request))
                .build();
    }

    /**
     * Update address
     * PUT /api/addresses/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<AddressDtos.AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDtos.AddressRequest request) {
        return ApiResponse.<AddressDtos.AddressResponse>builder()
                .success(true)
                .message("Address updated successfully")
                .data(addressService.updateAddress(id, request))
                .build();
    }

    /**
     * Set address as default
     * PATCH /api/addresses/{id}/default
     */
    @PatchMapping("/{id}/default")
    public ApiResponse<AddressDtos.AddressResponse> setAsDefault(@PathVariable Long id) {
        return ApiResponse.<AddressDtos.AddressResponse>builder()
                .success(true)
                .message("Address set as default")
                .data(addressService.setAsDefault(id))
                .build();
    }

    /**
     * Delete address
     * DELETE /api/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Address deleted successfully")
                .build();
    }
}
