package com.hansunok.petcast;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecordCalendarFragment extends Fragment {
    DBHelper dbHelper;
    View mainView;
    int petId;
    Button buttonSetTime, btnSave;
    RecyclerView recyclerView;
    TextView textTime, tv_selected_date;
    EditText etMemo;
    List<DayMemo> dayMemoList;
    DayMemoAdapter adapter;
    CalendarAdapter calendarAdapter;
    Calendar currentCalendar;
    FlowLayout kewordContainer;
    TextView monthYearText;

    NestedScrollView scrollView;

    LinearLayout timeRow;

    Map<String, Integer> countMap = new HashMap<String, Integer>();

    final String SUFFIX = " ✖\uFE0F";
    final String INITKEYWORD = "????";


    public RecordCalendarFragment(){}
    public RecordCalendarFragment(int petId) {
        this.petId= petId; // 부모 생성자 호출
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        dbHelper = new DBHelper(getContext());

        mainView = inflater.inflate(R.layout.fragment_record_calendar, container, false);
        scrollView = mainView.findViewById(R.id.nestedScrollView);

        kewordContainer = mainView.findViewById(R.id.kewordContainer);
        etMemo = mainView.findViewById(R.id.etMemo);
        buttonSetTime = mainView.findViewById(R.id.buttonSetTime);

        textTime = mainView.findViewById(R.id.textTime);
        tv_selected_date = mainView.findViewById(R.id.tv_selected_date);
        tv_selected_date.setText(AppHelper.getCurrentDateTime("yyyy-MM-dd"));
        timeRow = mainView.findViewById(R.id.timeRow);

        btnSave = mainView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {

            String content = etMemo.getText().toString();
            if(content.isEmpty()){
                AppHelper.showInfo(getContext(),"확인", "내용이 없습니다.");
                return;
            }

            String date = tv_selected_date.getText().toString();
            if(date.isEmpty()){
                AppHelper.showInfo(getContext(),"확인", "날짜를 선택하세요.");
                return;
            }

            long timemills = -1;
            String timeText = textTime.getText().toString();
            if(!timeText.isEmpty()) {
                timemills = getMillsValue(date + " " + timeText);
            }

            int memoId = dbHelper.insertDayMemo(petId, content, date, timemills, timeText);

            dayMemoList.add(0, new DayMemo(petId, memoId, content, date, timeText, timemills));
            adapter.notifyItemInserted(0);
            recyclerView.scrollToPosition(0);

            setInitMemoView();
            setCalendarCnt(true);
        });

        // ✅ 초기값: 달력 보기
        currentCalendar = Calendar.getInstance();
        showCalendarView(currentCalendar);

        //메모 입력
        setInitMemoView();
        setTimeOnClickListener(buttonSetTime);

        //저장된 키워드 가지고 오기
        //getSaveKeywords();
        //리스트
        recyclerView = mainView.findViewById(R.id.rvMemoList);
        changeListView(DayMemoAdapter.VIEW_MODE_MONTH);

        timeRow.setOnClickListener(v -> {
            //recyclerView.setVisibility(View.GONE);
        });

        etMemo.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {

            }
        });

        return mainView;
    }

    private void getSaveKeywords() {
        //Log.d("keyword-debug", "getSaveKeywords() 호출됨");

        List<Keyword> list = dbHelper.getAllKeywords();
        // ✅ 기존 키워드 뷰 모두 제거
        kewordContainer.removeAllViews();
        // ✅ "추가 버튼" 다시 붙이기 (1번째 위치에)
        View btnAdd = LayoutInflater.from(getContext()).inflate(R.layout.item_btn_add_keyword, kewordContainer, false);
        kewordContainer.addView(btnAdd);

        btnAdd.setOnClickListener(v -> {
            View newKeyword = createKeywordView(INITKEYWORD);
            kewordContainer.addView(newKeyword, 1);
            newKeyword.setTag(-1);

            EditText etKeyword = newKeyword.findViewById(R.id.et_keyword);
            etKeyword.performLongClick();

        });

        for (Keyword item : list) {
            View keywordView = createKeywordView(item.getContent());
            keywordView.setTag(item.getId());
            kewordContainer.addView(keywordView, 1); // 항상 "추가 버튼" 뒤에 추가
        }
    }

    private View createKeywordView(String text) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View keywordView = inflater.inflate(R.layout.item_editable_keyword, kewordContainer, false);

        EditText etKeyword = keywordView.findViewById(R.id.et_keyword);
        etKeyword.setText(text);
        etKeyword.setSelection(text.length());

        // 기본적으로 포커스 불가능 상태 & 커서 안 보이게 설정
        etKeyword.setFocusable(false);
        etKeyword.setFocusableInTouchMode(false);
        etKeyword.setCursorVisible(false);
        etKeyword.clearFocus();

        final boolean[] isLongClick = {false};

        etKeyword.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isLongClick[0] = false;
                    // 일반 터치 DOWN 시 포커스 강제 해제, 키보드 숨기기
                    etKeyword.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(etKeyword.getWindowToken(), 0);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (isLongClick[0]) {
                        return true; // 롱클릭한 경우 클릭 무시
                    }

                    Drawable[] drawables = etKeyword.getCompoundDrawables();
                    Drawable drawableEnd = drawables[2]; // 오른쪽 drawable

                    if (drawableEnd != null) {
                        int drawableWidth = drawableEnd.getBounds().width();
                        int touchX = (int) event.getX();
                        int width = etKeyword.getWidth();

                        if (touchX >= width - etKeyword.getPaddingEnd() - drawableWidth - 20) {
                            int procId = (int) keywordView.getTag();
                            if (procId != -1) {
                                dbHelper.deleteMemoKeword(procId);
                                Toast.makeText(getContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                            }

                            ((ViewGroup) keywordView.getParent()).removeView(keywordView);
                            return true;
                        }
                    }

                    // 일반 클릭 시 포커스 안 주고 etMemo에 텍스트 추가
                    String clean = etKeyword.getText().toString().trim();
                    int procId = (int) keywordView.getTag();

                    if (procId != -1) {
                        String xMemo = etMemo.getText().toString();
                        if (!xMemo.isEmpty()) xMemo += " ";
                        etMemo.setText(xMemo + clean);
                        etMemo.setSelection(etMemo.getText().length());
                        etMemo.requestFocus();
                    }

                    return true;
            }
            return false;
        });

        etKeyword.setOnLongClickListener(v -> {
            isLongClick[0] = true;

            // 롱클릭 시 포커스, 커서 보이기 활성화 후 요청
            etKeyword.setFocusable(true);
            etKeyword.setFocusableInTouchMode(true);
            etKeyword.setCursorVisible(true);
            etKeyword.requestFocus();

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etKeyword, InputMethodManager.SHOW_IMPLICIT);
            }

            return true;
        });

        etKeyword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // 포커스 잃으면 다시 포커스 불가능 상태, 커서 숨기기
                etKeyword.setFocusable(false);
                etKeyword.setFocusableInTouchMode(false);
                etKeyword.setCursorVisible(false);

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(etKeyword.getWindowToken(), 0);

                // 키워드 저장 처리
                String inputText = etKeyword.getText().toString();
                String keyword = inputText.replace(SUFFIX, "").trim();

                int procId = (int) keywordView.getTag();
                String inDate = AppHelper.getCurrentDateTime("yyyy-MM-dd hhmm");

                if (!keyword.isEmpty()) {
                    if (procId != -1) {
                        dbHelper.updateMemoKeword(procId, keyword, inDate);
                    } else {
                        if (!keyword.equals(INITKEYWORD)) {
                            int newId = dbHelper.insertMemoKeword(keyword, inDate);
                            keywordView.setTag(newId);
                        }
                    }
                }
            }
        });

        return keywordView;
    }



