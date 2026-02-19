package com.leathric.service;

import com.leathric.dto.AddressDtos;

import java.util.List;

public interface AddressService {

    /**
     * Get all addresses for current user
     */
    List<AddressDtos.AddressResponse> getMyAddresses();

    /**
     * Get address by ID
     */
    AddressDtos.AddressResponse getAddressById(Long id);

    /**
     * Get default address
     */
    AddressDtos.AddressResponse getDefaultAddress();

    /**
     * Create new address
     */
    AddressDtos.AddressResponse createAddress(AddressDtos.AddressRequest request);

    /**
     * Update existing address
     */
    AddressDtos.AddressResponse updateAddress(Long id, AddressDtos.AddressRequest request);

    /**
     * Set address as default
     */
    AddressDtos.AddressResponse setAsDefault(Long id);

    /**
     * Delete address
     */
    void deleteAddress(Long id);
}
