package com.prl.smartexpensetracker.dto;

import lombok.Data;

@Data
public class UserPropertyRequest {
    private String key;
    private String value;
}