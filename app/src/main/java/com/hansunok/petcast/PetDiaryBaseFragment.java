package com.hansunok.petcast;

import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class PetDiaryBaseFragment extends Fragment {

    private EditText editMemo;
    private RecyclerView registeredItemsRecyclerView;
    private MemoAdapter memoAdapter;
    private List<MemoItem> memoList = new ArrayList<>();
    private List<MediaItem> mediaItemList = new ArrayList<>();
    private TextView tv_selected_date;
    private MemoMediaAdapter mediaAdapter;
    private RecyclerView mediaRecyclerView;
    private Button btnAddPet, btnDeletePet, btnResetForm;
    private ImageView btnAddPhoto;
    private ImageButton closeTopBtn;
    private LinearLayout btnShareView, writeLayoutView;
    //private LinearLayout btnDownView, btnUpView;
    private DBHelper dbHelper;

    private int selectedPhotoMenu;
    private Uri photoUri;
    private Uri videoUri;
    private String imageFileName;

    private MemoItem selectedMemoItem;


    protected int petId;
    protected int viewMode;  //0:list, 1:calendar

    public PetDiaryBaseFragment() {
        // 기본 생성자 필요 (시스템이 재생성할 수 있게 함)
    }
    public PetDiaryBaseFragment(int petId, int viewMode) {
        this.petId = petId;
        this.viewMode = viewMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        dbHelper = new DBHelper(getContext());
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater,
                                      ViewGroup container,
                                      Bundle savedInstanceState);

    public abstract void redrawView();


    public void initView(View view){
        editMemo = view.findViewById(R.id.editMemo);
        btnAddPet = view.findViewById(R.id.btnAddPet); //등록
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto); //사진,동영상 추가
        btnResetForm = view.findViewById(R.id.btnResetForm); //리셋
        btnDeletePet = view.findViewById(R.id.btnDeletePet); //삭제
        btnShareView = view.findViewById(R.id.btnShareView);
        writeLayoutView = view.findViewById(R.id.writeLayoutView);
        tv_selected_date = view.findViewById(R.id.tv_selected_date);
        closeTopBtn = view.findViewById(R.id.closeTopBtn);

        tv_selected_date.setText(AppHelper.getCurrentDateTime("yyyy-MM-dd"));

        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);

        fabAdd.setOnClickListener(v -> {
            if (writeLayoutView.getVisibility() == View.VISIBLE) {
                // 뷰가 화면에 보여지고 있는 상태
                closeWriteView();
            } else if (writeLayoutView.getVisibility() == View.GONE) {
                // 뷰가 화면에 안 보이고 자리도 차지하지 않는 상태
                openWriteView();
            }
        });

        //등록
        btnAddPet.setOnClickListener(v -> {

            String memoText = editMemo.getText().toString().trim();
            if(memoText.isEmpty() && mediaItemList.isEmpty()){
                AppHelper.showInfo(getContext(), "등록", "등록할 내용이 없습니다");
                return;
            }

            String title = "등록 확인";
            String msg = "등록 하시겠습니까?";
            if (selectedMemoItem != null) {
                title = "수정 확인";
                msg = "수정 하시겠습니까?";
            }

            new AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            procRegister(true);
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }).show();

        });

        // 사진/동영상 추가
        btnAddPhoto.setOnClickListener(v -> {
            showDialogP(AppHelper.CONTENT_PHOTO);
        });

        // 삭제
        btnDeletePet.setOnClickListener(v -> {
            if(selectedMemoItem == null){
                AppHelper.showInfo(getContext(), "삭제", "삭제할 건이 없습니다");
                return;
            }
            procDelete(selectedMemoItem);
        });

        // 초기화
        btnResetForm.setOnClickListener(v -> {
            resetFormView();
        });

        closeTopBtn.setOnClickListener(v -> {
            closeWriteView();
        });


        //공유하기
        btnShareView.setOnClickListener(v -> {
            String memoText = editMemo.getText().toString().trim();
            int inputSize =  mediaItemList.size();
            if(selectedMemoItem!=null) {
                int objSize = selectedMemoItem.getMediaItemList().size();
                if(!memoText.equals(selectedMemoItem.getContent()) || inputSize != objSize){
                    new AlertDialog.Builder(getContext())
                            .setTitle("확인")
                            .setMessage("수정된 내용이 있습니다. 수정 후 공유 하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    procRegister(false);
                                    procShare();
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }else{
                    procShare();
                }
            }else{
                if(!memoText.isEmpty() || inputSize > 0) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("확인")
                            .setMessage("등록 후 공유 합니다.")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    procRegister(false);
                                    procShare();
                                }
                            }).show();
                }

            }

        });

        //상단 등록 썸네일
        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView);
        mediaAdapter = new MemoMediaAdapter(getContext(), mediaItemList, true);
        mediaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mediaRecyclerView.setAdapter(mediaAdapter);

        //등록된 메모 목록  & 썸네일
        registeredItemsRecyclerView = view.findViewById(R.id.registeredItemsRecyclerView);

        setListViews();

    }

    public void setListViews(){

        if(viewMode == 1){ //calendar
            memoList = dbHelper.getMemosWithMediaByPetId(petId, tv_selected_date.getText().toString());
        }else{ // list
            memoList = dbHelper.getMemosWithMediaByPetId(petId, null);
        }

        //memoList = dbHelper.getMemosWithMediaByPetId(petId); //db에서 조회
        memoAdapter = new MemoAdapter(getContext(), memoList);
        registeredItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        registeredItemsRecyclerView.setAdapter(memoAdapter);
        memoAdapter.setOnMediaItemClickListener(item-> {
            //btnAddPet.setText("등록");
            if(memoList!=null && memoList.size() > 0){
                for (int i = 0; i < memoList.size(); i++) {
                    MemoItem memo = memoList.get(i);
                    if(item.getMemoId() == memo.getId()){
                        setMainView(memo);
                        selectedMemoItem = memo;
                        //btnAddPet.setText("수정");
                        openWriteView();
                        break;
                    }
                }
            }
        });


        memoAdapter.setOnItemClickListener(new MemoAdapter.OnItemClickListener(){
            @Override
            public void onShareClicked(MemoItem item) {
                procShareRow(item);
            }

            @Override
            public void onDeleteClicked(MemoItem item) {
                procDelete(item);
            }

            @Override
            public void onItemClick(MemoItem item) {
                setMainView(item);
                selectedMemoItem = item;
                openWriteView();
            }

        });
    }

    private void openWriteView(){
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        writeLayoutView.startAnimation(fadeIn);
        writeLayoutView.setVisibility(View.VISIBLE);

    }

    private void closeWriteView(){

        resetFormView();

        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        writeLayoutView.startAnimation(fadeOut);
        writeLayoutView.setVisibility(View.GONE);

    }

    private void procRegister(boolean bReset){
        String memoText = editMemo.getText().toString().trim();
        String today = tv_selected_date.getText().toString(); //LocalDate.now().toString();

        if (selectedMemoItem != null) {
            // ✅ 수정 모드
            int memoId = selectedMemoItem.getId();
            dbHelper.updateMemo(memoId, memoText, today);

            // 기존 미디어를 다 삭제 후 다시 삽입 (간단한 방식)
            dbHelper.deleteMediaByMemoId(memoId);  // delete 쿼리 필요

            saveMediaItem(memoId);

            // UI 갱신
            int indexToUpdate = -1;
            for (int i = 0; i < memoList.size(); i++) {
                if (memoList.get(i).getId() == memoId) {
                    indexToUpdate = i;
                    break;
                }
            }

            if (indexToUpdate != -1) {
                // 수정된 메모 다시 조회해서 덮어쓰기
                MemoItem updatedMemo = dbHelper.getMemoWithMediaById(memoId);
                memoList.set(indexToUpdate, updatedMemo);
                memoAdapter.notifyItemChanged(indexToUpdate);

                selectedMemoItem = updatedMemo;
            }

        } else {
            // ✅ 신규 등록
            int memoId = dbHelper.insertMemo(petId, memoText, today);

            saveMediaItem(memoId);
            List<MemoItem> updatedList;
            if(viewMode == 1){ //calendar
                updatedList = dbHelper.getMemosWithMediaByPetId(petId, tv_selected_date.getText().toString());
            }else{ // list
                updatedList = dbHelper.getMemosWithMediaByPetId(petId, null);
            }

            //List<MemoItem> updatedList = dbHelper.getMemosWithMediaByPetId(petId);
            MemoItem lastItem = updatedList.get(0);  // 최신 메모가 제일 앞에 있다고 가정
            memoList.add(0, lastItem);
            memoAdapter.notifyItemInserted(0);
            registeredItemsRecyclerView.scrollToPosition(0);

            selectedMemoItem = lastItem;
        }

        closeWriteView();

        if(bReset) {
            resetFormView(); // 폼 초기화 + selectedMemoItem = null 등
        }

        redrawView();
    }

    private void saveMediaItem(int memoId){

        if(mediaItemList.isEmpty()){
            return;
        }

        for (MediaItem item : mediaItemList) {

            String type = AppHelper.getMediaType(getContext(), item.getFileUri());

            if(!item.isNew()) {
                Uri copyUri = null;
                String strUri = null;
                try {
                    copyUri = AppHelper.copyFile(getContext(), item.getFileUri(), type);
                } catch (IOException ex) {
                    //파일생성 오류
                }
                dbHelper.insertMedia(memoId, copyUri.toString(), type);
            }else{
                dbHelper.insertMedia(memoId, item.getFilePath(), type);
            }
        }
    }

    private void resetFormView(){
        selectedMemoItem = null;

        editMemo.setText("");
        mediaItemList.clear();
        btnAddPet.setText("등록");
        
        mediaAdapter.notifyDataSetChanged(); // 썸네일 초기화
    }

    private void setMainView(MemoItem item) {
        editMemo.setText(item.getContent());
        mediaItemList.clear();
        if (item.getMediaItemList() != null && item.getMediaItemList().size() > 0){
            for (MediaItem mediaItem : item.getMediaItemList()) {
                mediaItemList.add(new MediaItem(mediaItem.getId(), mediaItem.getMemoId(), mediaItem.getType(), mediaItem.getFilePath(), mediaItem.getFileUri(), false));
            }
        }
        mediaAdapter.notifyDataSetChanged();

        btnAddPet.setText("수정");
    }

    private void showDialogP(int id) {
        AlertDialog.Builder builder = null;

        switch (id) {
            case AppHelper.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(getContext());
                selectedPhotoMenu = 0;
                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, (dialog, which) -> {
                    selectedPhotoMenu = which;
                });
                builder.setPositiveButton("선택", (dialog, which) -> {
                    if (selectedPhotoMenu == 0) {
                        showPhotoSelectionActivity();
                    } else if (selectedPhotoMenu == 1) {
                        showPhotoCaptureActivity();
                    }else if (selectedPhotoMenu == 2) {
                        showVidioCaptureActivity();
                    }
                });
                builder.setNegativeButton("취소", null);
                break;
        }

        if (builder != null) {
            builder.create().show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //이미지 선택
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == AppHelper.REQ_PHOTO_SELECTION) {
                if (data != null) {
                    if (data.getClipData() == null) {
                        setMediaItemList(data.getData(), false);
                    } else {
                        ClipData clipData = data.getClipData();
                        Log.e("clipData", String.valueOf(clipData.getItemCount()));
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            //uri = clipData.getItemAt(i).getUri();
                            setMediaItemList(clipData.getItemAt(i).getUri(), false);
                        }
                    }
                }
            } else if (requestCode == AppHelper.REQ_PHOTO_CAPTURE) {
                setMediaItemList(photoUri, true);
            } else if (requestCode == AppHelper.REQ_VIDEO_CAPTURE) {
                setMediaItemList(videoUri, true);
            }
            mediaAdapter.notifyDataSetChanged();
        }
    }


    private void setMediaItemList(Uri uri, boolean isNew){
        if(uri == null){
            return;
        }

        if(isExistUri(uri)){
            return;
        }

        String filePath = uri.toString();
        if(selectedMemoItem==null){
            mediaItemList.add(new MediaItem(AppHelper.getMediaType(getContext(), uri), filePath, uri, isNew));
        }else{
            mediaItemList.add(new MediaItem(-1, selectedMemoItem.getId(), AppHelper.getMediaType(getContext(), uri), filePath, uri, isNew));
        }
        mediaRecyclerView.scrollToPosition(mediaItemList.size() - 1);
    }

    private boolean isExistUri(Uri uri){
        boolean bResult = false;

        if(mediaItemList.isEmpty()){
            return false;
        }

        for(MediaItem item : mediaItemList){
            if(uri.equals(item.getFileUri())){
                return true;
            }
        }

        return false;
    }
    private void showPhotoSelectionActivity() {

        if (AppHelper.hasPermissions(getContext(), AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE)) {
            //Toast.makeText(this, "이미 권한 허용됨", Toast.LENGTH_SHORT).show();
        } else {
            if (AppHelper.hasShouldShowRequestPermission(getActivity(), AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE)) {
                // 이전에 거절했지만 다시 요청 가능
                requestPermissions(AppHelper.IMAGE_PERMISSIONS, AppHelper.REQUEST_CODE_IMAGE);
            } else {
                // 첫 요청 또는 "다시 묻지 않기"가 체크된 경우
                openSettingUI(AppHelper.REQUEST_CODE_IMAGE);
            }
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*"); // 모든 파일 유형
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 개 선택 가능하게 하려면 true
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
                "image/*",
                "video/*"
        });

        startActivityForResult(intent, AppHelper.REQ_PHOTO_SELECTION);
    }

    private void showVidioCaptureActivity() {
        if (AppHelper.hasPermissions(getContext(), AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD)) {
            // Toast.makeText(this, "이미 권한 허용됨", Toast.LENGTH_SHORT).show();
        } else {
            if (AppHelper.hasShouldShowRequestPermission(getActivity(), AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD)) {
                // 이전에 거절했지만 다시 요청 가능
                requestPermissions(AppHelper.RECORD_PERMISSIONS, AppHelper.REQUEST_CODE_RECORD);
            } else {
                // 첫 요청 또는 "다시 묻지 않기"가 체크된 경우
                openSettingUI(AppHelper.REQUEST_CODE_RECORD);
            }
            return;
        }

        File videoFile = null;

        try {
            videoFile = createVideoFile();

            // FileProvider를 통해 Uri 생성 (Manifest와 xml 설정 필요)
            videoUri = FileProvider.getUriForFile(
                    getContext(),
                    getContext().getPackageName() + ".fileprovider",
                    videoFile
            );

            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            startActivityForResult(takeVideoIntent, AppHelper.REQ_VIDEO_CAPTURE);
            //Toast.makeText(getContext(), "동영상 파일 : " + videoUri.toString(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(getContext(), "동영상 파일 생성 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private File createVideoFile() throws IOException {
        // 1. 파일 이름 생성 (예: VIDEO_20250702_123456.mp4)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "VIDEO_" + timeStamp;

        // 2. 앱의 외부 저장소의 Movies 디렉토리 경로 가져오기
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        // 3. 빈 mp4 파일 생성
        File video = File.createTempFile(
                fileName,   // prefix
                ".mp4",     // suffix
                storageDir  // directory
        );

        return video;
    }

    private void showPhotoCaptureActivity() {

        if (AppHelper.hasPermissions(getContext(), AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA)) {
            // Toast.makeText(this, "이미 권한 허용됨", Toast.LENGTH_SHORT).show();
        } else {
            if (AppHelper.hasShouldShowRequestPermission(getActivity(), AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA)) {
                // 이전에 거절했지만 다시 요청 가능
                requestPermissions(AppHelper.CAMERA_PERMISSIONS, AppHelper.REQUEST_CODE_CAMERA);
            } else {
                // 첫 요청 또는 "다시 묻지 않기"가 체크된 경우
                openSettingUI(AppHelper.REQUEST_CODE_CAMERA);
            }
            return;
        }

        Log.d("call : ", "showPhotoCaptureActivity");

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.d("showPhotoCaptureActivity photoFile Error", ex.toString());
        }
        photoUri = null;

        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            startActivityForResult(intent, AppHelper.REQ_PHOTO_CAPTURE);

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // ✅ 여기 중요
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void openSettingUI(int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppHelper.REQUEST_CODE_CAMERA || requestCode == AppHelper.REQUEST_CODE_IMAGE) {
            boolean allGranted = true;

            for (int i = 0; i < permissions.length; i++) {
                String perm = permissions[i];
                int result = grantResults[i];
                Log.d("권한결과", perm + " = " + (result == PackageManager.PERMISSION_GRANTED ? "허용" : "거부"));

                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }

            if (allGranted) {
                if (requestCode == AppHelper.REQUEST_CODE_CAMERA) {
                    showPhotoCaptureActivity();
                } else {
                    showPhotoSelectionActivity();
                }
            } else {
                Toast.makeText(getContext(), "필수 권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void procShare(){
        ArrayList<Uri> uris = new ArrayList<>();

        String text = editMemo.getText().toString();

        if(text.isEmpty() && mediaItemList.isEmpty()){
            AppHelper.showInfo(getContext(), "확인", "공유할 내용이 없습니다");
            return;
        }

        Uri textUri = getContentImg(text);
        if(textUri!=null){
            uris.add(textUri);
        }

        for(MediaItem item : mediaItemList){
            Log.d("getFileUri >>>> ", item.getFileUri().toString());
            uris.add(item.getFileUri());
        }

        Intent shareIntent;

        if (uris.size() == 1) {
            // ✅ 이미지 또는 비디오 1개일 때
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(AppHelper.getMediaMimeType(getContext(), uris.get(0)));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        } else {
            // ✅ 여러 이미지/영상일 때
            shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("*/*");  // 복합 콘텐츠 MIME
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "공유하기"));
    }

    private void procShareRow(MemoItem memoItem){
        ArrayList<Uri> uris = new ArrayList<>();

        Log.d("getContent >>> ", memoItem.getContent());
        if(memoItem == null){
            AppHelper.showInfo(getContext(), "확인", "공유할 내용이 없습니다!");
        }

        String text = memoItem.getContent();
        List<MediaItem> mediaItems = memoItem.getMediaItemList();

        if(text.isEmpty() && mediaItems.isEmpty()){
            AppHelper.showInfo(getContext(), "확인", "공유할 내용이 없습니다");
            return;
        }

        Uri textUri = getContentImg(text);
        if(textUri!=null){
            uris.add(textUri);
        }

        for(MediaItem item : mediaItems){
            uris.add(item.getFileUri());
        }

        Intent shareIntent;

        if (uris.size() == 1) {
            // ✅ 이미지 또는 비디오 1개일 때
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(AppHelper.getMediaMimeType(getContext(), uris.get(0)));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        } else {
            // ✅ 여러 이미지/영상일 때
            shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("*/*");  // 복합 콘텐츠 MIME
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "공유하기"));
    }

    private Uri getContentImg(String text){
        Uri textUri = null;

        if (!text.isEmpty()) {
            try {
                //캐쉬파일 삭제 해줌
                File cacheDir = getContext().getCacheDir();
                if (cacheDir != null && cacheDir.isDirectory()) {
                    for (File child : cacheDir.listFiles()) {
                        child.delete();
                    }
                }

                File imageFile = AppHelper.createImageFromText(getContext(), text);
                textUri = FileProvider.getUriForFile(getContext(), "com.hansunok.petcast.fileprovider", imageFile);

                Log.d("textUri >>>> ", textUri.toString());
            }catch(Exception e){
                Log.d("textUri >>>> ", "XXXXXX");
                e.printStackTrace();
            }
        }

        return textUri;
    }


    private void procDelete(MemoItem memoItem) {

        if (memoItem == null) {
            AppHelper.showInfo(getContext(), "삭제", "삭제할 건이 없습니다");
            return;
        }

        //Log.d("procDelete getContent >>> ", memoItem.getContent());

        String title = "삭제 확인";
        String msg = "정말 삭제 하시겠습니까?";

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int id = memoItem.getId();
                        dbHelper.deleteMemo(id);
                        deleteMemoFiles(id);

                        if(selectedMemoItem!=null && selectedMemoItem.getId() == id) {
                            resetFormView();
                        }

                        if (memoList != null && memoList.size() > 0) {
                            for (int i = 0; i < memoList.size(); i++) {
                                MemoItem item = memoList.get(i);
                                if (id == item.getId()) {
                                    memoList.remove(i);
                                    memoAdapter.notifyItemRemoved(i);
                                    break;
                                }
                            }
                        }

                        closeWriteView();
                        redrawView();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void deleteMemoFiles(int memoId){
        List<MediaItem> mediaItemList = dbHelper.getMediaListWithMemoById(memoId);
        if(mediaItemList!=null && mediaItemList.size() > 0){
            for(MediaItem item : mediaItemList) {
                if(item.getFileUri()!=null) {
                    AppHelper.deleteFileByUri(getContext(), item.getFileUri());
                }
            }
        }
    }

}
