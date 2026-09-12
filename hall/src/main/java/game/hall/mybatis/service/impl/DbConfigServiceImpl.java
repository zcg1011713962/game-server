package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.DbConfig;
import game.hall.mybatis.service.DbConfigService;
import game.hall.mybatis.mapper.DbConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_config(配置表)】的数据库操作Service实现
* @createDate 2026-09-11 18:51:58
*/
@Service
public class DbConfigServiceImpl extends ServiceImpl<DbConfigMapper, DbConfig>
    implements DbConfigService{

}




