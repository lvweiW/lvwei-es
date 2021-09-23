package com.lvwei.lvweielasticjd.controller;

import com.lvwei.lvweielasticjd.service.IContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author lvwei
 * @date 2021/9/22
 * @description TODO
 */
@RestController
public class ContentController {
    @Autowired
    private IContentService iContentService;

    @GetMapping("/createDoc/{keyword}")
    public String createDoc(@PathVariable("keyword") String keyword) throws IOException {
        Boolean aBoolean = iContentService.parseContent(keyword);
        return aBoolean.toString();
    }

    @GetMapping("/search/{keyword}/{page}/{rows}")
    public List<Map<String, Object>> searchPage(@PathVariable("keyword") String keyword,
                                                @PathVariable("page") int page,
                                                @PathVariable("rows") int rows) throws IOException {
        return iContentService.searchPage(keyword, page, rows);
    }

}
