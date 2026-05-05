package game.hall.web;

import game.common.entity.req.RemoveRoomReq;
import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import game.hall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoomController {
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/room/remove")
    public ServerMsg remove(RemoveRoomReq req) {

        redisUtil.convertAndSend("room:remove", req);
        return ServerMsg.ok();
    }
}
