package com.example.ssafy301.team.dto;

import com.example.ssafy301.team.domain.Team;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeamDetailDto {

    private Long id;
    private String teamName;
    private String korName;
    private String createdYear;
    private String teamLogo;
    private String teamLocation;
    private List<Integer> activeYears;

    public TeamDetailDto(Team team, List<Integer> activeYears) {
        this.id = team.getId();
        this.teamName = team.getTeamName();
        this.korName = team.getKorName();
        this.createdYear = team.getCreatedYear();
        this.teamLogo = team.getTeamLogo();
        this.teamLocation = team.getTeamLocation();
        this.activeYears = new ArrayList<>(activeYears);
    }
}
