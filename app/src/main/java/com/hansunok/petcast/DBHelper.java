package com.hansunok.petcast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pets.db";
    private static final int DATABASE_VERSION = 9;

    public static final String TABLE_PET = "pet";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_BIRTHDATE = "birthdate";
    public static final String COL_GENDER = "gender";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IMAGE_URI = "image_uri";

    public static final String TABLE_MEMO = "memo";
    public static final String COL_MEMO_ID = "_id";
    public static final String COL_MEMO_PET_ID = "pet_id";
    public static final String COL_MEMO_CONTENT = "content";
    public static final String COL_MEMO_DATE = "date";

    public static final String TABLE_MEDIA = "media";
    public static final String COL_MEDIA_ID = "_id";
    public static final String COL_MEDIA_MEMO_ID = "memo_id";
    public static final String COL_MEDIA_URI = "media_uri";
    public static final String COL_MEDIA_TYPE = "media_type";


    public static final String TABLE_DAYMEMO = "dayMemo";
    public static final String COL_DAYMEMO_ID = "_id";
    public static final String COL_DAYMEMO_PET_ID = "pet_id";
    public static final String COL_DAYMEMO_CONTENT = "content";
    public static final String COL_DAYMEMO_DATE = "date";
    public static final String COL_DAYMEMO_TIMEMILLS = "time_millis";
    public static final String COL_DAYMEMO_TIMETEXT = "time_text";


    public static final String TABLE_MEMOKEYWORD = "memoKeyword";
    public static final String COL_MEMOKEYWORD_ID = "_id";
    public static final String COL_MEMOKEYWORD_CONTENT = "content";
    public static final String COL_MEMOKEYWORD_DATE = "date";

    private static final String SQL_CREATE_PET =
            "CREATE TABLE " + TABLE_PET + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_BIRTHDATE + " TEXT, " +
                    COL_GENDER + " TEXT, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_IMAGE_URI + " TEXT" +
                    ");";

    private static final String SQL_CREATE_MEMO =
            "CREATE TABLE " + TABLE_MEMO + " (" +
                    COL_MEMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MEMO_PET_ID + " INTEGER, " +
                    COL_MEMO_CONTENT + " TEXT, " +
                    COL_MEMO_DATE + " TEXT, " +
                    "FOREIGN KEY(" + COL_MEMO_PET_ID + ") REFERENCES " + TABLE_PET + "(" + COL_ID + ")" +
                    ");";

    private static final String SQL_CREATE_MEDIA =
            "CREATE TABLE " + TABLE_MEDIA + " (" +
                    COL_MEDIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MEDIA_MEMO_ID + " INTEGER, " +
                    COL_MEDIA_URI + " TEXT, " +
                    COL_MEDIA_TYPE + " TEXT, " +
                    "FOREIGN KEY(" + COL_MEDIA_MEMO_ID + ") REFERENCES " + TABLE_MEMO + "(" + COL_MEMO_ID + ")" +
                    ");";



    private static final String SQL_CREATE_DAYMEMO =
            "CREATE TABLE " + TABLE_DAYMEMO + " (" +
                    COL_DAYMEMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DAYMEMO_PET_ID + " INTEGER, " +
                    COL_DAYMEMO_CONTENT + " TEXT, " +
                    COL_DAYMEMO_DATE + " TEXT, " +
                    COL_DAYMEMO_TIMEMILLS + " LONG, " +
                    COL_DAYMEMO_TIMETEXT + " TEXT, " +
                    "FOREIGN KEY(" + COL_DAYMEMO_PET_ID + ") REFERENCES " + TABLE_PET + "(" + COL_ID + ")" +
                    ");";
    private static final String SQL_CREATE_MEMOKEYWORD =
            "CREATE TABLE " + TABLE_MEMOKEYWORD + " (" +
                    COL_MEMOKEYWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MEMOKEYWORD_CONTENT + " TEXT NOT NULL, " +
                    COL_MEMOKEYWORD_DATE + " TEXT" +
                    ");";

    private static final String SQL_DROP_PET = "DROP TABLE IF EXISTS " + TABLE_PET;
    private static final String SQL_DROP_MEMO = "DROP TABLE IF EXISTS " + TABLE_MEMO;
    private static final String SQL_DROP_MEDIA = "DROP TABLE IF EXISTS " + TABLE_MEDIA;
    private static final String SQL_DROP_DAYMEMO = "DROP TABLE IF EXISTS " + TABLE_DAYMEMO;
    private static final String SQL_DROP_MEMOKEYWORD = "DROP TABLE IF EXISTS " + TABLE_MEMOKEYWORD;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PET);
        db.execSQL(SQL_CREATE_MEMO);
        db.execSQL(SQL_CREATE_MEDIA);
        db.execSQL(SQL_CREATE_DAYMEMO);
        db.execSQL(SQL_CREATE_MEMOKEYWORD);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_MEDIA);
        db.execSQL(SQL_DROP_MEMO);
        db.execSQL(SQL_DROP_PET);
        db.execSQL(SQL_DROP_DAYMEMO);
        db.execSQL(SQL_DROP_MEMOKEYWORD);
        onCreate(db);
    }


    public int insertDayMemo(int petId, String content, String date, long timemills, String timeText) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DAYMEMO_PET_ID, petId);
        values.put(COL_DAYMEMO_CONTENT, content);
        values.put(COL_DAYMEMO_DATE, date);
        values.put(COL_DAYMEMO_TIMEMILLS, timemills);
        values.put(COL_DAYMEMO_TIMETEXT, timeText);
        long memoId = db.insert(TABLE_DAYMEMO, null, values);
        //db.close();
        return (int) memoId;
    }

    public int updateDayMemo(int memoId, String content, String date, long timemills, String timeText) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DAYMEMO_CONTENT, content);
        values.put(COL_DAYMEMO_DATE, date);
        values.put(COL_DAYMEMO_TIMEMILLS, timemills);
        values.put(COL_DAYMEMO_TIMETEXT, timeText);
        int rows = db.update(TABLE_DAYMEMO, values, COL_DAYMEMO_ID + "=?", new String[]{String.valueOf(memoId)});
        db.close();
        return rows;
    }

    public void deleteDayMemoById(int memoId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DAYMEMO, COL_DAYMEMO_ID + "=?", new String[]{String.valueOf(memoId)});
        db.close();
    }

    public int insertMemoKeword(String content, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MEMOKEYWORD_CONTENT, content);
        values.put(COL_MEMOKEYWORD_DATE, date);
        long keywordId = db.insert(TABLE_MEMOKEYWORD, null, values);
        db.close();
        return (int) keywordId;
    }

    public int updateMemoKeword(int id, String content, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MEMOKEYWORD_CONTENT, content);
        values.put(COL_MEMOKEYWORD_DATE, date);
        int rows = db.update(TABLE_MEMOKEYWORD, values, COL_MEMOKEYWORD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public void deleteMemoKeword(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MEMOKEYWORD, COL_MEMOKEYWORD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

//    public void deleteMemoKeword(String content) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.delete(TABLE_MEMOKEYWORD, COL_MEMOKEYWORD_CONTENT + "=?", new String[]{content});
//        db.close();
//    }



    public int insertMemo(int petId, String content, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MEMO_PET_ID, petId);
        values.put(COL_MEMO_CONTENT, content);
        values.put(COL_MEMO_DATE, date);
        long memoId = db.insert(TABLE_MEMO, null, values);
        db.close();
        return (int) memoId;
    }

    public int updateMemo(int memoId, String content, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MEMO_CONTENT, content);
        values.put(COL_MEMO_DATE, date);
        int rows = db.update(TABLE_MEMO, values, COL_MEMO_ID + "=?", new String[]{String.valueOf(memoId)});
        db.close();
        return rows;
    }

    public void insertMedia(int memoId, String uri, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MEDIA_MEMO_ID, memoId);
        values.put(COL_MEDIA_URI, uri);
        values.put(COL_MEDIA_TYPE, type);
        db.insert(TABLE_MEDIA, null, values);
        db.close();
    }

    public int updateMedia(int mediaId, String uri, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MEDIA_URI, uri);
        values.put(COL_MEDIA_TYPE, type);
        int rows = db.update(TABLE_MEDIA, values, COL_MEDIA_ID + "=?", new String[]{String.valueOf(mediaId)});
        db.close();
        return rows;
    }

    public void deleteMediaByMemoId(int memoId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MEDIA, COL_MEDIA_MEMO_ID + "=?", new String[]{String.valueOf(memoId)});
        db.close();
    }

    public List<MemoItem> getMemosWithMediaByPetId(int petId, String pDate) {
        List<MemoItem> memoList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String memoQuery;
        Cursor memoCursor;

        if(pDate!=null && !pDate.isEmpty()){
            memoQuery = "SELECT " + COL_MEMO_ID + ", " + COL_MEMO_CONTENT + ", " + COL_MEMO_DATE +
                    " FROM " + TABLE_MEMO +
                    " WHERE " + COL_MEMO_PET_ID + " = " + petId + " AND "  + COL_MEMO_DATE + " = '" + pDate + "' ORDER BY " + COL_MEMO_ID + " DESC";
            memoCursor = db.rawQuery(memoQuery, null);

        }else{
            memoQuery = "SELECT " + COL_MEMO_ID + ", " + COL_MEMO_CONTENT + ", " + COL_MEMO_DATE +
                    " FROM " + TABLE_MEMO +
                    " WHERE " + COL_MEMO_PET_ID + " = ? ORDER BY " + COL_MEMO_ID + " DESC";
            memoCursor = db.rawQuery(memoQuery, new String[]{String.valueOf(petId)});
        }


        Log.d("memoQuery >>> ", memoQuery);

        while (memoCursor.moveToNext()) {
            int memoId = memoCursor.getInt(memoCursor.getColumnIndexOrThrow(COL_MEMO_ID));
            String content = memoCursor.getString(memoCursor.getColumnIndexOrThrow(COL_MEMO_CONTENT));
            String date = memoCursor.getString(memoCursor.getColumnIndexOrThrow(COL_MEMO_DATE));

            List<MediaItem> mediaItemList = new ArrayList<>();
            String mediaQuery = "SELECT " + COL_MEDIA_ID + ", " + COL_MEDIA_MEMO_ID + ", " + COL_MEDIA_TYPE + ", " + COL_MEDIA_URI +
                    " FROM " + TABLE_MEDIA +
                    " WHERE " + COL_MEDIA_MEMO_ID + " = ?";

            Cursor mediaCursor = db.rawQuery(mediaQuery, new String[]{String.valueOf(memoId)});
            while (mediaCursor.moveToNext()) {
                mediaItemList.add(new MediaItem(
                        mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_ID)),
                        mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_MEMO_ID)),
                        mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)),
                        mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_URI)),
                        null, false)
                );
            }
            mediaCursor.close();

            memoList.add(new MemoItem(memoId, content, date, mediaItemList));
        }

        memoCursor.close();
        db.close();
        return memoList;
    }

    public MemoItem getMemoWithMediaById(int memoId) {
        SQLiteDatabase db = getReadableDatabase();
        MemoItem memoItem = new MemoItem();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MEMO + " WHERE " + COL_MEMO_ID + " = ?", new String[]{String.valueOf(memoId)});
        if (cursor.moveToFirst()) {
            String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEMO_CONTENT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEMO_DATE));

            List<MediaItem> mediaList = new ArrayList<>();
            Cursor mediaCursor = db.rawQuery("SELECT * FROM " + TABLE_MEDIA + " WHERE " + COL_MEDIA_MEMO_ID + " = ?", new String[]{String.valueOf(memoId)});
            while (mediaCursor.moveToNext()) {
                mediaList.add(new MediaItem(
                        mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_ID)),
                        memoId,
                        mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)),
                        mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_URI)),
                        null, false)
                );
            }
            mediaCursor.close();

            memoItem = new MemoItem(memoId, content, date, mediaList);
        }

        cursor.close();
        db.close();
        return memoItem;
    }

    public List<MediaItem> getMediaListWithMemoById(int memoId) {
        SQLiteDatabase db = getReadableDatabase();
        List<MediaItem> mediaList = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_MEDIA +
                " WHERE " + COL_MEDIA_MEMO_ID + " = ?" +
                " ORDER BY " + COL_MEDIA_ID;

        Cursor mediaCursor = db.rawQuery(sql, new String[]{String.valueOf(memoId)});
        while (mediaCursor.moveToNext()) {
            int mediaId = mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_ID));
            String type = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_TYPE));
            String uri = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(COL_MEDIA_URI));

            mediaList.add(new MediaItem(mediaId, memoId, type, uri, null,false));
        }

        mediaCursor.close();
        db.close();
        return mediaList;
    }


    public Map<String, Integer> getCountMap(int petId) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT "+ COL_MEMO_DATE + ",  COUNT(*) AS count  FROM " + TABLE_MEMO +
                " WHERE " +COL_MEMO_PET_ID + " = " + petId + " GROUP BY " + COL_MEMO_DATE;

        Log.d("TABLE_MEMO", sql);


        Map<String, Integer> countMap = new HashMap<String, Integer>();

        Cursor cursor = db.rawQuery(sql, null);

        int recordCount = cursor.getCount();

        Log.d("TABLE_MEMO recordCount>>", recordCount + "");

        if (cursor != null) {
            // 칼럼의 마지막까지
            while (cursor.moveToNext()) {
                countMap.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        cursor.close();
        //db.close();

        return countMap;
    }

    public Map<String, Integer> getDayMemoCountMap(int petId) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT "+ COL_DAYMEMO_DATE + ",  COUNT(*) AS count  FROM " + TABLE_DAYMEMO +
                " WHERE " +COL_DAYMEMO_PET_ID + " = " + petId + " GROUP BY " + COL_DAYMEMO_DATE;

        Log.d("TABLE_DAYMEMO", sql);


        Map<String, Integer> countMap = new HashMap<String, Integer>();

        Cursor cursor = db.rawQuery(sql, null);

        int recordCount = cursor.getCount();

        Log.d("TABLE_DAYMEMO recordCount>>", recordCount + "");

        if (cursor != null) {
            // 칼럼의 마지막까지
            while (cursor.moveToNext()) {
                countMap.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        cursor.close();
        //db.close();

        return countMap;
    }


    public void deleteMemo(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MEMO, COL_MEMO_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void insertPet(Pet pet) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, pet.getName());
        values.put(COL_BIRTHDATE, pet.getBirthDate());
        values.put(COL_GENDER, pet.getGender());
        values.put(COL_DESCRIPTION, pet.getDescription());
        values.put(COL_IMAGE_URI, pet.getImageUri());
        db.insert(TABLE_PET, null, values);
        db.close();
    }

    public void updatePet(Pet pet) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, pet.getName());
        values.put(COL_BIRTHDATE, pet.getBirthDate());
        values.put(COL_GENDER, pet.getGender());
        values.put(COL_DESCRIPTION, pet.getDescription());
        values.put(COL_IMAGE_URI, pet.getImageUri());
        db.update(TABLE_PET, values, COL_ID + " = ?", new String[]{String.valueOf(pet.getId())});
        db.close();
    }

    public void deletePet(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PET, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Pet> getAllPets() {
        List<Pet> petList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PET, null);
        while (cursor.moveToNext()) {
            Pet pet = new Pet();
            pet.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
            pet.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
            pet.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDATE)));
            pet.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COL_GENDER)));
            pet.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
            pet.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI)));
            petList.add(pet);
        }
        cursor.close();
        db.close();
        return petList;
    }

    public List<DayMemo> getAllDayMemos(int petId, String pStr) {
        List<DayMemo> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        if(pStr!=null && !pStr.isEmpty()) {
            if (pStr.length() == 7) { //월 조회 

                cursor = db.rawQuery("SELECT * FROM " + TABLE_DAYMEMO
                        + " WHERE " + COL_DAYMEMO_PET_ID + "= ?"
                        + " AND strftime('%Y-%m', " + COL_DAYMEMO_DATE + ") = ?"
                        + " ORDER BY " + COL_DAYMEMO_DATE + " DESC", new String[]{String.valueOf(petId), pStr});

            } else { //일자 조회
                cursor = db.rawQuery(
                        "SELECT * FROM " + TABLE_DAYMEMO +
                                " WHERE " + COL_DAYMEMO_PET_ID + "= ?" +
                                " AND " + COL_DAYMEMO_DATE + " = ?" +
                                " ORDER BY " + COL_DAYMEMO_TIMEMILLS + " DESC",
                        new String[]{String.valueOf(petId), pStr});
            }
        }else{
            cursor = db.rawQuery("SELECT * FROM " + TABLE_DAYMEMO +
                            " WHERE " + COL_DAYMEMO_PET_ID + "= ?" +
                            " ORDER BY " + COL_DAYMEMO_ID + " DESC", new String[]{String.valueOf(petId)});
        }

        while (cursor.moveToNext()) {
            DayMemo item = new DayMemo();
            item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DAYMEMO_ID)));
            item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COL_DAYMEMO_CONTENT)));
            item.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DAYMEMO_DATE)));
            item.setTimeMills(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DAYMEMO_TIMEMILLS)));
            item.setTimeText(cursor.getString(cursor.getColumnIndexOrThrow(COL_DAYMEMO_TIMETEXT)));
            list.add(item);
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Keyword> getAllKeywords() {
        List<Keyword> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MEMOKEYWORD, null);
        while (cursor.moveToNext()) {
            Keyword item = new Keyword();
            item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MEMOKEYWORD_ID)));
            item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEMOKEYWORD_CONTENT)));
            item.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEMOKEYWORD_DATE)));
            list.add(item);
        }
        cursor.close();
        db.close();
        return list;
    }

}
