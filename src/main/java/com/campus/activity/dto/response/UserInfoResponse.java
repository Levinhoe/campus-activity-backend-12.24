package com.campus.activity.dto.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String account;
    private String name;
    private String roleCode;
}
