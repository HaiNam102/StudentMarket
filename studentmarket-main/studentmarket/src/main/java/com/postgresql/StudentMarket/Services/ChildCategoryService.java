package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Dto.ChildCategoryDTO;
import com.postgresql.StudentMarket.Repository.ChildCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildCategoryService {

    private final ChildCategoryRepository childCategoryRepository;

    public List<ChildCategoryDTO> findAllChildren() {
        var list = childCategoryRepository.findAll().stream()
                .map(c -> new ChildCategoryDTO(c.getChildId(), c.getName()))
                .collect(Collectors.toList());
        System.out.println(">>> childCategories.size=" + list.size());
        return list;
    }

}
