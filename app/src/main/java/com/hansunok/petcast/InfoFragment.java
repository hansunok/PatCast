package com.hansunok.petcast;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hansunok.petcast.databinding.FragmentInfoBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;
    RecyclerView recyclerView;
    PetAdapter adapter;
    private EditText editPetName;
    private EditText editBirthDate;
    private EditText editDescription;
    private RadioGroup genderGroup;

    private Button btnAddPet, btnDeletePet, btnResetForm;

    private ScrollView writeLayoutView;

    private ImageButton closeTopBtn;

    private Pet selectedPet = null;
    private ImageView imgPet;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private Uri selectedImageUri;

    private DBHelper dbHelper;

    private List<Pet> petList = new ArrayList<Pet>();

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgPet.setImageURI(selectedImageUri);
                    }
                }
        );

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DBHelper(getContext());

        recyclerView = binding.recyclerPets;
        editPetName = binding.editPetName;
        editBirthDate = binding.editBirthDate;
        editDescription = binding.editDescription;
        genderGroup = binding.genderGroup;
        btnAddPet = binding.btnAddPet;
        btnDeletePet = binding.btnDeletePet;
        btnResetForm = binding.btnResetForm;
        imgPet = binding.imgPet;
        closeTopBtn = binding.closeTopBtn;
        writeLayoutView = binding.writeLayoutView;

        editBirthDate.setInputType(InputType.TYPE_NULL); // 키보드 안 뜨게
        editBirthDate.setOnClickListener(v -> showDatePicker());

        binding.imgPet.setOnClickListener(v -> {

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


            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnAddPet.setOnClickListener(v -> {
            String name = editPetName.getText().toString();
            String birthDate = editBirthDate.getText().toString();
            String description = editDescription.getText().toString();

            String gender = "";
            int checkedId = genderGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.radioMale) gender = "남";
            else if (checkedId == R.id.radioFemale) gender = "여";

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "이름을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String strUri = "";
            if(selectedImageUri!=null) {
                String copiedPath = copyImageToInternalStorage(selectedImageUri);
                if (copiedPath != null) {
                    Uri imgUri = Uri.fromFile(new File(copiedPath));
                    imgPet.setImageURI(imgUri);
                    // DB에 savedUri 대신 copiedPath를 저장
                    strUri = imgUri.toString();
                }
            }

            if (selectedPet == null) {
                // 추가
                Pet newPet = new Pet(name, birthDate, gender, description, strUri);
                dbHelper.insertPet(newPet);
                Toast.makeText(getContext(), "펫 정보가 등록되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 수정
                selectedPet.setName(name);
                selectedPet.setBirthDate(birthDate);
                selectedPet.setGender(gender);
                selectedPet.setDescription(description);
                selectedPet.setImageUri(strUri);
                dbHelper.updatePet(selectedPet);
                Toast.makeText(getContext(), "펫 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                selectedPet = null;
            }
            loadPets(); // 목록 새로고침
            // clearForm();
            btnResetForm.performClick();
        });

        btnDeletePet.setOnClickListener(v -> {

           //Log.d("selectedPet.getId()", selecstedPet.getId() + "");

            if (selectedPet != null) {

                int petId = selectedPet.getId();
                //db 데이터 삭제 - only pet 테이블
                dbHelper.deletePet(petId);
                //연관 파일 삭제
                deleteMemoFiles(petId);
                Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                selectedPet = null;

                loadPets();
                btnResetForm.performClick();
                //clearForm();

            } else {
                Toast.makeText(getContext(), "삭제할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        btnResetForm.setOnClickListener(v -> {
            // 입력 폼 초기화
            editPetName.setText("");
            editBirthDate.setText("");
            editDescription.setText("");
            genderGroup.clearCheck();

            // 이미지 초기화 (필요 시)
            imgPet.setImageResource(R.drawable.pet_profile);

            // 수정 중이던 Pet 선택 초기화 (예: selectedPet = null)
            selectedPet = null;

            // 버튼 텍스트, 상태 등 신규 등록 상태로 변경
            btnAddPet.setText("등록");
            btnDeletePet.setEnabled(false);  // 삭제 버튼 비활성화

            resetList();
        });


        loadPets();

        closeTopBtn.setOnClickListener(v -> {
            closeWriteView();
        });

        FloatingActionButton fabAdd = binding.fabAdd;
        fabAdd.setOnClickListener(v -> {
            btnResetForm.performClick();
            if (writeLayoutView.getVisibility() == View.VISIBLE) {
                // 뷰가 화면에 보여지고 있는 상태
                //closeWriteView();

            } else if (writeLayoutView.getVisibility() == View.GONE) {
                // 뷰가 화면에 안 보이고 자리도 차지하지 않는 상태
                openWriteView();
            }
        });

        return root;
    }

    private void resetList(){

        if(!petList.isEmpty()) {
            for (Pet item : petList) {
                item.setSelected(false);
            }

        }
        adapter.notifyDataSetChanged();
    }

    private void loadPets() {

        DBHelper dbHelper = new DBHelper(getContext());
        petList = dbHelper.getAllPets();

        adapter = new PetAdapter(getContext(), petList);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//
//        if (!petList.isEmpty()) {
//            recyclerView.setVisibility(View.VISIBLE);
//            recyclerView.setAdapter(adapter);
//            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//            //setMainView(petList.get(0));
//
//        } else {
//            //recyclerView.setVisibility(View.GONE);
//            openWriteView();
//        }


        if (petList.isEmpty()) {
            openWriteView();
        }

        adapter.setOnClickListener(new PetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Pet pet) {
                setMainView(pet);
            }

            @Override
            public void onAlbumClick(Pet pet) {

                Bundle bundle = new Bundle();
                bundle.putInt("petId", pet.getId());
                bundle.putString("petName", pet.getName());

                NavController navController = NavHostFragment.findNavController(InfoFragment.this);
                navController.navigate(R.id.navigation_diary, bundle);

            }

            @Override
            public void onRecordClick(Pet pet) {

                Bundle bundle = new Bundle();
                bundle.putInt("petId", pet.getId());
                bundle.putString("petName", pet.getName());

                NavController navController = NavHostFragment.findNavController(InfoFragment.this);
                navController.navigate(R.id.navigation_record, bundle);

            }
        });

    }

    private void openSettingUI(int requestCode){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    private void setMainView(Pet pet){

        openWriteView();

        imgPet.setImageResource(R.drawable.pet_profile);

        selectedPet = pet;
        editPetName.setText(pet.getName());
        editBirthDate.setText(formatDate(pet.getBirthDate()));
        //editBirthDate.setText(pet.getBirthDate());
        editDescription.setText(pet.getDescription());
        if ("남".equals(pet.getGender())) genderGroup.check(R.id.radioMale);
        else if ("여".equals(pet.getGender())) genderGroup.check(R.id.radioFemale);

        if(null!=pet.getImageUri()&& !pet.getImageUri().isEmpty()){
            Uri imageUri = Uri.parse(pet.getImageUri());
            imgPet.setImageURI(imageUri);
        }else{
            imgPet.setImageResource(R.drawable.pet_profile);
        }

        btnAddPet.setText("수정");
        btnDeletePet.setEnabled(true);  // 삭제 버튼 비활성화

        if(!petList.isEmpty()){
            for(Pet item : petList){
                if(pet == item){
                    item.setSelected(true);
                }else{
                    item.setSelected(false);
                }
            }
            adapter.notifyDataSetChanged();
        }

    }

    private String copyImageToInternalStorage(Uri sourceUri) {

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File imageFile = new File(requireContext().getFilesDir(), "pet_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openWriteView(){
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        writeLayoutView.startAnimation(fadeIn);
        writeLayoutView.setVisibility(View.VISIBLE);
//        btnDownView.setVisibility(View.GONE);
//        btnUpView.setVisibility(View.VISIBLE);
    }

    private void closeWriteView(){
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        writeLayoutView.startAnimation(fadeOut);
        writeLayoutView.setVisibility(View.GONE);
//        btnDownView.setVisibility(View.VISIBLE);
//        btnUpView.setVisibility(View.GONE);
    }
//    private void clearForm() {
//        imgPet.setImageResource(R.drawable.pet_profile);
//        editPetName.setText("");
//        editBirthDate.setText("");
//        editDescription.setText("");
//        genderGroup.clearCheck();
//        resetList();
//    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editBirthDate.setText(dateStr);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private String formatDate(String input) {
        if (input == null || input.isEmpty()) return "";

        try {
            // 원본 형식에 맞게 포맷 설정
            SimpleDateFormat inputFormat;

            if (input.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
            } else if (input.contains("-")) {
                // 이미 원하는 포맷이면 그대로 반환
                return input;
            } else {
                inputFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
            }

            Date date = inputFormat.parse(input);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            return outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return input; // 파싱 실패 시 원본 그대로 반환
        }
    }

    private void deleteMemoFiles(int petId){
        List<MemoItem> memoItemList = dbHelper.getMemosWithMediaByPetId(petId, null);
        if(memoItemList!=null && memoItemList.size() > 0) {
            for (MemoItem memo : memoItemList) {
                int memoId = memo.getId();
                List<MediaItem> mediaItemList = dbHelper.getMediaListWithMemoById(memoId);
                if (mediaItemList != null && mediaItemList.size() > 0) {
                    for (MediaItem item : mediaItemList) {
                        if (item.getFileUri() != null) {
                            AppHelper.deleteFileByUri(getContext(), item.getFileUri());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}