package com.leathric.dto;

import com.leathric.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class AddressDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        
        // Support both 'fullName' and 'name' from frontend
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        private String fullName;

        @Size(max = 100, message = "Name must not exceed 100 characters")
        private String name;

        // Support both 'phoneNumber' and 'phone' from frontend
        @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "Invalid phone number format")
        private String phoneNumber;

        @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "Invalid phone format")
        private String phone;

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 200, message = "Address line 1 must not exceed 200 characters")
        private String addressLine1;

        @Size(max = 200, message = "Address line 2 must not exceed 200 characters")
        private String addressLine2;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must not exceed 100 characters")
        private String state;

        // Support both 'postalCode' and 'zipCode' from frontend
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        private String postalCode;

        @Size(max = 20, message = "Zip code must not exceed 20 characters")
        private String zipCode;

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        private String country;

        // Make type optional, default to BOTH
        private AddressType type;

        private Boolean isDefault;

        // Helper methods to get the correct value with fallback
        public String getFullName() {
            if (fullName != null && !fullName.isBlank()) {
                return fullName;
            }
            if (name != null && !name.isBlank()) {
                return name;
            }
            throw new IllegalArgumentException("Full name or name is required");
        }

        public String getPhoneNumber() {
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                return phoneNumber;
            }
            if (phone != null && !phone.isBlank()) {
                return phone;
            }
            throw new IllegalArgumentException("Phone number or phone is required");
        }

        public String getPostalCode() {
            if (postalCode != null && !postalCode.isBlank()) {
                return postalCode;
            }
            if (zipCode != null && !zipCode.isBlank()) {
                return zipCode;
            }
            throw new IllegalArgumentException("Postal code or zip code is required");
        }

        public AddressType getType() {
            return type != null ? type : AddressType.BOTH;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AddressResponse {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private AddressType type;
        private Boolean isDefault;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
