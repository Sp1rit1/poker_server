package com.io.github.Sp1rit1.poker_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendDto {
    private Long userId;
    private String username;
    private String friendCode;
}