package game.gateway.session;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {

    private Long userId;
    private Long roomId;
    private Integer seatId;
    private Channel channel;
    private long lastHeartbeatTime;

    public UserSession(Long userId, Channel channel) {
        this.userId = userId;
        this.channel = channel;
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    public void refreshHeartbeat() {
        this.lastHeartbeatTime = System.currentTimeMillis();
    }
}