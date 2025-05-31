package com.fuar.dto;

import com.fuar.model.PublicationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicationDTO {
    private Long id;
    private String title;
    private String description;
    private String publisher;
    private String url;
    private String doi;
    private LocalDate publicationDate;
    private String authors;
    private PublicationType publicationType;
}
