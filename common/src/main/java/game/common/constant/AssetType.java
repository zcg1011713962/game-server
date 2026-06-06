package game.common.constant;

import lombok.Getter;

@Getter
public enum AssetType {

    ROOM_CARD("roomCard"),

    GOLD("gold"),

    DIAMOND("diamond");

    private final String field;

    AssetType(String field) {
        this.field = field;
    }
}
