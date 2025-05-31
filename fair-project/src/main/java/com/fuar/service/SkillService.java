package com.fuar.service;

import com.fuar.model.Skill;
import com.fuar.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
    }

    public Skill getSkillByName(String name) {
        return skillRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
    }

    @Transactional
    public Skill createSkill(Skill skill) {
        if (skillRepository.existsByName(skill.getName())) {
            throw new RuntimeException("Skill with this name already exists");
        }
        return skillRepository.save(skill);
    }

    @Transactional
    public Skill updateSkill(Long id, Skill skillDetails) {
        Skill skill = getSkillById(id);
        
        if (!skill.getName().equals(skillDetails.getName()) && 
            skillRepository.existsByName(skillDetails.getName())) {
            throw new RuntimeException("Skill with this name already exists");
        }

        skill.setName(skillDetails.getName());
        skill.setDescription(skillDetails.getDescription());
        
        return skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new RuntimeException("Skill not found");
        }
        skillRepository.deleteById(id);
    }

    public List<Skill> searchSkills(String keyword) {
        return skillRepository.searchByKeyword(keyword);
    }

    public List<Skill> getMostPopularSkills() {
        return skillRepository.findMostPopularSkills();
    }

    public List<Skill> getUserSkills(Long userInfoId) {
        return skillRepository.findByUserInfoId(userInfoId);
    }
}
