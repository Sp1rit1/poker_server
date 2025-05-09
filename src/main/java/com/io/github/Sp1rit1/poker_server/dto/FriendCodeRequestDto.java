package com.io.github.Sp1rit1.poker_server.dto;

import jakarta.validation.constraints.NotBlank; // Для валидации
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FriendCodeRequestDto {

    @NotBlank(message = "Friend code cannot be blank")
    @Size(min = 6, max = 6, message = "Friend code must be 6 characters long")
    private String friendCode;
}
