package lucene.pojo;

import java.util.List;
import java.util.Map;

public class FileMessage {
    String id; //用于存储文件id
    String name; //文件名
    String type; //文件的type，pdf，word，or txt
    String content; //文件转化成base64编码后所有的内容。
    String data;
    String fileName;
    Map attachment;
    List<Map> attachments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map getAttachment() {
        return attachment;
    }

    public void setAttachment(Map attachment) {
        this.attachment = attachment;
    }

    public List<Map> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Map> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", data='" + data + '\'' +
                ", fileName='" + fileName + '\'' +
                ", attachment=" + attachment +
                ", attachments=" + attachments +
                '}';
    }
}
