package game.common.service;

import game.common.constant.RedisKeyConstants;
import game.common.entity.SettleRecordQueueDTO;
import game.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisSettleService {
    @Autowired
    private RedisUtil redisUtil;

    public void pushSettleRecord(SettleRecordQueueDTO dto) {
        redisUtil.rightPush(
                RedisKeyConstants.GAME_SETTLE_RECORD_QUEUE,
                dto
        );
    }

    public SettleRecordQueueDTO popSettleRecord() {
        return redisUtil.leftPop(
                RedisKeyConstants.GAME_SETTLE_RECORD_QUEUE,
                5,
                TimeUnit.SECONDS,
                SettleRecordQueueDTO.class
        );
    }

}
