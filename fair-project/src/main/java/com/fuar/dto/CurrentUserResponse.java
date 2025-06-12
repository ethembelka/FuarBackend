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
public class CurrentUserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String image;
}
