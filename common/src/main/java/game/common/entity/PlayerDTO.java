package game.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {

    private Long userId;

    private String avatar;

    private Long gold;

    private String nickname;

    private Integer seatId;

    private Boolean online;

    /**
     * 0=未入座
     * 1=已入座
     * 2=已准备
     * 3=游戏中
     */
    private Integer state;
}