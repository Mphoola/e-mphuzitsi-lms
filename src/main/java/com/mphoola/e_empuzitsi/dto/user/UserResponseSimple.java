package com.mphoola.e_empuzitsi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseSimple {
    private Long id;
    private String name;
    private String email;
    private String accountType;
    private String status;
}
