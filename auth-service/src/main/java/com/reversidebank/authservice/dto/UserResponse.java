package com.reversidebank.authservice.dto;

import com.reversidebank.authservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private Role role;
    private Instant createdAt;

}
