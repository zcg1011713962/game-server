package game.hall.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import game.common.entity.CardInfo;
import game.common.entity.SettleRecordQueueDTO;
import game.hall.entity.res.SettleRecordVO;
import game.hall.mybatis.domain.DbSettleRecord;
import game.hall.mybatis.mapper.DbSettleRecordMapper;
import game.hall.mybatis.service.DbSettleRecordService;
import game.hall.service.SettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                        record.setGameId(dto.getGameId());
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
            Integer pageSize,
            Long gameId,
            Integer roomId
    ) {

        if (roomId == null) {
            return pageRoomSummary(userId, pageNo, pageSize, gameId);
        }

        Page<DbSettleRecord> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<DbSettleRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DbSettleRecord::getUserId, userId)
                .eq(DbSettleRecord::getRoomId, roomId);
        applyGameFilter(wrapper, gameId);
        wrapper.orderByAsc(DbSettleRecord::getRoomId)
                .orderByAsc(DbSettleRecord::getRoundId);

        Page<DbSettleRecord> result = dbSettleRecordMapper.selectPage(page, wrapper);
        Page<SettleRecordVO> voPage =
                new Page<>(
                        result.getCurrent(),
                        result.getSize(),
                        result.getTotal()
                );

        Map<Long, DbSettleRecord> bankerRecordMap = loadBankerRecordMap(roomId, result.getRecords());
        voPage.setRecords(
                result.getRecords()
                        .stream()
                        .map(record -> convert(record, bankerRecordMap.get(record.getRoundId())))
                        .sorted(Comparator.comparing(SettleRecordVO::getRoundId))
                        .toList()
        );

        return voPage;
    }

    private IPage<SettleRecordVO> pageRoomSummary(
            Long userId,
            Integer pageNo,
            Integer pageSize,
            Long gameId
    ) {
        LambdaQueryWrapper<DbSettleRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DbSettleRecord::getUserId, userId);
        applyGameFilter(wrapper, gameId);
        wrapper.orderByDesc(DbSettleRecord::getSettleTime);

        List<DbSettleRecord> records = dbSettleRecordMapper.selectList(wrapper);

        List<SettleRecordVO> summaries = records.stream()
                .collect(Collectors.groupingBy(DbSettleRecord::getRoomId))
                .entrySet()
                .stream()
                .map(this::convertRoomSummary)
                .sorted(Comparator.comparing(SettleRecordVO::getEndTime).reversed())
                .collect(Collectors.toList());

        long total = summaries.size();
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int fromIndex = Math.min((safePageNo - 1) * safePageSize, summaries.size());
        int toIndex = Math.min(fromIndex + safePageSize, summaries.size());

        Page<SettleRecordVO> voPage = new Page<>(safePageNo, safePageSize, total);
        voPage.setRecords(summaries.subList(fromIndex, toIndex));

        return voPage;
    }

    private void applyGameFilter(LambdaQueryWrapper<DbSettleRecord> wrapper, Long gameId) {
        if (gameId == null) {
            return;
        }

        if (gameId == 1L) {
            wrapper.and(item -> item.eq(DbSettleRecord::getGameId, gameId)
                    .or()
                    .isNull(DbSettleRecord::getGameId));
            return;
        }

        wrapper.eq(DbSettleRecord::getGameId, gameId);
    }

    private SettleRecordVO convertRoomSummary(
            Map.Entry<Long, List<DbSettleRecord>> entry
    ) {
        List<DbSettleRecord> records = entry.getValue();
        long startTime = records.stream()
                .mapToLong(record -> record.getSettleTime() == null ? 0L : record.getSettleTime())
                .min()
                .orElse(0L);
        long endTime = records.stream()
                .mapToLong(record -> record.getSettleTime() == null ? 0L : record.getSettleTime())
                .max()
                .orElse(0L);
        long winAmount = records.stream()
                .mapToLong(record -> record.getWinAmount() == null ? 0L : record.getWinAmount())
                .sum();
        long betAmount = records.stream()
                .mapToLong(record -> record.getBetAmount() == null ? 0L : record.getBetAmount())
                .sum();
        long roundCount = records.stream()
                .map(DbSettleRecord::getRoundId)
                .distinct()
                .count();
        long bankerCount = records.stream()
                .filter(record -> record.getUserId() != null && record.getUserId().equals(record.getBankerUserId()))
                .map(DbSettleRecord::getRoundId)
                .distinct()
                .count();

        SettleRecordVO vo = new SettleRecordVO();
        vo.setGameId(resolveSummaryGameId(records));
        vo.setRoomId(entry.getKey());
        vo.setRoundCount(roundCount);
        vo.setBankerCount(bankerCount);
        vo.setBetAmount(betAmount);
        vo.setWinAmount(winAmount);
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        vo.setSettleTime(endTime);
        vo.setDuration(Math.max(0L, endTime - startTime));
        vo.setWin(winAmount > 0 ? 2 : winAmount < 0 ? 0 : 1);
        vo.setCardTypeName("房间汇总");
        vo.setSettleDesc("共" + roundCount + "局");

        return vo;
    }

    private Long resolveSummaryGameId(List<DbSettleRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        return records.stream()
                .map(DbSettleRecord::getGameId)
                .filter(gameId -> gameId != null)
                .findFirst()
                .orElse(null);
    }

    private Map<Long, DbSettleRecord> loadBankerRecordMap(
            Integer roomId,
            List<DbSettleRecord> records
    ) {
        if (records == null || records.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> roundIds = records.stream()
                .map(DbSettleRecord::getRoundId)
                .distinct()
                .toList();

        LambdaQueryWrapper<DbSettleRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DbSettleRecord::getRoomId, roomId)
                .in(DbSettleRecord::getRoundId, roundIds);

        return dbSettleRecordMapper.selectList(wrapper)
                .stream()
                .filter(record -> record.getUserId() != null && record.getUserId().equals(record.getBankerUserId()))
                .collect(Collectors.toMap(
                        DbSettleRecord::getRoundId,
                        record -> record,
                        (left, right) -> left
                ));
    }

    private SettleRecordVO convert(DbSettleRecord record) {
        return convert(record, null);
    }

    private SettleRecordVO convert(
            DbSettleRecord record,
            DbSettleRecord bankerRecord
    ) {
        SettleRecordVO vo = new SettleRecordVO();

        vo.setGameId(record.getGameId());
        vo.setRoomId(record.getRoomId());
        vo.setRoundId(record.getRoundId());
        vo.setWin(record.getWin());

        vo.setBetAmount(record.getBetAmount());
        vo.setWinAmount(record.getWinAmount());

        vo.setCardTypeName(record.getCardTypeName());
        vo.setSettleDesc(record.getSettleDesc());

        vo.setCards(JSONUtil.toList(record.getCards(), CardInfo.class));
        if (bankerRecord != null) {
            vo.setBankerCardTypeName(bankerRecord.getCardTypeName());
            vo.setBankerCards(JSONUtil.toList(bankerRecord.getCards(), CardInfo.class));
        }

        vo.setSettleTime(record.getSettleTime());

        return vo;
    }
}
