package com.postgresql.StudentMarket.Dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParentWithChildrenDTO {
    Integer parentId;
    String parentName;
    List<ChildCategoryDTO> children;
}
