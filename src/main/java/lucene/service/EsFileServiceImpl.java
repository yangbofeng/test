package lucene.service;

import com.alibaba.fastjson.JSON;

import lucene.pojo.FileMessage;
import lucene.util.EsUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;


@Service
public class EsFileServiceImpl implements EsFileService {

    Log logger = LogFactory.getLog(EsFileServiceImpl.class);

    public void updateESFile(String filePath){
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("找不到文件");
        }
        FileMessage fileM = new FileMessage();
        try {
            byte[] bytes = getContent(file);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            fileM.setId("1");
            fileM.setName(file.getName());
            fileM.setContent(base64);
            IndexRequest indexRequest = new IndexRequest("fileindex");
            //上传同时，使用attachment pipline进行提取文件
            indexRequest.source(JSON.toJSONString(fileM), XContentType.JSON);
            indexRequest.setPipeline("attachment");
            IndexResponse indexResponse = EsUtil.client.index(indexRequest, RequestOptions.DEFAULT);
            logger.info("send to eSearch:" + file.getName());
            logger.info("send to eSeach results:" + indexResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件转base64
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] getContent(File file) throws IOException {

        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }

}
