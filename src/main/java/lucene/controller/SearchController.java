package lucene.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lucene.pojo.FileMessage;
import lucene.util.EsUtil;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private RestHighLevelClient restHighLevelClient;

    /*
     * @Qualifier("elasticsearchClient") 主要是针对容器中有多个 bean, 可以通过 @Qualifier 将那个bean注入进来
     * @param elasticsearchClient
     */
    /*public NewsController(@Qualifier("estHighLevelClient") RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }*/

    @GetMapping("/tips")
    public List<FileMessage> prefixTip(@RequestParam(name = "tips") String tips,
                                       @RequestParam(name = "pageNum") int pageNum,
                                       @RequestParam(name = "size") String size
    ) throws IOException {
       /* Request request = new Request("GET", "news/_search");

        String suggestJson = String.format("{" +
                "  \"_source\": false, " +
                "  \"suggest\": {" +
                "    \"news_tag_suggest\": {" +
                "      \"prefix\": \"%s\"," +
                "      \"completion\": {" +
                "        \"field\": \"tags\"," +
                "        \"size\": 10," +
                "        \"skip_duplicates\":true" +
                "      }" +
                "    }" +
                "  }" +
                "}", tips);*/
        Request request = new Request("GET", "/pdftest/_search");
        int from = 0;
        if (0!=pageNum) {
            from = (pageNum-1)*Integer.parseInt(size);
        }

        String suggestJson = String.format("{\n" +
                "\"from\": " +
                from + "," +
                "\"size\": " +
                size + "," +
                "  \"query\": {\n" +
                "    \"match\": {\n" +
                "      \"attachment.content\": " +
                tips +
                "    }\n" +
                "  }\n" +
                "}");

        request.setJsonEntity(suggestJson);

        // 发送请求, 返回结果
        Response response = EsUtil.client.getLowLevelClient().performRequest(request);

        // 返回的json字符串, 就是于 kibana中得到的json数据
        String responseJson = EntityUtils.toString(response.getEntity());

        JSONObject jsonObject = JSONObject.parseObject(responseJson);
        // 获取 kibana 中的 "suggest" 对应的json对象
       /* JSONObject suggest = jsonObject.getJSONObject("suggest");

        // 就是搜索的信息与结构
        JSONObject resultInfo = suggest.getJSONArray("news_tag_suggest").getJSONObject(0);

        JSONArray options = resultInfo.getJSONArray("options");

        // 给定长度, 是一种优化的策略
        List<String> results = new ArrayList<>(options.size());

        for (int i = 0; i < options.size(); i++) {
            // opt 就是我们需要的数据
            JSONObject opt = options.getJSONObject(i);

            results.add(opt.getString("text"));
        }
        return results;*/

        JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");

        List<FileMessage> results = new ArrayList<>();

        if (hits.size() > 0) {
            for (int i = 0; i < hits.size(); i++) {
                // 这是最终查询的每一条数据
                JSONObject data = hits.getJSONObject(i);
                FileMessage nm = new FileMessage();

                // 原始数据
                JSONObject originData = data.getJSONObject("_source");
                String idData = (String) data.get("_id");

                //
                nm.setId(idData);
                nm.setFileName(originData.getString("fileName"));


                JSONObject attachmentData = originData.getJSONObject("attachment");

                String content = (String) attachmentData.get("content");


                // 如果title中没有查询的高亮文本, 就从原始数据中取
                if (null != content) {
                    nm.setContent(content.toString());
                }

                results.add(nm);
            }
            return results;
        } else {
            return results;
        }
    }

    //分词查询高亮显示
    @GetMapping("/participleSearch")
    public List<FileMessage> participleSearch(String keyword) throws IOException {
        Request request = new Request("GET", "pdftest/_search");

            /*String suggestJson = String.format("{" +
                    "  \"_source\": [\"title\", \"content\", \"url\", \"id\"], " +
                    "  \"query\": {" +
                    "    \"multi_match\": {" +
                    "      \"query\": \"%s\"," +
                    "      \"fields\": [\"title\", \"content\"]" +
                    "    }" +
                    "  }," +
                    "  \"highlight\": {" +
                    "    \"pre_tags\": \"<span class='hl'>\", " +
                    "    \"post_tags\": \"</span>\", " +
                    "    \"fields\": {" +
                    "      \"title\": {}," +
                    "      \"content\": {}" +
                    "    }" +
                    "  }" +
                    "}", keyword);*/

        String suggestJson = String.format("{\n" +
                "  \"query\": {\n" +
                "    \"match\": {\n" +
                "      \"attachment.content\": " +
                keyword +
                "    }\n" +
                "  },\n" +
                "  \"highlight\": {\n" +
                "    \"pre_tags\": [\n" +
                "      \"<span style=color:green>\"\n" +
                "    ],\n" +
                "    \"post_tags\": [\n" +
                "      \"</span>\"\n" +
                "    ],\n" +
                "    \"fields\": {\n" +
                "      \"attachment.content\": {\n" +
                "        \"fragment_size\": 10000,\n" +
                "        \"number_of_fragments\": 0,\n" +
                "        \"fragment_offset\": 20\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");

        request.setJsonEntity(suggestJson);

        // 发送请求, 返回结果
        Response response = EsUtil.client.getLowLevelClient().performRequest(request);
// 返回的json字符串, 就是于 kibana中得到的json数据
        String responseJson = EntityUtils.toString(response.getEntity());

        JSONObject jsonObject = JSONObject.parseObject(responseJson);

        JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");

        List<FileMessage> results = new ArrayList<>();

        if (hits.size() > 0) {
            for (int i = 0; i < hits.size(); i++) {
                // 这是最终查询的每一条数据
                JSONObject data = hits.getJSONObject(i);
                FileMessage nm = new FileMessage();

                // 原始数据
                JSONObject originData = data.getJSONObject("_source");
                String idData = (String) data.get("_id");

                //
                nm.setId(idData);
                nm.setFileName(originData.getString("fileName"));

                // 高亮数据
                JSONObject highLightData = data.getJSONObject("highlight");

                // 获取title中高亮的文本碎片, 返回的是一个数组
                JSONArray titles = highLightData.getJSONArray("title");

                // 如果title中没有查询的高亮文本, 就从原始数据中取
                /*if (null == titles) {
                    nm.setTitle(originData.getString("title"));
                } else {
                    StringBuffer titleSb = new StringBuffer();
                    for (int j = 0; j < titles.size(); j++) {
                        titleSb.append(titles.getString(j));
                    }
                    nm.setTitle(titleSb.toString());
                }*/

                JSONArray contents = highLightData.getJSONArray("attachment.content");

                // 如果title中没有查询的高亮文本, 就从原始数据中取
                if (null == contents) {
                    nm.setContent(originData.getString("attachment.content"));
                } else {
                    StringBuffer contentSb = new StringBuffer();
                    for (int j = 0; j < contents.size(); j++) {
                        contentSb.append(contents.getString(j));
                    }
                    nm.setContent(contentSb.toString());
                }

                results.add(nm);
            }
            return results;
        } else {
            return results;
        }
    }

    //精确查询高亮显示
    @GetMapping("/accurateSearch")
    public List<FileMessage> search(String keyword) throws IOException {
        Request request = new Request("GET", "pdftest/_search");

        String suggestJson = String.format("{\n" +
                "  \"query\": {\n" +
                "  \"bool\": {\n" +
                "    \"must\": [\n" +
                "  {\n" +
                "    \"match_phrase\": {\n" +
                "      \"attachment.content\": " +
                keyword +
                "  }\n" +
                "  }\n" +
                "    ]\n" +
                "  }\n" +
                "  },\n" +
                "  \"highlight\": {\n" +
                "    \"pre_tags\": [\n" +
                "      \"<span style=color:green>\"\n" +
                "    ],\n" +
                "    \"post_tags\": [\n" +
                "      \"</span>\"\n" +
                "    ],\n" +
                "    \"fields\": {\n" +
                "      \"attachment.content\": {\n" +
                "        \"fragment_size\": 10000,\n" +
                "        \"number_of_fragments\": 0,\n" +
                "        \"fragment_offset\": 20\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");

        request.setJsonEntity(suggestJson);

        // 发送请求, 返回结果
        Response response = EsUtil.client.getLowLevelClient().performRequest(request);
// 返回的json字符串, 就是于 kibana中得到的json数据
        String responseJson = EntityUtils.toString(response.getEntity());

        JSONObject jsonObject = JSONObject.parseObject(responseJson);

        JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");

        List<FileMessage> results = new ArrayList<>();

        if (hits.size() > 0) {
            for (int i = 0; i < hits.size(); i++) {
                // 这是最终查询的每一条数据
                JSONObject data = hits.getJSONObject(i);
                FileMessage nm = new FileMessage();

                // 原始数据
                JSONObject originData = data.getJSONObject("_source");
                String idData = (String) data.get("_id");

                //
                nm.setId(idData);
                nm.setFileName(originData.getString("fileName"));

                // 高亮数据
                JSONObject highLightData = data.getJSONObject("highlight");

                // 获取title中高亮的文本碎片, 返回的是一个数组
                JSONArray titles = highLightData.getJSONArray("title");

                // 如果title中没有查询的高亮文本, 就从原始数据中取
                /*if (null == titles) {
                    nm.setTitle(originData.getString("title"));
                } else {
                    StringBuffer titleSb = new StringBuffer();
                    for (int j = 0; j < titles.size(); j++) {
                        titleSb.append(titles.getString(j));
                    }
                    nm.setTitle(titleSb.toString());
                }*/

                JSONArray contents = highLightData.getJSONArray("attachment.content");

                // 如果title中没有查询的高亮文本, 就从原始数据中取
                if (null == contents) {
                    nm.setContent(originData.getString("attachment.content"));
                } else {
                    StringBuffer contentSb = new StringBuffer();
                    for (int j = 0; j < contents.size(); j++) {
                        contentSb.append(contents.getString(j));
                    }
                    nm.setContent(contentSb.toString());
                }

                results.add(nm);
            }
            return results;
        } else {
            return results;
        }
    }


    public static void main(String[] args) {
        int answer = 42;
        String text = String.format("The answer is %d", answer);
        System.out.println(text);
    }

}


