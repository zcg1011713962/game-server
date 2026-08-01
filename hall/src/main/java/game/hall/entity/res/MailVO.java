package game.hall.entity.res;

import lombok.Data;

import java.util.List;

@Data
public class MailVO {
    private Long mailId;

    private Integer mailType;

    private String title;

    private String content;

    private String sender;

    private Long startTime;

    private Long expireTime;

    private Long createTime;

    private Integer readStatus;

    private Integer receiveStatus;

    private Integer deleteStatus;

    private Boolean hasAttachment;

    private List<MailAttachmentVO> attachments;
}
