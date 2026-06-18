package game.hall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import game.common.entity.SettleRecordQueueDTO;
import game.hall.entity.res.SettleRecordVO;

public interface SettleService {

    void saveSettleRecord(SettleRecordQueueDTO dto);

    IPage<SettleRecordVO> page(
            Long userId,
            Integer pageNo,
            Integer pageSize
    );
}
