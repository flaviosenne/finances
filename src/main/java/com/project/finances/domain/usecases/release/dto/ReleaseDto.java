package com.project.finances.domain.usecases.release.dto;

import com.project.finances.domain.entity.Release;
import com.project.finances.domain.entity.Status;
import com.project.finances.domain.entity.Type;
import lombok.*;

import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class ReleaseDto {
    private String id;
    private Date dueDate;
    private String description;
    private String status;
    private String type;
    private Double value;
    private ReleaseCategoryDto category;

    public static Release of(ReleaseDto dto){
        return Release.builder()
                .dueDate(dto.getDueDate())
                .description(dto.getDescription())
                .status(Status.getStatus(dto.getStatus()))
                .type(Type.getType(dto.getType()))
                .value(dto.getValue())
                .build();
    }

    public static ReleaseDto of(Release entity){
        return ReleaseDto.builder()
                .id(entity.getId())
                .dueDate(entity.getDueDate())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .type(entity.getType().name())
                .value(entity.getValue())
                .category(ReleaseCategoryDto.builder().id(entity.getCategory().getId()).build())
                .build();
    }


}
