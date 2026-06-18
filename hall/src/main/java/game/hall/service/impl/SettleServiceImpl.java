package game.hall.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import game.common.entity.SettleRecordQueueDTO;
import game.hall.entity.res.SettleRecordVO;
import game.hall.mybatis.domain.DbSettleRecord;
import game.hall.mybatis.mapper.DbSettleRecordMapper;
import game.hall.mybatis.service.DbSettleRecordService;
import game.hall.service.SettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettleServiceImpl implements SettleService {
    @Autowired
    DbSettleRecordService dbSettleRecordService;
    @Autowired
    private DbSettleRecordMapper dbSettleRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveSettleRecord(SettleRecordQueueDTO dto) {
        if(!dto.getSettlePlayers().isEmpty()){
            List<DbSettleRecord> records = dto.getSettlePlayers().stream()
                    .map(item -> {
                        DbSettleRecord record = new DbSettleRecord();
                        record.setRoomId(dto.getRoomId());
                        record.setRoundId(dto.getRoundId());
                        record.setUserId(item.getUserId());
                        record.setSeatId(item.getSeatId());
                        record.setBankerUserId(dto.getBankerUserId());
                        record.setBankerSeat(dto.getBankerSeat());
                        record.setWin(item.getWin());
                        record.setBetAmount(item.getBetAmount());
                        record.setWinAmount(item.getWinAmount());
                        record.setAfterGold(item.getAfterGold());
                        record.setCards(JSONUtil.toJsonStr(item.getCards()));
                        record.setCardTypeName(item.getCardTypeName());
                        record.setSettleDesc(item.getSettleDesc());
                        record.setSettleTime(dto.getSettleTime());
                        return record;
                    })
                    .collect(Collectors.toList());
            dbSettleRecordService.getBaseMapper().insert(records);
        }
    }


    @Override
    public IPage<SettleRecordVO> page(
            Long userId,
            Integer pageNo,
            Integer pageSize
    ) {

        Page<DbSettleRecord> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<DbSettleRecord> wrapper =
                Wrappers.lambdaQuery();

        wrapper.eq(DbSettleRecord::getUserId, userId)
                .orderByDesc(DbSettleRecord::getSettleTime);
        Page<DbSettleRecord> result = dbSettleRecordMapper.selectPage(page, wrapper);
        Page<SettleRecordVO> voPage =
                new Page<>(
                        result.getCurrent(),
                        result.getSize(),
                        result.getTotal()
                );

        voPage.setRecords(
                result.getRecords()
                        .stream()
                        .map(this::convert)
                        .toList()
        );

        return voPage;
    }

    private SettleRecordVO convert(
            DbSettleRecord record
    ) {
        SettleRecordVO vo = new SettleRecordVO();

        vo.setRoundId(record.getRoundId());
        vo.setWin(record.getWin());

        vo.setBetAmount(record.getBetAmount());
        vo.setWinAmount(record.getWinAmount());

        vo.setCardTypeName(record.getCardTypeName());
        vo.setSettleDesc(record.getSettleDesc());

        vo.setCards(record.getCards());

        vo.setSettleTime(record.getSettleTime());

        return vo;
    }
}
