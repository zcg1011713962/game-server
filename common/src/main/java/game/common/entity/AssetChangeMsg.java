package game.common.entity;

import lombok.Data;

@Data
public class AssetChangeMsg {
    private Long userId;
    private String field;
}
