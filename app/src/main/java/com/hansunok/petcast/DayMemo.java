package com.hansunok.petcast;

public class DayMemo {
    private int id;
    private int petId;
    private String content;
    private String date;
    private String timeText;
    private long timeMills;
    public DayMemo(){}
    public DayMemo(int id, int petId, String content, String date, String timeText, long timeMills) {
        this.id = id;
        this.petId = petId;
        this.content = content;
        this.date = date;
        this.timeText = timeText;
        this.timeMills = timeMills;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public long getTimeMills() {
        return timeMills;
    }

    public void setTimeMills(long timeMills) {
        this.timeMills = timeMills;
    }
}