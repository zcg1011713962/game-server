package game.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class PlayerCardDTO {

    private Long userId;

    private Integer seatId;

    private List<CardInfo> cards;
}
