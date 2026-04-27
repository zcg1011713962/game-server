package game.paijiu.room;

import game.common.constant.GameState;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Room {

    private Long roomId;

    private Map<Integer, Long> seats = new HashMap<>();

    private GameState state;

    private Map<Long, Integer> bets = new HashMap<>();
}