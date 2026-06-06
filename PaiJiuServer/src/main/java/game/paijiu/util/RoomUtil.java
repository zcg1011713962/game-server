package game.paijiu.util;

import game.common.constant.GameError;
import game.paijiu.exception.GameException;

public class RoomUtil {

    public static int calcRoomCardCost(Integer roundCount) {
        if (roundCount == null) {
            throw new GameException(GameError.ERROR22);
        }
        return switch (roundCount) {
            case 8 -> 1;
            case 16 -> 2;
            case 32 -> 3;
            default -> throw new GameException(GameError.ERROR20);
        };
    }
}
