package com.fuar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String bio;
    private String headLine;
    private String location;
    private String country;
    private String linkedinUrl;
    private String githubUrl;
    private String personalWebsite;
    private String twitterUrl;
    private String instagramUrl;
    private String facebookUrl;
    private String youtubeUrl;
    private String mediumUrl;
    private String scholarUrl;
    private String researchGateUrl;
    private String orcidId;
}
