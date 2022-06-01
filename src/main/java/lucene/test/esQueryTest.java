package lucene.test;

import lucene.pojo.FileMessage;
import lucene.util.EsUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;
import java.util.Map;

public class esQueryTest {

    public static void main(String[] args) {
        SearchSourceBuilder srb = new SearchSourceBuilder();
        srb.query(QueryBuilders.matchQuery("attachment.content", "阅云台青年项目介绍").analyzer("ik_smart"));
        List<Map> pdftest = EsUtil.selectDocumentListHighLight("pdftest", srb, FileMessage.class, "attachment.content");
        System.out.println(pdftest);
    }

}
