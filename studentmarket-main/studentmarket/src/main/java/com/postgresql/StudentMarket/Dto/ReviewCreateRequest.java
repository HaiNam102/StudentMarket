package com.postgresql.StudentMarket.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewCreateRequest {
    private Integer transactionId;
    private Integer revieweeId;
    private Integer rating;   // 1..5
    private String comment;
}
