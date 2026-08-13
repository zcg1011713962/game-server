package game.hall.entity.resp;

public class HallBannerVO {
    private Integer gameId;
    private String title;
    private String subtitle;
    private String tagText;
    private String bgAsset;
    private String buttonAsset;
    private Integer onlineCount;
    private Boolean enabled;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
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

    public String getTagText() {
        return tagText;
    }

    public void setTagText(String tagText) {
        this.tagText = tagText;
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
}
