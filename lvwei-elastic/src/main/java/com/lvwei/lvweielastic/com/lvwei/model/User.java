package com.lvwei.lvweielastic.com.lvwei.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author lvwei
 * @date 2021/9/22
 * @description 用户
 */
@Data
@Builder
public class User {
    private Long userId;
    private String userName;
    private Integer age;
    private String remark;
}
