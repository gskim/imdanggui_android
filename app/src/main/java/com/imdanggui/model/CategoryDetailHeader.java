package com.imdanggui.model;

/**
 * Created by user on 2015-09-10.
 */
public class CategoryDetailHeader {
    int id;
    String thumb;
    String name;
    String favorite;
    String yesterdayPost;
    String yesterdayReply;
    int ranking;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}
