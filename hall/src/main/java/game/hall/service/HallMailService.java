package game.hall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import game.hall.entity.res.MailReceiveResultVO;
import game.hall.entity.res.MailVO;

public interface HallMailService {
    IPage<MailVO> page(Long userId, Integer pageNo, Integer pageSize);

    Integer unreadCount(Long userId);

    MailVO read(Long userId, Long mailId);

    MailReceiveResultVO receive(Long userId, Long mailId);

    MailReceiveResultVO receiveAll(Long userId);

    void delete(Long userId, Long mailId);
}
