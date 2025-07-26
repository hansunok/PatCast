package com.hansunok.petcast;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.hansunok.petcast.databinding.FragmentSettingBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;

    private ActivityResultLauncher<Intent> settingsLauncher;
    SwitchCompat switchCompatVideo, switchCompatCamera, switchCompatImage;
    CompoundButton.OnCheckedChangeListener listenerVideo, listenerCamera, listenerImage;

    TextView downData, downvideo, downImage;

    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CURRENT_REQUEST_CODE = 0;
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    setSwitchCompat(true, switchCompatVideo, listenerVideo, AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD);
                    setSwitchCompat(true, switchCompatCamera, listenerCamera, AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA);
                    setSwitchCompat(true, switchCompatImage, listenerImage, AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE);
                }
        );
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        downData = binding.downData;
        downvideo = binding.downVideo;
        downImage = binding.downImage;
        progressBar = binding.progressBar;

        progressBar.setVisibility(View.GONE);

        downData.setOnClickListener(v -> {
            exportDatabase();
        });

        downvideo.setOnClickListener(v -> {
            exportVideos();
        });

        downImage.setOnClickListener(v -> {
            exportPhotos();
        });

        TextView aboutTextView = binding.aboutTextView; //root.findViewById(R.id.aboutTextView);
        String htmlText = "<font color='#FF0000'>펫캐스트</font>는 " +
                "반려동물의 일상을 사진, 동영상, 기록을 저장 할 수 있고, 공유할수 있습니다.. " +
                "입력하신 데이터는 기기 내부에만 저장되며, 앱 삭제시 모든 데이터가 삭제 됩니다.";
//                "이용시 문의사항은 <a href='mailto:longdarihany@naver.com'>longdarihany@naver.com</a> 로 부탁드립니다.";

        aboutTextView.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        aboutTextView.setLinksClickable(true);


        ImageButton btnInfoVideo = root.findViewById(R.id.btn_info_video);
        ImageButton btnInfoCamera = root.findViewById(R.id.btn_info_camera);
        ImageButton btnInfoImage = root.findViewById(R.id.btn_info_image);

        btnInfoCamera.setOnClickListener(v -> {
            AppHelper.showInfo(getContext(), "카메라 권한 안내", "메모 입력 시 사진 및 동영상 촬영 및 저장을 위해 카메라 권한이 필요합니다.");
        });

        btnInfoImage.setOnClickListener(v -> {
            AppHelper.showInfo(getContext(),"이미지 권한 안내", "메모 입력 시 사진 선택 및 저장을 위해 이미지 권한이 필요합니다.");
        });

        btnInfoVideo.setOnClickListener(v -> {
            AppHelper.showInfo(getContext(),"동영상 권한 안내", "메모 입력 시 동영상 선택 및 저장을 위해 이미지 권한이 필요합니다.");
        });
        
        switchCompatCamera =  root.findViewById(R.id.switch_camera);
        // 리스너 정의
        listenerCamera = (buttonView, isChecked) -> {
            setChangeListener(isChecked, AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA);
        };

        switchCompatImage =  root.findViewById(R.id.switch_image);
        // 리스너 정의
        listenerImage = (buttonView, isChecked) -> {
            setChangeListener(isChecked, AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE);
        };

        switchCompatVideo =  root.findViewById(R.id.switch_video);
        // 리스너 정의
        listenerVideo = (buttonView, isChecked) -> {
            setChangeListener(isChecked, AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD);
        };
        
        setSwitchCompat(true, switchCompatCamera, listenerCamera, AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA);
        setSwitchCompat(true, switchCompatImage, listenerImage, AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE);
        setSwitchCompat(true, switchCompatVideo, listenerVideo, AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD);
        
        return root;
    }

    public void exportPhotos() {

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
        try {
            // 1. 사진 폴더
            File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // 2. 다운로드 폴더 경로
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) downloadDir.mkdirs();

            // 3. 다운로드 폴더에 직접 ZIP 파일 생성
            File zipFile = new File(downloadDir, "petcast_photos.zip");

            // 4. 압축
            zipDirectoryToPhotoFile(picturesDir, zipFile);

            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                new AlertDialog.Builder(requireContext())
                        .setTitle("완료 📦")
                        .setMessage("다운로드 폴더에 [ petcast_photos.zip ]로 저장 되었습니다.")
                        .setPositiveButton("확인", null)
                        .show();
            });


            //shareFile(zipFile, "application/zip");

        } catch (IOException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                //Toast.makeText(requireContext(), "실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
        }).start();
    }

    public void exportVideos() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // 1. 동영상 폴더
                File moviesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);

                // 2. 다운로드 폴더 경로
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) downloadDir.mkdirs();

                // 3. 다운로드 폴더에 직접 ZIP 파일 생성
                File zipFile = new File(downloadDir, "petcast_movies.zip");

                // 4. 압축
                zipDirectoryToFile(moviesDir, zipFile);

                // 5. UI 업데이트
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    new AlertDialog.Builder(requireContext())
                            .setTitle("완료 📦")
                            .setMessage("다운로드 폴더에 [ petcast_movies.zip ]로 저장되었습니다.")
                            .setPositiveButton("확인", null)
                            .show();
                });

            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    //Toast.makeText(requireContext(), "실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void exportDatabase() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                File exportDir = new File(requireContext().getExternalFilesDir(null), "export");
                if (!exportDir.exists()) exportDir.mkdirs();

                SQLiteDatabase db = requireContext().openOrCreateDatabase("pets.db", Context.MODE_PRIVATE, null);

                // 2. 테이블 목록
                String[] tableNames = {"memo", "dayMemo"};
                List<File> csvFiles = new ArrayList<>();

                for (String table : tableNames) {
                    File csvFile = new File(exportDir, table + ".csv");
                    try (FileWriter writer = new FileWriter(csvFile)) {
                        Cursor cursor = db.rawQuery("SELECT a.name AS petName, b.content AS content, b.date AS date " +
                                " FROM pet a, " + table + " b WHERE a._id = b.pet_id ORDER BY a._id, b.date", null);
                        int columnCount = cursor.getColumnCount();

                        // 헤더 작성 (pet_id → pet_name 대체)
                        for (int i = 0; i < columnCount; i++) {
                            String columnName = cursor.getColumnName(i);
                            writer.append(columnName);
                            if (i < columnCount - 1) writer.append(",");
                        }
                        writer.append("\n");

                        // 데이터 행 작성
                        while (cursor.moveToNext()) {
                            for (int i = 0; i < columnCount; i++) {
                                //String columnName = cursor.getColumnName(i);
                                String value = cursor.getString(i);

                                writer.append(value != null ? value.replace(",", " ") : "");

                                if (i < columnCount - 1) writer.append(",");
                            }
                            writer.append("\n");
                        }

                        cursor.close();
                        csvFiles.add(csvFile);
                    }
                }

                db.close();

                // 3. zip으로 묶기
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) downloadDir.mkdirs();

                File zipFile = new File(downloadDir, "petcast_data.zip");

                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                    for (File file : csvFiles) {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            ZipEntry entry = new ZipEntry(file.getName());
                            zos.putNextEntry(entry);

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();
                        }
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    new AlertDialog.Builder(requireContext())
                            .setTitle("완료 📦")
                            .setMessage("다운로드 폴더에 [ petcast_data.zip ]로 저장 되었습니다.")
                            .setPositiveButton("확인", null)
                            .show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    //Toast.makeText(requireContext(), "에러: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }


    private void zipDirectoryToFile(File sourceDir, File zipFile) throws IOException {
        if (!sourceDir.exists()) return;

        if (!zipFile.getParentFile().exists()) zipFile.getParentFile().mkdirs();

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    zipFile(zos, file, file.getName());
                }
            }
        }
        zos.close();
    }

    private void zipDirectoryToPhotoFile(File sourceDir, File zipFile) throws IOException {
        if (!sourceDir.exists()) return;

        if (!zipFile.getParentFile().exists()) zipFile.getParentFile().mkdirs();

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {

                //Log.d("zipDirectoryToPhotoFile", file.getName());
                if (file.isFile() && file.getName().endsWith(".jpg")) { // ✅ 필터 추가
                    zipFile(zos, file, file.getName()); // ✅ 폴더명 없이
                }
            }
        }

        zos.close();
    }


    private void zipFile(ZipOutputStream zos, File file, String entryName) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, len);
        }

        zos.closeEntry();
        fis.close();
    }


