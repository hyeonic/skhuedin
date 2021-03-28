package com.skhuedin.skhuedin.domain;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Talent extends BaseEntity{
    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "talent")
    List<SkillCategory> skillCategories = new ArrayList<>();


    @OneToMany(mappedBy = "talent")
    List<Roadmap> roadmaps = new ArrayList<>();


    private String talentImageUrl;
    private String content;
    private String view;


    private LocalDateTime entranceYear;
    private LocalDateTime graduationYear;

    private LocalDateTime start_career;


    @Builder
    public Talent(Long id, String talentImageUrl, String content,
                  String view, LocalDateTime entranceYear, LocalDateTime graduationYear,
                  LocalDateTime start_career) {
        this.id = id;
        this.talentImageUrl = talentImageUrl;
        this.content = content;
        this.view = view;
        this.entranceYear = entranceYear;
        this.graduationYear = graduationYear;
        this.start_career = start_career;
    }


}
