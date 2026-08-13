package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.DbMailUser;
import game.hall.mybatis.service.DbMailUserService;
import game.hall.mybatis.mapper.DbMailUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_mail_user】的数据库操作Service实现
* @createDate 2026-08-12 15:04:07
*/
@Service
public class DbMailUserServiceImpl extends ServiceImpl<DbMailUserMapper, DbMailUser>
    implements DbMailUserService{

}




