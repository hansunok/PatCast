package com.hansunok.petcast;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MemoMediaAdapter extends RecyclerView.Adapter<MemoMediaAdapter.MediaViewHolder> {

    private List<MediaItem> mediaItem;
    private Context context;
    private boolean showDelete;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }
    private MemoMediaAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(MemoMediaAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public MemoMediaAdapter(Context context, List<MediaItem> mediaItem, boolean showDelete) {
        this.context = context;
        this.mediaItem = mediaItem;
        this.showDelete = showDelete;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media_thumbnail, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaItem.get(position);

        String path = item.getFilePath();
        Uri uri = item.getFileUri();
        if(uri == null){
            uri = Uri.parse(path);
            item.setFileUri(uri);
        }

        if(AppHelper.MEDIA_TYPE_VIDEO.equals(item.getType())){
            holder.playIcon.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(uri)
                    .frame(1000000) // 1초 지점의 썸네일 (단위: 마이크로초)
                    .into(holder.imageView);
        }else{
            holder.playIcon.setVisibility(View.GONE);
            // Glide 또는 다른 이미지 라이브러리로 미디어 썸네일 로드
            Glide.with(context)
                    .load(uri)
                    .centerCrop()
                    .into(holder.imageView);
        }

        if(showDelete) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                mediaItem.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mediaItem.size());
            });
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MediaPreviewActivity.class);

                intent.putExtra("media_list", new ArrayList<>(mediaItem)); // Serializable 또는 Parcelable
                intent.putExtra("media_index", position); // 시작할 인덱스도 같이 넘김

                intent.setDataAndType(item.getFileUri(), context.getContentResolver().getType(item.getFileUri()));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 중요!
                context.startActivity(intent);
            });
        }else{
            holder.btnDelete.setVisibility(View.GONE);
            holder.imageView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mediaItem.size();
    }


    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnDelete;

        ImageView playIcon;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivMediaThumbnail);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            playIcon = itemView.findViewById(R.id.playIcon);
        }
    }
}
