package com.hansunok.petcast;

import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AppHelper {

    // Prevent instantiation
    private AppHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final int REQUEST_CODE_LOCATION = 901;
    public static final int REQUEST_CODE_CAMERA = 902;
    public static final int REQUEST_CODE_IMAGE = 903;
    public static final int REQUEST_CODE_RECORD = 904;

    public static final int CONTENT_PHOTO = 101;
    public static final int REQ_PHOTO_SELECTION = 102;
    public static final int REQ_PHOTO_CAPTURE = 103;
    public static final int REQ_VIDEO_CAPTURE = 104;

    public static final String STR_Y = "Y";
    public static final String STR_N = "N";

    public static final String MEDIA_TYPE_IMAGE = "I";
    public static final String MEDIA_TYPE_VIDEO = "V";

    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    public static final String[] IMAGE_PERMISSIONS = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] RECORD_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };


    public static final int RESULT_OK = -1;
    public static final int REQ_WRITE_ACTIVITY = 201;
    public static boolean hasPermissions(Context context, String[] permissions, int requestCode) {

        for (String perm : permissions) {

            if(requestCode == REQUEST_CODE_IMAGE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if(!perm.equals(Manifest.permission.READ_MEDIA_IMAGES)){
                        continue;
                    }
                } else {
                    if(!perm.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
                        continue;
                    }
                }
            }

            Log.d("권한결과", perm + " = " + (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED ? "허용" : "거부"));

            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasShouldShowRequestPermission(Activity activity, String[] permissions, int requestCode) {

        for (String perm : permissions) {

            if(requestCode == REQUEST_CODE_IMAGE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if(!perm.equals(Manifest.permission.READ_MEDIA_IMAGES)){
                        continue;
                    }
                } else {
                    if(!perm.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
                        continue;
                    }
                }
            }

            if (shouldShowRequestPermissionRationale(activity, perm)) {
                Log.d("shouldShow", perm + " = Y" );
                return true;
            }
        }

        return false;
    }

    public static  void showInfo(Context context, String title, String Message){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(Message)
                .setPositiveButton("확인", null)
                .show();
    }

    public static String getFloatStr(Double dVal){
        String strVal = "";
        if(dVal != null) {
            Float floatVal = dVal.floatValue();
            strVal = String.valueOf(floatVal);
        }

        return strVal;
    }

    public static String getCurrentDateTime(String strFormat) {
        Date dateNow = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat(strFormat, Locale.getDefault());  // 2월 15일
        return format.format(dateNow);
    }

    public static String getDateDashStyle(String date){
        //orum.getCompleteDate()
        String rtnStr = date;
        if(!isEmpty(date) && date.length() == 8 ) {
            rtnStr = date.substring(0, 4) + "-" + date.substring(4, 6)
                    + "-" + date.substring(6);


        }

        return rtnStr;
    }

    // Utility Methods
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getMediaMimeType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);  // e.g. "image/jpeg" or "video/mp4"
    }

    public static String getMediaType(Context context, Uri uri) {
        String mime = getMediaMimeType(context, uri);
        if (mime == null) return "unknown";

        if (mime.startsWith("image/")) return MEDIA_TYPE_IMAGE;
        if (mime.startsWith("video/")) return MEDIA_TYPE_VIDEO;
        return "unknown";
    }

    public static String getMediaType(Context context, String filePath) {
        return getMediaType(context, Uri.parse(filePath));
    }

    public static File createImageFromText(Context context, String text) throws IOException {
        int paddingDp = 50;
        int textSizeSp = 15;
        int logoSizeDp = 40; // 로고 고정 크기
        float lineSpacing = 20f;

        float density = context.getResources().getDisplayMetrics().density;

        int padding = (int) (paddingDp * density);
        float scaledTextSize = textSizeSp * density;
        int logoSizePx = (int) (logoSizeDp * density);

        // Paint 설정
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(scaledTextSize);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.MONOSPACE);

        // 텍스트 줄 나누기
        List<String> lines = splitByLength(text, 15);

        // 텍스트 블록 크기 계산
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, (int) paint.measureText(line));
        }

        int width = maxWidth + padding * 2;
        float lineHeight = paint.getTextSize() + lineSpacing;
        float textBlockHeight = lineHeight * lines.size();
        int height = (int) (textBlockHeight + padding * 2);

        // 비트맵 생성
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // 🎯 로고 정중앙에 40dp × 40dp로 연하게 배경처럼 배치
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_petcast);
        if (logo != null) {
            // 로고 스케일 고정 (40dp x 40dp)
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoSizePx, logoSizePx, true);

            // 중앙 위치 계산
            int logoX = (width - logoSizePx) / 2;
            int logoY = (height - logoSizePx) / 2;

            Paint logoPaint = new Paint();
            logoPaint.setAlpha(40); // 아주 연하게 (도장 느낌)

            canvas.drawBitmap(scaledLogo, logoX, logoY, logoPaint);
        }

        // 텍스트 수직 중앙 정렬
        float y = (height - textBlockHeight) / 2 + paint.getTextSize();
        for (String line : lines) {
            canvas.drawText(line, padding, y, paint);
            y += lineHeight;
        }

        // 파일 저장
        File imageFile = new File(context.getCacheDir(), "text_center_logo_" + System.currentTimeMillis() + ".png");
        FileOutputStream out = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();

        return imageFile;
    }



    public static List<String> splitByLength(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int end = Math.min(index + maxLength, text.length());
            lines.add(text.substring(index, end));
            index = end;
        }
        return lines;
    }

    public static Uri copyFile(Context context, Uri sourceUri, String fileType) throws IOException {
        String extension;
        if ("image".equals(fileType)) {
            extension = ".jpg";
        } else if ("video".equals(fileType)) {
            extension = ".mp4";
        } else {
            extension = ".jpg";
        }

        String newFileName = "copied_file_" + System.currentTimeMillis() + extension;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File destFile = new File(storageDir, newFileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destFile)) {

            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        }

        // ✅ content:// 형태로 반환
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                destFile
        );
    }

    public static boolean deleteFileByUri(Context context, Uri fileUri) {
        Log.d("deleteFileByUri>>>", fileUri.toString());
        try {
            // ContentResolver에 delete 요청
            int rowsDeleted = context.getContentResolver().delete(fileUri, null, null);
            if (rowsDeleted > 0) {
                Log.d("FileDelete", "파일 삭제 성공");
                return true;
            } else {
                Log.w("FileDelete", "삭제된 파일 없음");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FileDelete", "파일 삭제 중 오류 발생");
            return false;
        }
    }

}