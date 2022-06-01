package lucene.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lucene.util.EsUtil;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.*;
import java.util.*;

public class esTest {

    public static void main(String[] args) {

        List<File> files = new ArrayList<>();
        //File file1 = new File("/Users/huahuajun/IdeaProjects/es-spring-test/src/main/java/lucene/controller/EsFileController.java");
        //File file2 = new File("/Users/huahuajun/IdeaProjects/es-spring-test/src/main/java/lucene/controller/EsFileController.java");
        //File file3 = new File("/Users/huahuajun/IdeaProjects/es-spring-test/src/main/java/lucene/pojo/FileMessage.java");
        //File file4 = new File("/Users/huahuajun/IdeaProjects/es-spring-test/src/main/java/lucene/dao/副本0527保利阅云台户型海报.docx");
        //File file5 = new File("/Users/huahuajun/IdeaProjects/es-spring-test/src/main/java/lucene/dao/副本0527串词V1(1).docx");
        //File file6 = new File("/Users/huahuajun/IdeaProjects/es-spring-test/src/main/java/lucene/dao/副本0526保利阅云台介绍.docx");
        File file7 = new File("/Users/huahuajun/Downloads/副本0527保利阅云台户型海报.docx");
        File file8 = new File("/Users/huahuajun/Downloads/副本0527串词V1(1).docx");
        File file9 = new File("/Users/huahuajun/Downloads/副本0526保利阅云台介绍.docx");
        //long timeout = 1000;
        files.add(file7);
        files.add(file8);
        files.add(file9);
        simplePatchInsert(files);
        //simplePatchInsertByattachments(files);
        /*SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        List<FileMessage> fileindex = EsUtil.selectDocumentList("fileindex", searchSourceBuilder, FileMessage.class);
        System.out.println(fileindex)*/;

    }

    //批量上传文件  attachment
    public static void simplePatchInsert(List<File> files){
        List<Map> dataList = new ArrayList<>();
        long timeout = 100000;
        try {
            for (File file:files){
                InputStream inputStream = new FileInputStream(file);
                byte[] fileByteStream = IOUtils.toByteArray(inputStream);
                String base64String = new String(Base64.getEncoder().encodeToString(fileByteStream).getBytes(), "UTF-8");
                inputStream.close();
                Map attachmentMap = new HashMap();
                attachmentMap.put("data", base64String);
                attachmentMap.put("fileName", file.getName());
                dataList.add(attachmentMap);
            }

            BulkRequest bulkRequest = new BulkRequest();
            //bulkRequest.timeout(TimeValue.timeValueSeconds(timeout));
            if (dataList != null && dataList.size() > 0) {
                for (Object obj : dataList) {
                    IndexRequest indexRequest = new IndexRequest("fileindex");
                    //上传同时，使用attachment pipline进行提取文件
                    indexRequest.source(JSON.toJSONString(obj), XContentType.JSON);
                    indexRequest.setPipeline("test_attachment");
                    bulkRequest.add(
                            indexRequest
                    );
                }
                BulkResponse response = EsUtil.client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println(response.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ElasticsearchException e) {
            e.getDetailedMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////批量上传文件  attachments
    public static void simplePatchInsertByattachments(List<File> files){
        List<Map> dataList = new ArrayList<>();
        try {
            for (File file:files){
                InputStream inputStream = new FileInputStream(file);
                byte[] fileByteStream = IOUtils.toByteArray(inputStream);
                String base64String = new String(Base64.getEncoder().encodeToString(fileByteStream).getBytes(), "UTF-8");
                Map<Object, Object> map = new HashMap<>();
                map.put("data",base64String);
                map.put("fileName",file.getName());
                dataList.add(map);
                inputStream.close();
            }
            Map attachmentMap = new HashMap();
            attachmentMap.put("attachments", dataList);
            attachmentMap.put("fileName", "多文件上传测试");


            BulkRequest bulkRequest = new BulkRequest();
            //bulkRequest.timeout(TimeValue.timeValueSeconds(timeout));
            String jsonString = JSONObject.toJSONString(attachmentMap);
            IndexRequest request = new IndexRequest("pdftest");
            request.id(UUID.randomUUID().toString());
            request.setPipeline("huahua_attachment");
            request.source(jsonString, XContentType.JSON);
            IndexResponse response = EsUtil.client.index(request, RequestOptions.DEFAULT);
            System.out.println(response);
            }
         catch (IOException e) {
            e.printStackTrace();
        }
    }
}
