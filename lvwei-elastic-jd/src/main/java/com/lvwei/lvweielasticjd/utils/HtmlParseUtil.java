package com.lvwei.lvweielasticjd.utils;

import com.lvwei.lvweielasticjd.model.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lvwei
 * @date 2021/9/22
 * @description 解析网页工具类
 */
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        HtmlParseUtil.parseJD("机械键盘").forEach(System.out::println);
    }

    public static List<Content> parseJD(String keywords) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keywords;

        //解析网页
        Document document = Jsoup.parse(new URL(url), 30000);

        Element element = document.getElementById("J_goodsList");
        Elements li = element.getElementsByTag("li");
        List<Content> contents = new ArrayList<>();

        for (Element el : li) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            contents.add(Content.builder().title(title).price(price).img(img).build());
        }
        return contents;
    }
}
