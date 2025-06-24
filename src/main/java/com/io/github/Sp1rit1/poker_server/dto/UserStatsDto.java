package com.io.github.Sp1rit1.poker_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Long userId;
    private int handsPlayed;
    private int handsWon;
}