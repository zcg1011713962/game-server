package game.hall.entity.resp;

public class HallGameEntryVO {
    private Integer gameId;
    private String gameCode;
    private String gameName;
    private String title;
    private String subtitle;
    private String tag;
    private String coverAsset;
    private String bgAsset;
    private String buttonAsset;
    private Integer onlineCount;
    private Boolean enabled;
    private Boolean matchEnabled;
    private Boolean roomEnabled;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCoverAsset() {
        return coverAsset;
    }

    public void setCoverAsset(String coverAsset) {
        this.coverAsset = coverAsset;
    }

    public String getBgAsset() {
        return bgAsset;
    }

    public void setBgAsset(String bgAsset) {
        this.bgAsset = bgAsset;
    }

    public String getButtonAsset() {
        return buttonAsset;
    }

    public void setButtonAsset(String buttonAsset) {
        this.buttonAsset = buttonAsset;
    }

    public Integer getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(Integer onlineCount) {
        this.onlineCount = onlineCount;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getMatchEnabled() {
        return matchEnabled;
    }

    public void setMatchEnabled(Boolean matchEnabled) {
        this.matchEnabled = matchEnabled;
    }

    public Boolean getRoomEnabled() {
        return roomEnabled;
    }

    public void setRoomEnabled(Boolean roomEnabled) {
        this.roomEnabled = roomEnabled;
    }
}
