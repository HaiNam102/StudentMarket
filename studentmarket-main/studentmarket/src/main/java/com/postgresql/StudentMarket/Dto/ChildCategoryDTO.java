package com.postgresql.StudentMarket.Dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChildCategoryDTO {
    Integer childId;
    String name;
}
