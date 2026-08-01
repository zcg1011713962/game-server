package game.hall.entity.res;

import lombok.Data;

@Data
public class MailAttachmentVO {
    private Long id;

    private Long mailId;

    private Integer itemType;

    private Integer itemId;

    private Long itemCount;
}
