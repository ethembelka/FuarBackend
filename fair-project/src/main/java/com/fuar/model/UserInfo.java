package com.fuar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"user", "workExperiences", "educations", "publications", "skills"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_info")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "userInfo")
    @JsonBackReference("user-info")
    private User user;

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

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<WorkExperience> workExperiences = new HashSet<>();

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Education> educations = new HashSet<>();

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Publication> publications = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "user_info_skills",
        joinColumns = @JoinColumn(name = "user_info_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonManagedReference("userinfo-skills")
    private Set<Skill> skills = new HashSet<>();
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UserInfo other = (UserInfo) obj;
        if (id == null) {
            return other.id == null;
        } else {
            return id.equals(other.id);
        }
    }
}
