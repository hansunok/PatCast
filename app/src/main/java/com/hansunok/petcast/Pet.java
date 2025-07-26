package com.hansunok.petcast;

public class Pet {
    private int id;
    private String name;
    private String birthDate;
    private String gender;
    private String description;
    private String imageUri;  // 이미지 URI 문자열

    private boolean isSelected;

    public Pet() {
    }

    public Pet(String name, String birthDate, String gender, String description, String imageUri) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.description = description;
        this.imageUri = imageUri;
    }

    // getter / setter
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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}