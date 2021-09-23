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

    //测试创建索引
    @Test
    void createIndex() throws IOException {
        //1、创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(index);
        //2、执行请求
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(createIndexResponse);
        assertTrue(createIndexResponse.isAcknowledged());
    }

    //判断索引是否存在
    @Test
    void getIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        assertTrue(exists);
    }

    //删除索引
    @Test
    void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        assertTrue(delete.isAcknowledged());
    }

    //测试添加文档
    @Test
    void addDco() throws IOException {
        User user = User.builder().userId(1L).userName("张三丰").age(100).remark("武学宗师").build();
        IndexRequest request = new IndexRequest("lvwei123");

        //规则
        request.id(user.getUserId().toString());
        request.timeout(TimeValue.timeValueSeconds(1));

        request.source(JSONObject.toJSONString(user), XContentType.JSON);

        IndexResponse index = client.index(request, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSONString(index));
        assertEquals(index.getId(), "1");
    }

    //获取文档，判断是否存在
    @Test
    void existDoc() throws IOException {
        GetRequest request = new GetRequest(index, "1");

        //不获取返回值的上下文了
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

        User user = User.builder().remark("武学宗师，德玛西亚").build();


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
        users.add(User.builder().userId(1L).userName("张三丰").age(100).remark("武学宗师1").build());
        users.add(User.builder().userId(2L).userName("令狐冲").age(20).remark("武学宗师2").build());
        users.add(User.builder().userId(3L).userName("张无忌").age(30).remark("武学宗师3").build());
        users.add(User.builder().userId(4L).userName("石破天").age(40).remark("武学宗师4").build());
        users.add(User.builder().userId(5L).userName("袁承志").age(50).remark("武学宗师5").build());
        users.add(User.builder().userId(6L).userName("王重阳").age(60).remark("武学宗师6").build());
        users.add(User.builder().userId(7L).userName("胡一刀").age(100).remark("武学宗师7").build());

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

        //term 需要设置type 为keyword，es会为每个中文都进行切割
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("userName.keyword", "王重阳");


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