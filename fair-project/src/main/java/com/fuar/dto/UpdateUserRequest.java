package com.fuar.dto;

import com.fuar.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String name;
    private String email;
    private String password;  // Optional, only update if provided
    private Role role;
    private String image;
    private UserInfoDTO userInfo;
}
