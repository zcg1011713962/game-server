package game.hall.mybatis.domain;

import java.util.Date;

/**
 * @TableName db_settle_record
 */
public class DbSettleRecord {
    private Long id;

    private Long roomId;

    private Long roundId;

    private Long userId;

    private Integer seatId;

    private Long bankerUserId;

    private Integer bankerSeat;

    private Integer win;

    private Long betAmount;

    private Long winAmount;

    private Long afterGold;

    private String cards;

    private String cardTypeName;

    private String settleDesc;

    private Long settleTime;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Long getBankerUserId() {
        return bankerUserId;
    }

    public void setBankerUserId(Long bankerUserId) {
        this.bankerUserId = bankerUserId;
    }

    public Integer getBankerSeat() {
        return bankerSeat;
    }

    public void setBankerSeat(Integer bankerSeat) {
        this.bankerSeat = bankerSeat;
    }

    public Integer getWin() {
        return win;
    }

    public void setWin(Integer win) {
        this.win = win;
    }

    public Long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(Long betAmount) {
        this.betAmount = betAmount;
    }

    public Long getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(Long winAmount) {
        this.winAmount = winAmount;
    }

    public Long getAfterGold() {
        return afterGold;
    }

    public void setAfterGold(Long afterGold) {
        this.afterGold = afterGold;
    }

    public String getCards() {
        return cards;
    }

    public void setCards(String cards) {
        this.cards = cards;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getSettleDesc() {
        return settleDesc;
    }

    public void setSettleDesc(String settleDesc) {
        this.settleDesc = settleDesc;
    }

    public Long getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(Long settleTime) {
        this.settleTime = settleTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}