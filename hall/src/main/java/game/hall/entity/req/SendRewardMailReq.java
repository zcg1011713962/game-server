package game.hall.entity.req;

import lombok.Data;

import java.util.List;

@Data
public class SendRewardMailReq {
    private Long userId;

    private String title;

    private String content;

    private String sender;

    private Integer expireDays;

    private List<Attachment> attachments;

    @Data
    public static class Attachment {
        private Integer itemType;

        private Integer itemId;

        private Long itemCount;
    }
}
