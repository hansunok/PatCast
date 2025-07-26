package com.hansunok.petcast;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Pet pet);
        void onAlbumClick(Pet pet);
        void onRecordClick(Pet pet);
    }
    private Context context;
    private List<Pet> petList;

    private OnItemClickListener listener;

    public void setOnClickListener (OnItemClickListener listener) {
        this.listener = listener;
    }


    public PetAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);

        holder.tvName.setText(pet.getName());
        holder.tvBirthDate.setText("생년월일: " + pet.getBirthDate());
        holder.tvGender.setText("성별: " + pet.getGender());
        holder.tvDescription.setText("소개: " + pet.getDescription());

        // 이미지 URI가 있으면 이미지 표시, 없으면 기본 이미지 표시
        if (pet.getImageUri() != null && !pet.getImageUri().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(pet.getImageUri());
                holder.imgPet.setImageURI(imageUri);
            } catch (Exception e) {
                holder.imgPet.setImageResource(R.drawable.pet_profile);
            }
        } else {
            holder.imgPet.setImageResource(R.drawable.pet_profile);
        }

//        if(pet.isSelected()){
//            holder.itemRow.setVisibility(View.GONE);
//        }else{
//            holder.itemRow.setVisibility(View.VISIBLE);
//        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(pet);
        });
        holder.btnAlbumView.setOnClickListener(v -> {
            if (listener != null) listener.onAlbumClick(pet);
        });
        holder.btnRecordView.setOnClickListener(v -> {
            if (listener != null) listener.onRecordClick(pet);
        });

    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPet;
        TextView tvName, tvBirthDate, tvGender, tvDescription;
        LinearLayout itemRow, btnAlbumView, btnRecordView;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgPet);
            tvName = itemView.findViewById(R.id.tvName);
            tvBirthDate = itemView.findViewById(R.id.tvBirthDate);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            itemRow = itemView.findViewById(R.id.itemRow);
            btnAlbumView = itemView.findViewById(R.id.btnAlbumView);
            btnRecordView = itemView.findViewById(R.id.btnRecordView);
        }
    }

    // 리스트 갱신 메서드 (필요시)
    public void updateList(List<Pet> newList) {
        petList = newList;
        notifyDataSetChanged();
    }
}
