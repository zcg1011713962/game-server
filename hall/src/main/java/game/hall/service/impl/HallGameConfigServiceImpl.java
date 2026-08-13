package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import game.hall.entity.resp.HallBannerVO;
import game.hall.entity.resp.HallGameEntryVO;
import game.hall.entity.resp.HallGameListResp;
import game.hall.mybatis.domain.DbGameInfo;
import game.hall.mybatis.domain.DbHallBanner;
import game.hall.mybatis.domain.DbHallGameEntry;
import game.hall.mybatis.mapper.DbGameInfoMapper;
import game.hall.mybatis.mapper.DbHallBannerMapper;
import game.hall.mybatis.mapper.DbHallGameEntryMapper;
import game.hall.service.HallGameConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HallGameConfigServiceImpl implements HallGameConfigService {

    private static final int ENABLED = 1;
    private static final int CLOSED = 0;

    @Autowired
    private DbHallBannerMapper hallBannerMapper;

    @Autowired
    private DbHallGameEntryMapper hallGameEntryMapper;

    @Autowired
    private DbGameInfoMapper gameInfoMapper;

    @Override
    public HallGameListResp getGameList() {
        HallGameListResp resp = new HallGameListResp();

        Map<Integer, DbGameInfo> gameInfoMap = gameInfoMapper.selectList(
                new LambdaQueryWrapper<DbGameInfo>()
                        .ne(DbGameInfo::getStatus, CLOSED)
        ).stream().collect(Collectors.toMap(DbGameInfo::getGameId, Function.identity(), (a, b) -> a));

        List<DbHallGameEntry> entries = hallGameEntryMapper.selectList(
                new LambdaQueryWrapper<DbHallGameEntry>()
                        .orderByAsc(DbHallGameEntry::getSort)
                        .orderByAsc(DbHallGameEntry::getId)
        );

        resp.setGames(entries.stream()
                .filter(entry -> entry.getGameId() != null)
                .filter(entry -> gameInfoMap.containsKey(entry.getGameId()))
                .map(entry -> toGameEntryVO(entry, gameInfoMap.get(entry.getGameId())))
                .collect(Collectors.toList()));

        resp.setBanner(loadBanner(gameInfoMap));

        return resp;
    }

    private HallBannerVO loadBanner(Map<Integer, DbGameInfo> gameInfoMap) {
        List<DbHallBanner> banners = hallBannerMapper.selectList(
                new LambdaQueryWrapper<DbHallBanner>()
                        .eq(DbHallBanner::getEnabled, ENABLED)
                        .orderByAsc(DbHallBanner::getSort)
                        .orderByAsc(DbHallBanner::getId)
        );

        return banners.stream()
                .filter(banner -> banner.getGameId() == null || gameInfoMap.containsKey(banner.getGameId()))
                .min(Comparator.comparing(DbHallBanner::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DbHallBanner::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toBannerVO)
                .orElse(null);
    }

    private HallGameEntryVO toGameEntryVO(DbHallGameEntry entry, DbGameInfo gameInfo) {
        HallGameEntryVO vo = new HallGameEntryVO();
        vo.setGameId(entry.getGameId());
        vo.setGameCode(gameInfo.getGameCode());
        vo.setGameName(gameInfo.getGameName());
        vo.setTitle(entry.getTitle());
        vo.setSubtitle(entry.getSubtitle());
        vo.setTag(entry.getTag());
        vo.setCoverAsset(entry.getCoverAsset());
        vo.setBgAsset(entry.getBgAsset());
        vo.setButtonAsset(entry.getButtonAsset());
        vo.setOnlineCount(defaultInt(entry.getOnlineCount(), 0));
        vo.setEnabled(isTrue(entry.getEnabled()) && isTrue(gameInfo.getStatus()));
        vo.setMatchEnabled(isTrue(entry.getMatchEnabled()));
        vo.setRoomEnabled(isTrue(entry.getRoomEnabled()));
        return vo;
    }

    private HallBannerVO toBannerVO(DbHallBanner banner) {
        HallBannerVO vo = new HallBannerVO();
        vo.setGameId(banner.getGameId());
        vo.setTitle(banner.getTitle());
        vo.setSubtitle(banner.getSubtitle());
        vo.setTagText(banner.getTagText());
        vo.setBgAsset(banner.getBgAsset());
        vo.setButtonAsset(banner.getButtonAsset());
        vo.setOnlineCount(defaultInt(banner.getOnlineCount(), 0));
        vo.setEnabled(isTrue(banner.getEnabled()));
        return vo;
    }

    private boolean isTrue(Integer value) {
        return Objects.equals(value, ENABLED);
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
