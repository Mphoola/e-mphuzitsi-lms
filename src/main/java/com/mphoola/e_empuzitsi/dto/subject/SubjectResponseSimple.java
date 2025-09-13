package com.mphoola.e_empuzitsi.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectResponseSimple {
    private Long id;
    private String name;
    private Long lessonCount;
    private Long studentCount;
}