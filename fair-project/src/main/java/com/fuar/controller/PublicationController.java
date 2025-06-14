package com.fuar.controller;

import com.fuar.dto.PublicationDTO;
import com.fuar.model.Publication;
import com.fuar.model.PublicationType;
import com.fuar.service.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {
    private final PublicationService publicationService;

    @GetMapping("/user/{userInfoId}")
    public ResponseEntity<List<Publication>> getUserPublications(@PathVariable Long userInfoId) {
        return ResponseEntity.ok(publicationService.getUserPublications(userInfoId));
    }

    @PostMapping("/user/{userInfoId}")
    @PreAuthorize("@userInfoService.getUserInfo(#userInfoId).user.id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Publication> addPublication(
            @PathVariable Long userInfoId,
            @RequestBody PublicationDTO publicationDTO
    ) {
        Publication publication = new Publication();
        // Map DTO to entity
        publication.setTitle(publicationDTO.getTitle());
        publication.setDescription(publicationDTO.getDescription());
        publication.setPublisher(publicationDTO.getPublisher());
        publication.setUrl(publicationDTO.getUrl());
        publication.setDoi(publicationDTO.getDoi());
        publication.setPublicationDate(publicationDTO.getPublicationDate());
        publication.setAuthors(publicationDTO.getAuthors());
        publication.setPublicationType(publicationDTO.getPublicationType());

        return ResponseEntity.ok(publicationService.addPublication(userInfoId, publication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@publicationService.getPublicationById(#id).userInfo.user.id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Publication> updatePublication(
            @PathVariable Long id,
            @RequestBody PublicationDTO publicationDTO
    ) {
        Publication publication = new Publication();
        // Map DTO to entity
        publication.setTitle(publicationDTO.getTitle());
        publication.setDescription(publicationDTO.getDescription());
        publication.setPublisher(publicationDTO.getPublisher());
        publication.setUrl(publicationDTO.getUrl());
        publication.setDoi(publicationDTO.getDoi());
        publication.setPublicationDate(publicationDTO.getPublicationDate());
        publication.setAuthors(publicationDTO.getAuthors());
        publication.setPublicationType(publicationDTO.getPublicationType());

        return ResponseEntity.ok(publicationService.updatePublication(id, publication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@publicationService.getPublicationById(#id).userInfo.user.id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePublication(@PathVariable Long id) {
        publicationService.deletePublication(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Publication>> searchPublications(@RequestParam String keyword) {
        return ResponseEntity.ok(publicationService.searchPublications(keyword));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Publication>> findByPublicationType(@PathVariable PublicationType type) {
        return ResponseEntity.ok(publicationService.findByPublicationType(type));
    }

    @GetMapping("/doi/{doi}")
    public ResponseEntity<List<Publication>> findByDoi(@PathVariable String doi) {
        return ResponseEntity.ok(publicationService.findByDoi(doi));
    }
}
