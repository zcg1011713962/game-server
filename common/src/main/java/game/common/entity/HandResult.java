package game.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandResult {

    private List<CardInfo> cards;
    private int type;
    private int point;
    private String name;
}