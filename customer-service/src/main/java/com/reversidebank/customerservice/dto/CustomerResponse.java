package com.reversidebank.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String street;
    private String suburb;
    private String city;
    private String postalCode;
    private String province;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
}
