package com.hansunok.petcast;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> days;
    private final int year;
    private final int month;

    private int selectedPosition = -1;

    private Map<String, Integer> countMap;

    public CalendarAdapter(Context context, List<String> days, int year, int month) {
        this.context = context;
        this.days = days;
        this.year = year;
        this.month = month;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int i) {
        return days.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(0, 2, 0, 2);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        String day = days.get(position);

        TextView tvDay = new TextView(context);
        TextView tvCount = new TextView(context);

        tvDay.setTextSize(14);
        tvDay.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        tvCount.setTextSize(8);
        tvCount.setTextColor(Color.DKGRAY);
        tvCount.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        if (!day.isEmpty()) {
            int dayInt = Integer.parseInt(day);
            tvDay.setText(day);

            // 요일별 색상
            int mod = position % 7;
            if (mod == 0) {
                tvDay.setTextColor(Color.parseColor("#D32F2F")); // 일
            } else if (mod == 6) {
                tvDay.setTextColor(Color.parseColor("#1976D2")); // 토
            } else {
                tvDay.setTextColor(Color.BLACK); // 평일
            }

            // 오늘 날짜 강조
            if (isToday(year, month, dayInt)) {
                layout.setBackgroundColor(Color.parseColor("#FFF176")); // 연노랑
            }

            // 선택된 날짜 강조 (우선순위: 선택 > 오늘)
            if (position == selectedPosition) {
                layout.setBackgroundColor(Color.parseColor("#4CAF50")); // 초록
                tvDay.setTextColor(Color.WHITE);
                tvCount.setTextColor(Color.WHITE);
            }

            // 날짜별 개수 표시
            if (countMap != null) {
                String dateKey = String.format("%04d-%02d-%02d", year, month + 1, dayInt);
                if (countMap.containsKey(dateKey)) {
                    int count = countMap.get(dateKey);
                    if (count > 0) {
                        tvCount.setText("(" + count + ")");
                    }
                }
            }

        } else {
            tvDay.setText("");
            tvCount.setText("");
        }

        layout.addView(tvDay);
        layout.addView(tvCount);

        return layout;
    }
    private boolean isToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == year &&
                today.get(Calendar.MONTH) == month &&
                today.get(Calendar.DAY_OF_MONTH) == day;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public void setCountMap(Map<String, Integer> map) {
        this.countMap = map;
        notifyDataSetChanged();
    }

    public Map<String, Integer> getCountMap() {
        return this.countMap;
    }

}
