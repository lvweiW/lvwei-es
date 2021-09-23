package com.lvwei.lvweielasticjd.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author lvwei
 * @date 2021/9/22
 * @description TODO
 */
public interface IContentService {
    Boolean parseContent(String keyword) throws IOException;

    List<Map<String, Object>> searchPage(String keyword, int page, int rows) throws IOException;
}
