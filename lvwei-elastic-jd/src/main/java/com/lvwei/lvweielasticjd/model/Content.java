package com.lvwei.lvweielasticjd.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author lvwei
 * @date 2021/9/22
 * @description content
 */
@Data
@Builder
public class Content {
    private String img;
    private String price;
    private String title;
}