//    public void copyZipToDownloadFolder(File sourceZipFile) {
//        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//
//        if (!downloadsDir.exists()) downloadsDir.mkdirs();
//
//        File destFile = new File(downloadsDir, sourceZipFile.getName());
//
//        try (InputStream in = new FileInputStream(sourceZipFile);
//             OutputStream out = new FileOutputStream(destFile)) {
//
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = in.read(buffer)) > 0) {
//                out.write(buffer, 0, length);
//            }
//
//            //Toast.makeText(requireContext(), "다운로드 폴더로 복사 완료", Toast.LENGTH_SHORT).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            //Toast.makeText(requireContext(), "복사 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        setSwitchCompat(true, switchCompatVideo, listenerVideo, AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD);
        setSwitchCompat(true, switchCompatCamera, listenerCamera, AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA);
        setSwitchCompat(true, switchCompatImage, listenerImage, AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE);

    }

    private void setSwitchCompat(boolean init, SwitchCompat switchCompat, CompoundButton.OnCheckedChangeListener listener, String[] permissions, int requestCode){
        // 리스너 등록
        if(init){
            // 값을 코드로만 바꾸고 싶을 때:
            switchCompat.setOnCheckedChangeListener(null);  // 리스너 제거
            switchCompat.setChecked(AppHelper.hasPermissions(requireContext(), permissions, requestCode));                // 값만 변경
            switchCompat.setOnCheckedChangeListener(listener); // 리스너 다시 등록
        }else {
            switchCompat.setOnCheckedChangeListener(listener);
        }

    }

    private void setChangeListener(boolean isChecked, String[] permissions, int requestCode){
        if (isChecked && AppHelper.hasPermissions(requireContext(), permissions, requestCode)) {
            Toast.makeText(getContext(), requestCode + "이미 권한 허용됨", Toast.LENGTH_SHORT).show();
        } else {
            if (isChecked && AppHelper.hasShouldShowRequestPermission(getActivity(), permissions, requestCode)) {
                // 이전에 거절했지만 다시 요청 가능
                requestPermissions(permissions, requestCode);
            } else {
                // 첫 요청 또는 "다시 묻지 않기"가 체크된 경우
                openSettingUI(requestCode);
            }
        }
    }

    private void openSettingUI(int requestCode){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setSwitchCompat(true, switchCompatVideo, listenerVideo, AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD);
        setSwitchCompat(true, switchCompatCamera, listenerCamera, AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA);
        setSwitchCompat(true, switchCompatImage, listenerImage, AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
