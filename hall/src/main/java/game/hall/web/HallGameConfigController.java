package game.hall.web;

import game.common.constant.ErrorCode;
import game.common.protocol.ServerMsg;
import game.hall.service.HallGameConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hall")
public class HallGameConfigController {

    @Autowired
    private HallGameConfigService hallGameConfigService;

    @PostMapping("/game-list")
    public ServerMsg gameList() {
        try {
            return ServerMsg.ok(hallGameConfigService.getGameList());
        } catch (Exception e) {
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }
}
