package game.gateway.session;

import io.netty.util.AttributeKey;

public class SessionKeys {
    public static final AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    private SessionKeys() {}
}
