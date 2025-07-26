package com.hansunok.petcast;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private List<MemoItem> memoList;
    private Context context;


    public interface OnItemClickListener {
        void onItemClick(MemoItem item);

        void onShareClicked(MemoItem item); //공유
        void onDeleteClicked(MemoItem item); //삭제
    }
    private MemoAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(MemoAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnMediaItemClickListener {
        void onMediaItemClick(MediaItem item);
    }

    private OnMediaItemClickListener mediaItemClickListener;

    public void setOnMediaItemClickListener(OnMediaItemClickListener listener) {
        this.mediaItemClickListener = listener;
    }

    public MemoAdapter(Context context, List<MemoItem> memoList) {
        this.context = context;
        this.memoList = memoList;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_memo, parent, false);

        return new MemoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, int position) {
        MemoItem item = memoList.get(position);


//        String date = item.getDate();
//        String content = " " + item.getContent();
//
//        SpannableString spannable = new SpannableString(date + content);
//        //spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), 0, date.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        // 날짜 색상 적용 (오렌지색)
//        spannable.setSpan(
//                new ForegroundColorSpan(Color.parseColor("#FF5722")),
//                0,
//                date.length(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//        );
//
//// 날짜 크기 축소 (기본 텍스트 크기의 0.85배)
//        spannable.setSpan(
//                new RelativeSizeSpan(0.85f),
//                0,
//                date.length(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//        );


        holder.tvMemoText.setText(item.getContent());
        holder.tvDateText.setText(item.getDate());

        List<MediaItem> mediaItemList = item.getMediaItemList();

        if (mediaItemList != null && !mediaItemList.isEmpty()) {
            holder.rvMedia.setVisibility(View.VISIBLE);
            // 미디어 리사이클러뷰 세팅
            MemoMediaAdapter mediaAdapter = new MemoMediaAdapter(context, item.getMediaItemList(), false);
            holder.rvMedia.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.rvMedia.setAdapter(mediaAdapter);
            mediaAdapter.setOnItemClickListener(mediaItem -> {
                // 이 안에서 필요한 동작 수행
                Log.d("MemoAdapter", "Media clicked: " + mediaItem.getId());
                // 또는 MemoAdapter에도 콜백 만들어서 밖으로 전달 가능
                if (mediaItemClickListener != null) {
                    mediaItemClickListener.onMediaItemClick(mediaItem);
                }
            });
        } else {
            // 미디어가 없으면 숨김
            holder.rvMedia.setVisibility(View.GONE);
        }

        //holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF")); // 노란색


        holder.itemView.setOnClickListener(v -> {
            //holder.itemView.setBackgroundColor(Color.parseColor("#FFEB3B")); // 노란색
            Log.d("itemView", "itemView clicked: " + item.getId());
            if (listener != null) listener.onItemClick(item);
        });

        if(holder.itemView.isSelected()){
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEB3B"));
        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        // 삭제 버튼 클릭 리스너 설정
        holder.btnDeleteView.setOnClickListener(v -> {
            Log.d("Delete", "Delete clicked: " + item.getId());
            if (listener != null) {
                listener.onDeleteClicked(item);  // 삭제 아이템 전달
            }
        });

        // 공유 버튼 클릭 리스너 설정
        holder.btnShareView.setOnClickListener(v -> {
            Log.d("Share", "Share clicked: " + item.getId());
            if (listener != null) {
                listener.onShareClicked(item);  // 공유 아이템 전달
            }
        });


    }



    @Override
    public int getItemCount() {
        return memoList.size();
    }

    public void addMemo(MemoItem item) {
        memoList.add(item);
        notifyItemInserted(memoList.size() - 1);
    }

    static class MemoViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemoText, tvDateText;
        ImageView btnShareView, btnDeleteView;
        RecyclerView rvMedia;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemoText = itemView.findViewById(R.id.tvMemoText);
            tvDateText = itemView.findViewById(R.id.tvDateText);
            rvMedia = itemView.findViewById(R.id.rvMedia);

            btnShareView = itemView.findViewById(R.id.shareView);
            btnDeleteView = itemView.findViewById(R.id.deleteView);
        }
    }
}
