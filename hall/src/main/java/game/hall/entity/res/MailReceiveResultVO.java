package game.hall.entity.res;

import game.common.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class MailReceiveResultVO {
    private User user;

    private List<MailAttachmentVO> attachments;
}
