package com.imdanggui.model;

/**
 * Created by giseon on 2015-09-06.
 */
public class CategoryTabItem {
    private int id;
    private String thumb;
    private String name;
    private int ranking;
    private boolean isNew;
    private String yesterdayPost;
    private String yesterdayReply;
    private String category;
    private boolean isUser;

    public boolean isUser() {
        return isUser;
    }

    public void setIsUser(boolean isUser) {
        this.isUser = isUser;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getYesterdayPost() {
        return yesterdayPost;
    }

    public void setYesterdayPost(String yesterdayPost) {
        this.yesterdayPost = yesterdayPost;
    }

    public String getYesterdayReply() {
        return yesterdayReply;
    }

    public void setYesterdayReply(String yesterdayReply) {
        this.yesterdayReply = yesterdayReply;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

}