//    private View createKeywordView(String text) {
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//        View keywordView = inflater.inflate(R.layout.item_editable_keyword, kewordContainer, false);
//
//        EditText etKeyword = keywordView.findViewById(R.id.et_keyword);
//        etKeyword.setText(text);
//        etKeyword.setSelection(text.length());
//
//        // 🔹 키워드 X 버튼 클릭 처리
//        etKeyword.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                Drawable[] drawables = etKeyword.getCompoundDrawables();
//                Drawable drawableEnd = drawables[2]; // 오른쪽 drawable
//
//                if (drawableEnd != null) {
//                    int drawableWidth = drawableEnd.getBounds().width();
//                    int touchX = (int) event.getX();
//                    int width = etKeyword.getWidth();
//
//                    if (touchX >= width - etKeyword.getPaddingEnd() - drawableWidth - 20) {
//                        // X 버튼 클릭 처리
//                        int procId = (int) keywordView.getTag();
//                        if (procId != -1) {
//                            dbHelper.deleteMemoKeword(procId);
//                            Toast.makeText(getContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
//                        }
//
//                        ((ViewGroup) keywordView.getParent()).removeView(keywordView);
//                        return true;
//                    }
//                }
//
//                // 키워드 클릭 → etMemo에 추가
//                String clean = etKeyword.getText().toString().trim();
//                int procId = (int) keywordView.getTag();
//
//                if (procId != -1) {
//                    String xMemo = etMemo.getText().toString();
//                    if (!xMemo.isEmpty()) xMemo += " ";
//                    etMemo.setText(xMemo + clean);
//                    etMemo.setSelection(etMemo.getText().length());
//                }
//                return true;
//            }
//            return false;
//        });
//
//        etKeyword.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                String inputText = etKeyword.getText().toString();
//                String keyword = inputText.replace(SUFFIX, "").trim();
//
//                // 🔁 수정: Tag는 keywordView에서 가져오기
//                int procId = (int) keywordView.getTag();
//
//                String inDate = AppHelper.getCurrentDateTime("yyyy-MM-dd hhmm");
//
//                if (!keyword.isEmpty()) {
//                    if (procId != -1) {
//                        // 기존 키워드 수정
//                        dbHelper.updateMemoKeword(procId, keyword, inDate);
//                    } else {
//                        // 새로운 키워드 저장
//                        if (!keyword.equals(INITKEYWORD)) {
//                            int newId = dbHelper.insertMemoKeword(keyword, inDate);
//                            // ✅ 저장 후 Tag 업데이트
//                            keywordView.setTag(newId);
//                        }
//                    }
//                }
//            }
//        });
//
//        etKeyword.setOnLongClickListener(v -> {
//            // 롱클릭 했을 때 포커스 요청
//            etKeyword.requestFocus();
//
//            // 키보드 띄우기
//            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm != null) {
//                imm.showSoftInput(etKeyword, InputMethodManager.SHOW_IMPLICIT);
//            }
//
//            return true;  // 롱클릭 이벤트 소비
//        });
//
//        return keywordView;
//    }

    public void setTimeOnClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                MaterialTimePicker picker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)  // 12시간제 (AM/PM)
                        .setHour(hour)
                        .setMinute(minute)
                        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)  // ✅ 키보드 입력 모드로 시작
                        .setTitleText("시간 선택")
                        .build();

                picker.show(requireActivity().getSupportFragmentManager(), "timePicker");

                picker.addOnPositiveButtonClickListener(dialog -> {
                    int selectedHour = picker.getHour();
                    int selectedMinute = picker.getMinute();

                    String amPm = (selectedHour < 12) ? "오전" : "오후";
                    int hour12 = (selectedHour % 12 == 0) ? 12 : selectedHour % 12;

                    String timeTextStr = String.format("%s %d시 %02d분", amPm, hour12, selectedMinute);
                    textTime.setText(timeTextStr);
                });

                picker.addOnNegativeButtonClickListener(dialog -> {
                    textTime.setText("");  // 사용자가 취소했을 경우 초기화
                });

                picker.addOnCancelListener(dialog -> {
                    textTime.setText("");  // 다이얼로그 닫혔을 때도 초기화
                });
            }
        });
    }

    private long getMillsValue(String dateTimeStr){
        //String timeString = "오전 12시 30분";
        long millis = -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd a h시 mm분", Locale.KOREAN);
            Date date = sdf.parse(dateTimeStr);
            millis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return millis;
    }


    private void setInitMemoView(){
        etMemo.setText("");
        textTime.setText("");
    }

    private void showCalendarView(Calendar selectedCalendar) {

        GridView calendarGrid = mainView.findViewById(R.id.calendar_grid);
        monthYearText = mainView.findViewById(R.id.tv_month_year);
        ImageButton btnPrev = mainView.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = mainView.findViewById(R.id.btn_next_month);

        // Calendar 클론으로 내부 수정
        currentCalendar = (Calendar) selectedCalendar.clone();
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH); // 0-based
        int firstDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String displayMonth = String.format(Locale.getDefault(), "%d년 %d월", year, month + 1);
        monthYearText.setText(displayMonth);

        ArrayList<String> dayList = new ArrayList<>();
        for (int i = 1; i < firstDayOfWeek; i++) {
            dayList.add("");
        }
        for (int i = 1; i <= daysInMonth; i++) {
            dayList.add(String.valueOf(i));
        }

        calendarAdapter = new CalendarAdapter(getContext(), dayList, year, month);
        calendarGrid.setAdapter(calendarAdapter);

        setCalendarCnt(false);

        // 날짜 클릭 이벤트
        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDay = dayList.get(position);
            if (!selectedDay.isEmpty()) {
                String clickedDate = String.format(Locale.getDefault(),
                        "%d-%02d-%02d", year, month + 1, Integer.parseInt(selectedDay));
                //Toast.makeText(getContext(), "선택한 날짜: " + clickedDate, Toast.LENGTH_SHORT).show();

                calendarAdapter.setSelectedPosition(position);
                tv_selected_date.setText(clickedDate);
                changeListView(DayMemoAdapter.VIEW_MODE_DAY);

            }
        });

        // ✅ "년월 텍스트" 클릭
        monthYearText.setOnClickListener(v -> {
            changeListView(DayMemoAdapter.VIEW_MODE_MONTH);
        });

        // ✅ 이전 월 버튼
        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            showCalendarView(currentCalendar);
            changeListView(DayMemoAdapter.VIEW_MODE_MONTH);
        });

        // ✅ 다음 월 버튼
        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            showCalendarView(currentCalendar);
            changeListView(DayMemoAdapter.VIEW_MODE_MONTH);
        });
    }

    private void changeListView(int pViewMode){

        dayMemoList = new ArrayList<>();

        String param = "";
        if(pViewMode == DayMemoAdapter.VIEW_MODE_MONTH){

            String mY = monthYearText.getText().toString();
            String formatted = mY.replaceAll("([0-9]{4})년\\s*([0-9]{1,2})월", "$1-$2");

            // 05월처럼 되도록 보정
            String[] parts = formatted.split("-");
            if (parts[1].length() == 1) {
                parts[1] = "0" + parts[1];
            }
            param = parts[0] + "-" + parts[1];
        }else{
            param = tv_selected_date.getText().toString();
        }
        
        dayMemoList.addAll(dbHelper.getAllDayMemos(petId, param));

        adapter = new DayMemoAdapter(dayMemoList, pViewMode);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter.setOnItemClickListener(new DayMemoAdapter.OnItemClickListener(){
            @Override
            public void onDeleteClicked(DayMemo item) {

                String title = "삭제 확인";
                String msg = "정말 삭제 하시겠습니까?";

                new AlertDialog.Builder(getContext())
                        .setTitle(title)
                        .setMessage(msg)
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                int id = item.getId();

                                dbHelper.deleteDayMemoById(item.getId());

                                if (dayMemoList != null && dayMemoList.size() > 0) {
                                    for (int i = 0; i < dayMemoList.size(); i++) {
                                        DayMemo item = dayMemoList.get(i);
                                        if (id == item.getId()) {
                                            dayMemoList.remove(i);
                                            adapter.notifyItemRemoved(i);
                                            break;
                                        }
                                    }
                                }

                                setCalendarCnt(true);
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }

        });
    }

    private void setCalendarCnt(boolean isRedraw){

        countMap = dbHelper.getDayMemoCountMap(petId);
        calendarAdapter.setCountMap(countMap);

        if(isRedraw){
            calendarAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getSaveKeywords(); // 또는 필요한 UI 초기화 로직
    }
}
