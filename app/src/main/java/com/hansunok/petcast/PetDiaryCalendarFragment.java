package com.hansunok.petcast;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PetDiaryCalendarFragment extends PetDiaryBaseFragment {


    private CalendarAdapter calendarAdapter;
    private Calendar currentCalendar;

    private DBHelper dbHelper;


    private View mainView;

    private Map<String, Integer> countMap = new HashMap<String, Integer>();

    public PetDiaryCalendarFragment(){}
    public PetDiaryCalendarFragment(int petId) {
        super(petId, 1); // 부모 생성자 호출
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_diary_pet_calendar, container, false);

        initView(mainView);

        dbHelper = new DBHelper(getContext());

        // ✅ 초기값: 달력 보기
        currentCalendar = Calendar.getInstance();
        showCalendarView(currentCalendar);
       // setDayViewList(getCurrentDay());

        LinearLayout selCalLayout = mainView.findViewById(R.id.selCalLayout); //등록
        selCalLayout.setVisibility(View.VISIBLE);

        return mainView;
    }

    @Override
    public void redrawView(){
        setCalendarCnt(true);
    }

    private void showCalendarView(Calendar selectedCalendar) {

        GridView calendarGrid = mainView.findViewById(R.id.calendar_grid);
        TextView monthYearText = mainView.findViewById(R.id.tv_month_year);
        ImageButton btnPrev = mainView.findViewById(R.id.btn_prev_month);
        ImageButton btnNext = mainView.findViewById(R.id.btn_next_month);

        TextView tv_selected_date = mainView.findViewById(R.id.tv_selected_date);


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


                setListViews();

            }
        });


        // ✅ "년월 텍스트" 클릭
        monthYearText.setOnClickListener(v -> showMonthYearPicker(year, month));

        // ✅ 이전 월 버튼
        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            showCalendarView(currentCalendar);
        });

        // ✅ 다음 월 버튼
        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            showCalendarView(currentCalendar);
        });



        tv_selected_date.setText(AppHelper.getCurrentDateTime("yyyy-MM-dd"));


    }

        private void showMonthYearPicker(int initYear, int initMonth) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 날짜 선택 완료 → 달력 갱신
                    Calendar newCal = Calendar.getInstance();
                    newCal.set(Calendar.YEAR, selectedYear);
                    newCal.set(Calendar.MONTH, selectedMonth);
                    newCal.set(Calendar.DAY_OF_MONTH, selectedDay);

                    showCalendarView(newCal);
                },
                initYear, initMonth, 1
        );

        // 일(day)은 숨길 수는 없지만 무시됨
        datePickerDialog.show();
    }

    private void setCalendarCnt(boolean isRedraw){

        countMap = dbHelper.getCountMap(petId);
        calendarAdapter.setCountMap(countMap);

        if(isRedraw){
            calendarAdapter.notifyDataSetChanged();
        }

    }

}
