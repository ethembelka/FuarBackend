package com.fuar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String image;
    
    // Only include basic profile info from UserInfo
    private String bio;
    private String headLine;
    private String location;
}
