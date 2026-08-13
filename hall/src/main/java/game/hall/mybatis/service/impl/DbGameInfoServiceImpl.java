package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.DbGameInfo;
import game.hall.mybatis.service.DbGameInfoService;
import game.hall.mybatis.mapper.DbGameInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_game_info(游戏基础配置)】的数据库操作Service实现
* @createDate 2026-08-12 15:04:17
*/
@Service
public class DbGameInfoServiceImpl extends ServiceImpl<DbGameInfoMapper, DbGameInfo>
    implements DbGameInfoService{

}




