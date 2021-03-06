package com.lvwei.lvweielastic.com.lvwei.controller;

import com.alibaba.fastjson.JSONObject;
import com.lvwei.lvweielastic.com.lvwei.model.User;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestControllerTest {

    @Autowired
    private RestHighLevelClient client;

    private final static String index = "lvwei123";

    //??????????????????
    @Test
    void createIndex() throws IOException {
        //1?????????????????????
        CreateIndexRequest request = new CreateIndexRequest(index);
        //2???????????????
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(createIndexResponse);
        assertTrue(createIndexResponse.isAcknowledged());
    }

    //????????????????????????
    @Test
    void getIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        assertTrue(exists);
    }

    //????????????
    @Test
    void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        assertTrue(delete.isAcknowledged());
    }

    //??????????????????
    @Test
    void addDco() throws IOException {
        User user = User.builder().userId(1L).userName("?????????").age(100).remark("????????????").build();
        IndexRequest request = new IndexRequest("lvwei123");

        //??????
        request.id(user.getUserId().toString());
        request.timeout(TimeValue.timeValueSeconds(1));

        request.source(JSONObject.toJSONString(user), XContentType.JSON);

        IndexResponse index = client.index(request, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSONString(index));
        assertEquals(index.getId(), "1");
    }

    //?????????????????????????????????
    @Test
    void existDoc() throws IOException {
        GetRequest request = new GetRequest(index, "1");

        //?????????????????????????????????
//        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        assertTrue(exists);
    }

    @Test
    void getDocInfo() throws IOException {
        GetRequest request = new GetRequest(index, "1");

        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String sourceAsString = response.getSourceAsString();
        System.out.println(sourceAsString);
    }

    @Test
    void updateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest(index, "1");

        request.timeout(TimeValue.timeValueSeconds(1));

        User user = User.builder().remark("???????????????????????????").build();


        request.doc(JSONObject.toJSONString(user), XContentType.JSON);

        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        System.out.println(response);

    }

    @Test
    void deleteDoc() throws IOException {
        DeleteRequest request = new DeleteRequest(index, "1");

        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response);

        assertEquals(response.getResult(), DocWriteResponse.Result.DELETED);
    }

    @Test
    void bulkRequest() throws IOException {
        BulkRequest request = new BulkRequest();

        ArrayList<User> users = new ArrayList<>();
        users.add(User.builder().userId(1L).userName("?????????").age(100).remark("????????????1").build());
        users.add(User.builder().userId(2L).userName("?????????").age(20).remark("????????????2").build());
        users.add(User.builder().userId(3L).userName("?????????").age(30).remark("????????????3").build());
        users.add(User.builder().userId(4L).userName("?????????").age(40).remark("????????????4").build());
        users.add(User.builder().userId(5L).userName("?????????").age(50).remark("????????????5").build());
        users.add(User.builder().userId(6L).userName("?????????").age(60).remark("????????????6").build());
        users.add(User.builder().userId(7L).userName("?????????").age(100).remark("????????????7").build());

        for (User user : users) {
            request.add(new IndexRequest(index).id(user.getUserId().toString())
                    .source(JSONObject.toJSONString(user), XContentType.JSON));
        }

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);

        System.out.println(response);

        assertFalse(response.hasFailures());
    }

    @Test
    void searchRequest() throws IOException {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //term ????????????type ???keyword???es?????????????????????????????????
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("userName.keyword", "?????????");


        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(10));

        HighlightBuilder highlightBuilder = new HighlightBuilder();

        sourceBuilder.highlighter(highlightBuilder);

        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSONString(response));
        System.out.println("================");
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }

}