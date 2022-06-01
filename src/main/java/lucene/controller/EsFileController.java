package lucene.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lucene.pojo.FileMessage;
import lucene.service.EsFileService;
import lucene.service.EsFileServiceImpl;
import lucene.util.EsUtil;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

@RestController
@RequestMapping("/es")
public class EsFileController {

    //高亮查询
    @GetMapping("/file")
    public Map file(@RequestParam String file) {
        Map<Object, Object> repMap = new HashMap<>();
        SearchSourceBuilder srb = new SearchSourceBuilder();
        //srb.query(QueryBuilders.matchPhraseQuery("attachment.content", file).analyzer("ik_smart"));//smart max_word
        srb.query(QueryBuilders.matchPhraseQuery("attachment.content", file));//smart max_word
        List<Map> pdftest = EsUtil.selectDocumentListHighLight("fileindex", srb, FileMessage.class, "attachment.content");
        repMap.put("result",pdftest);
        repMap.put("total",pdftest.size());
        return repMap;
    }

    //ES全文检索
    @GetMapping("/allFile")
    public List<Map<String, Object>> eSearch(String msg) throws UnknownHostException {
//
        List<Map<String, Object>> matchRsult = new LinkedList<Map<String, Object>>();
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //因为我这边实际业务需要其他字段的查询，所以进行查询的字段就比较，如果只是查询文档中内容的话，打开注释的代码，然后注释掉这行代码
        //builder.query(QueryBuilders.multiMatchQuery(msg,"attachment.content","name","sfName","createBy").analyzer("ik_smart"));

        builder.query(QueryBuilders.matchQuery("attachment.content", msg).analyzer("ik_smart"));
        SearchResponse searchResponse = EsUtil.selectDocument("fileindex", builder);
        SearchHits hits = searchResponse.getHits();

        for (SearchHit hit : hits.getHits()) {
            hit.getSourceAsMap().put("msg", "");
            matchRsult.add(hit.getSourceAsMap());
            // System.out.println(hit.getSourceAsString());
        }
        System.out.println("over in the main");

        return matchRsult;
    }



    public static void main(String[] args) throws IOException {
        File file = new File("/Users/huahuajun/Downloads/ingest-attachment-7.10.1");

        /*InputStream inputStream;
        IndexRequest request;
        try {
            inputStream = new FileInputStream(file);
            byte[] fileByteStream = IOUtils.toByteArray(inputStream);
            String base64String = new String(Base64.getEncoder().encodeToString(fileByteStream).getBytes(), "UTF-8");
            inputStream.close();
            Map attachmentMap = new HashMap();
            attachmentMap.put("data", base64String);
            attachmentMap.put("fileName", "四个空格-https://www.4spaces.org");
            String jsonString = JSONObject.toJSONString(attachmentMap);
            request = new IndexRequest("fileindex");
            request.id(UUID.randomUUID().toString());
            request.setPipeline("single_attachment");
            request.source(jsonString, XContentType.JSON);
            IndexResponse response = EsUtil.client.index(request, RequestOptions.DEFAULT);
            System.out.println(response);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ElasticsearchException e) {
            e.getDetailedMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        if (!file.exists()) {
            System.out.println("找不到文件");
        }
        FileMessage fileM = new FileMessage();
        byte[] buffer = null;
        try {
            long fileSize = file.length();
            if (fileSize > Integer.MAX_VALUE) {
                System.out.println("file too big...");
                return;
            }
            FileInputStream fi = new FileInputStream(file);
            buffer = new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            // 确保所有数据均被读取
            if (offset != buffer.length) {
                System.out.println("Could not completely read file");
            }
            fi.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
            byte[] bytes = buffer;
            String base64 = Base64.getEncoder().encodeToString(bytes);
            fileM.setId("1");
            fileM.setName(file.getName());
            fileM.setData(base64);
            IndexRequest indexRequest = new IndexRequest("fileindex");
            //上传同时，使用attachment pipline进行提取文件
            indexRequest.source(JSON.toJSONString(fileM), XContentType.JSON);
            indexRequest.setPipeline("attachment");
            IndexResponse indexResponse = EsUtil.client.index(indexRequest, RequestOptions.DEFAULT);
            System.out.println(indexResponse);
        }

}
