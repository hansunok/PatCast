package com.hansunok.petcast;

import android.net.Uri;

import java.io.Serializable;

public class MediaItem implements Serializable {
    private int  memoId;
    private int  id;
    private String type;      // "image" or "video"
    private String filePath;  // 파일 경로 or Uri

    private boolean isNew;
    private transient Uri fileUri;

    public MediaItem(){}
    public MediaItem(String type, String filePath, Uri fileUri, boolean isNew){
        this.type = type;
        this.filePath = filePath;
        if((filePath!=null && !filePath.isEmpty()) && fileUri == null){
            fileUri = Uri.parse(filePath);
        }
        this.fileUri = fileUri;
        this.isNew = isNew;
    }
    public MediaItem(int id, int memoId, String type, String filePath, Uri fileUri, boolean isNew){
        this.id = id;
        this.memoId = memoId;
        this.type = type;
        this.filePath = filePath;
        if((filePath!=null && !filePath.isEmpty()) && fileUri == null){
            fileUri = Uri.parse(filePath);
        }
        this.fileUri = fileUri;
        this.isNew = isNew;
    }

    public int getMemoId() {
        return memoId;
    }

    public void setMemoId(int memoId) {
        this.memoId = memoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}