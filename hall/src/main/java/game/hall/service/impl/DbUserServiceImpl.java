package game.hall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.domain.DbUser;
import game.hall.service.DbUserService;
import game.hall.mapper.DbUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_user】的数据库操作Service实现
* @createDate 2026-04-27 16:12:42
*/
@Service
public class DbUserServiceImpl extends ServiceImpl<DbUserMapper, DbUser>
    implements DbUserService{

}




