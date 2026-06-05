package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.DbUser;
import game.hall.mybatis.service.DbUserService;
import game.hall.mybatis.mapper.DbUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_user(用户表)】的数据库操作Service实现
* @createDate 2026-06-05 18:20:05
*/
@Service
public class DbUserServiceImpl extends ServiceImpl<DbUserMapper, DbUser>
    implements DbUserService{

}




