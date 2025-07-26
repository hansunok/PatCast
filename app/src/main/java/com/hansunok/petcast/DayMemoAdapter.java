package com.hansunok.petcast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DayMemoAdapter extends RecyclerView.Adapter<DayMemoAdapter.DayMemoViewHolder> {

    private List<DayMemo> dayMemoList;
    static final int VIEW_MODE_DAY = 0;
    static final int VIEW_MODE_MONTH = 1;
    int viewMode = VIEW_MODE_MONTH;

    public interface OnItemClickListener {
        void onDeleteClicked(DayMemo item); //삭제
    }
    private DayMemoAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(DayMemoAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public DayMemoAdapter(List<DayMemo> dayMemoList, int viewMode) {
        this.dayMemoList = dayMemoList;
        this.viewMode = viewMode;
    }

    @NonNull
    @Override
    public DayMemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_memo, parent, false);
        return new DayMemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayMemoViewHolder holder, int position) {
        DayMemo item = dayMemoList.get(position);
        holder.contentText.setText(item.getContent());
        holder.dateText.setText(item.getDate());
        holder.timeText.setText(item.getTimeText());

        holder.btnDeleteView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(item);  // 삭제 아이템 전달
            }
        });

        if(item.getTimeText()==null || item.getTimeText().isEmpty()){
            holder.timeText.setVisibility(View.GONE);
        }else{
            holder.timeText.setVisibility(View.VISIBLE);
        }

        if (viewMode == VIEW_MODE_MONTH ) {
            holder.dateText.setVisibility(View.VISIBLE);
        }else{
            holder.dateText.setVisibility(View.GONE);
        }

        if(holder.timeText.getVisibility() == View.GONE && holder.dateText.getVisibility() == View.GONE){
            holder.dateTimeLayout.setVisibility(View.GONE);
        }else{
            holder.dateTimeLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return dayMemoList.size();
    }

    public static class DayMemoViewHolder extends RecyclerView.ViewHolder {
        TextView contentText, dateText, timeText, btnDeleteView;
        LinearLayout dateTimeLayout;

        public DayMemoViewHolder(@NonNull View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.tv_content);
            dateText = itemView.findViewById(R.id.tv_date);
            timeText = itemView.findViewById(R.id.tv_time_text);
            btnDeleteView = itemView.findViewById(R.id.btn_delete);
            dateTimeLayout = itemView.findViewById(R.id.dateTimeLayout);
        }
    }
}