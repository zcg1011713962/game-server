package game.hall.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import game.common.entity.CardInfo;
import game.common.entity.SettleRecordQueueDTO;
import game.hall.entity.req.TestSettleRecordReq;
import game.hall.entity.res.SettleRecordVO;
import game.hall.exception.HallException;
import game.hall.mybatis.domain.DbUser;
import game.hall.mybatis.domain.DbSettleRecord;
import game.hall.mybatis.mapper.DbSettleRecordMapper;
import game.hall.mybatis.service.DbSettleRecordService;
import game.hall.mybatis.service.DbUserService;
import game.hall.service.SettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
    @Autowired
    private DbUserService dbUserService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insertTestRecord(Long operatorUserId, TestSettleRecordReq req) {
        validateTestRecordReq(req);

        Long userId = req.getUserId();
        Long bankerUserId = req.getBankerUserId() == null ? userId : req.getBankerUserId();
        Long gameId = req.getGameId() == null ? 1L : req.getGameId();
        Integer roomId = req.getRoomId();
        Long baseBetAmount = req.getBetAmount() == null ? 10L : req.getBetAmount();
        long startTime = req.getStartTime() == null ? System.currentTimeMillis() : req.getStartTime();

        List<TestSettleRecordReq.Round> rounds = req.getRounds();
        if (rounds == null || rounds.isEmpty()) {
            rounds = buildMockRounds(req.getRoundCount(), baseBetAmount, startTime);
        }

        List<DbSettleRecord> records = new ArrayList<>();
        for (int i = 0; i < rounds.size(); i++) {
            TestSettleRecordReq.Round round = rounds.get(i);
            Long roundId = round.getRoundId() == null ? (long) i + 1 : round.getRoundId();
            Long settleTime = round.getSettleTime() == null ? startTime + i * 180000L : round.getSettleTime();
            Long betAmount = round.getBetAmount() == null ? baseBetAmount : round.getBetAmount();
            Long winAmount = round.getWinAmount() == null ? 0L : round.getWinAmount();

            records.add(buildRecord(
                    gameId,
                    roomId,
                    roundId,
                    userId,
                    1,
                    bankerUserId,
                    0,
                    resolveWin(round.getWin(), winAmount),
                    betAmount,
                    winAmount,
                    0L,
                    round.getCards(),
                    round.getCardTypeName(),
                    round.getSettleDesc(),
                    settleTime
            ));

            if (!userId.equals(bankerUserId)) {
                records.add(buildRecord(
                        gameId,
                        roomId,
                        roundId,
                        bankerUserId,
                        0,
                        bankerUserId,
                        0,
                        resolveWin(null, -winAmount),
                        betAmount,
                        -winAmount,
                        0L,
                        round.getBankerCards(),
                        round.getBankerCardTypeName(),
                        winAmount > 0 ? "庄家失败" : winAmount < 0 ? "庄家获胜" : "平局",
                        settleTime
                ));
            }
        }

        records.forEach(record -> dbSettleRecordService.getBaseMapper().insert(record));
        return records.size();
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

    private void validateTestRecordReq(TestSettleRecordReq req) {
        if (req == null) {
            throw new HallException("请求参数不能为空");
        }
        if (req.getUserId() == null || req.getUserId() <= 0) {
            throw new HallException("用户ID不能为空");
        }
        if (req.getRoomId() == null || req.getRoomId() <= 0) {
            throw new HallException("房间ID不能为空");
        }
        if (dbUserService.getById(req.getUserId()) == null) {
            throw new HallException("用户不存在");
        }
        if (req.getBankerUserId() != null && dbUserService.getById(req.getBankerUserId()) == null) {
            throw new HallException("庄家用户不存在");
        }
        if (req.getRoundCount() != null && req.getRoundCount() > 100) {
            throw new HallException("测试局数不能超过100");
        }
    }

    private List<TestSettleRecordReq.Round> buildMockRounds(Integer roundCount, Long betAmount, long startTime) {
        int count = roundCount == null || roundCount <= 0 ? 8 : Math.min(roundCount, 100);
        List<String> cardTypes = Arrays.asList("双天", "4点", "8点", "3点", "5点", "7点", "对子", "地牌");
        List<TestSettleRecordReq.Round> rounds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            long winAmount = (i % 3 == 1) ? 0L : (i % 2 == 0 ? betAmount : -betAmount);
            TestSettleRecordReq.Round round = new TestSettleRecordReq.Round();
            round.setRoundId((long) i + 1);
            round.setWinAmount(winAmount);
            round.setBetAmount(betAmount);
            round.setWin(resolveWin(null, winAmount));
            round.setCardTypeName(cardTypes.get(i % cardTypes.size()));
            round.setBankerCardTypeName(cardTypes.get((i + 2) % cardTypes.size()));
            round.setSettleDesc(winAmount > 0 ? "小赢" : winAmount < 0 ? "失败" : "平局");
            round.setSettleTime(startTime + i * 180000L);
            round.setCards(buildMockCards(i));
            round.setBankerCards(buildMockCards(i + 2));
            rounds.add(round);
        }

        return rounds;
    }

    private List<CardInfo> buildMockCards(int index) {
        int first = index % 10 + 1;
        int second = (index + 5) % 10 + 1;
        return Arrays.asList(
                new CardInfo(first, "牌" + first, first),
                new CardInfo(second, "牌" + second, second)
        );
    }

    private DbSettleRecord buildRecord(
            Long gameId,
            Integer roomId,
            Long roundId,
            Long userId,
            Integer seatId,
            Long bankerUserId,
            Integer bankerSeat,
            Integer win,
            Long betAmount,
            Long winAmount,
            Long afterGold,
            List<CardInfo> cards,
            String cardTypeName,
            String settleDesc,
            Long settleTime
    ) {
        DbSettleRecord record = new DbSettleRecord();
        record.setGameId(gameId);
        record.setRoomId(roomId == null ? null : roomId.longValue());
        record.setRoundId(roundId);
        record.setUserId(userId);
        record.setSeatId(seatId);
        record.setBankerUserId(bankerUserId);
        record.setBankerSeat(bankerSeat);
        record.setWin(win);
        record.setBetAmount(betAmount);
        record.setWinAmount(winAmount);
        record.setAfterGold(afterGold);
        record.setCards(JSONUtil.toJsonStr(cards == null ? buildMockCards(0) : cards));
        record.setCardTypeName(cardTypeName == null ? "测试牌型" : cardTypeName);
        record.setSettleDesc(settleDesc == null ? "测试战绩" : settleDesc);
        record.setSettleTime(settleTime);
        record.setCreateTime(new Date());
        return record;
    }

    private Integer resolveWin(Integer win, Long winAmount) {
        if (win != null) {
            return win;
        }
        long amount = winAmount == null ? 0L : winAmount;
        return amount > 0 ? 2 : amount < 0 ? 0 : 1;
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
