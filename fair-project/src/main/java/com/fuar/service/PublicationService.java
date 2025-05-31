package com.fuar.service;

import com.fuar.model.Publication;
import com.fuar.model.PublicationType;
import com.fuar.model.UserInfo;
import com.fuar.repository.PublicationRepository;
import com.fuar.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicationService {
    private final PublicationRepository publicationRepository;
    private final UserInfoRepository userInfoRepository;

    public List<Publication> getUserPublications(Long userInfoId) {
        return publicationRepository.findByUserInfo_IdOrderByPublicationDateDesc(userInfoId);
    }

    @Transactional
    public Publication addPublication(Long userInfoId, Publication publication) {
        UserInfo userInfo = userInfoRepository.findById(userInfoId)
                .orElseThrow(() -> new RuntimeException("UserInfo not found"));

        publication.setUserInfo(userInfo);
        return publicationRepository.save(publication);
    }

    @Transactional
    public Publication updatePublication(Long id, Publication publicationDetails) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        publication.setTitle(publicationDetails.getTitle());
        publication.setDescription(publicationDetails.getDescription());
        publication.setPublisher(publicationDetails.getPublisher());
        publication.setUrl(publicationDetails.getUrl());
        publication.setDoi(publicationDetails.getDoi());
        publication.setPublicationDate(publicationDetails.getPublicationDate());
        publication.setAuthors(publicationDetails.getAuthors());
        publication.setPublicationType(publicationDetails.getPublicationType());

        return publicationRepository.save(publication);
    }

    public void deletePublication(Long id) {
        if (!publicationRepository.existsById(id)) {
            throw new RuntimeException("Publication not found");
        }
        publicationRepository.deleteById(id);
    }

    public List<Publication> searchPublications(String keyword) {
        return publicationRepository.searchByKeyword(keyword);
    }

    public List<Publication> findByPublicationType(PublicationType type) {
        return publicationRepository.findByPublicationType(type);
    }

    public List<Publication> findByDoi(String doi) {
        return publicationRepository.findByDoi(doi);
    }
}
