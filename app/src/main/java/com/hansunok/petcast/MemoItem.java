package com.hansunok.petcast;
import java.util.List;

public class MemoItem {
    private int id;
    private String content;
    private String date;
    private List<MediaItem> mediaItemList;
    public MemoItem(){}
    public MemoItem(int id, String content, String date, List<MediaItem> mediaItemList) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.mediaItemList = mediaItemList;
    }

    public int getId() { return id; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public List<MediaItem> getMediaItemList() { return mediaItemList; }
    public void setMediaItemList(List<MediaItem> mediaItemList) {
        this.mediaItemList = mediaItemList;
    }
}