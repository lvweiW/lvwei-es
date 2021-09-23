package com.lvwei.lvweielasticjd.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lvwei.lvweielasticjd.constants.EsConstants;
import com.lvwei.lvweielasticjd.model.Content;
import com.lvwei.lvweielasticjd.service.IContentService;
import com.lvwei.lvweielasticjd.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lvwei
 * @date 2021/9/22
 * @description TODO
 */
@Service
public class ContentServiceImpl implements IContentService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean parseContent(String keyword) throws IOException {
        List<Content> contents = HtmlParseUtil.parseJD(keyword);

        BulkRequest request = new BulkRequest();

        request.timeout(TimeValue.timeValueSeconds(2));

        for (Content content : contents) {
            request.add(new IndexRequest(EsConstants.Index).source(JSONObject.toJSONString(content), XContentType.JSON));
        }

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);

        return !response.hasFailures();
    }


    @Override
    public List<Map<String, Object>> searchPage(String keyword, int page, int rows) throws IOException {
        if (page <= 1) {
            page = 1;
        }

        SearchRequest request = new SearchRequest(EsConstants.Index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //条件
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(60));

        //分页
        searchSourceBuilder.from(page);
        searchSourceBuilder.size(rows);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false); //多个高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.query(termQueryBuilder);

        request.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);

        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            //解析高亮的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            if (title != null) {
                Text[] fragments = title.fragments();
                String newTitle = "";
                for (Text fragment : fragments) {
                    newTitle += fragment;
                }
                sourceAsMap.put("title", newTitle);
            }

            list.add(sourceAsMap);
        }
        return list;
    }
}
