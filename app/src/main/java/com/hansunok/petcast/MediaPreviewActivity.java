package com.hansunok.petcast;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MediaPreviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private VideoView videoView;

    private ImageButton btnClose, btnPrev, btnNext;

    int currentIndex;
    int listSize;
    ArrayList<MediaItem> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_preview);
        getSupportActionBar().hide();
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        btnClose = findViewById(R.id.btnClose);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);


        mediaList = (ArrayList<MediaItem>) getIntent().getSerializableExtra("media_list");
        currentIndex = getIntent().getIntExtra("media_index", 0);
        listSize = mediaList.size();

        Log.d("getData>>>",  currentIndex + ":" + listSize);

        preview();

        btnClose.setOnClickListener(v -> finish());
        btnPrev.setOnClickListener(v -> viewPrev());
        btnNext.setOnClickListener(v -> viewNext());
    }

    private void viewNext(){


        Log.d("viewNext currentIndex>>>",  currentIndex + "" );

        if(currentIndex == (listSize - 1)){
            AppHelper.showInfo(this, "알림", "마지막 입니다");
            return;
        }
        currentIndex++;
        preview();

    }

    private void viewPrev(){
        Log.d("viewNext currentIndex>>>",  currentIndex + "" );
        if(currentIndex == 0){
            AppHelper.showInfo(this, "알림", "처음 입니다");
            return;
        }
        currentIndex--;
        preview();
    }


    private void preview(){
        btnPrev.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);

        if(currentIndex == 0){
            btnPrev.setVisibility(View.GONE);
        }

        if(currentIndex == (listSize - 1)){
            btnNext.setVisibility(View.GONE);
        }

        Log.d("preview currentIndex>>>",  currentIndex + "" );
        if(!mediaList.isEmpty()){
            for (int i = 0; i < listSize ; i++) {

                MediaItem item = mediaList.get(i);

                if(currentIndex == i){

                    String mimeType = item.getType();
                    Uri mediaUri = Uri.parse(item.getFilePath());

                    Log.d("mimeType>>>>", mimeType);
                    Log.d("mediaUri>>>>", mediaUri.toString());

                    if(AppHelper.MEDIA_TYPE_IMAGE.equals(mimeType)){
                        imageView.setVisibility(View.VISIBLE);
                        videoView.setVisibility(View.GONE);

                        Glide.with(this)
                                .load(mediaUri)
                                .into(imageView);
                    }else{
                        imageView.setVisibility(View.GONE);
                        videoView.setVisibility(View.VISIBLE);

                        videoView.setVideoURI(mediaUri);
                        videoView.setMediaController(new MediaController(this));
                        videoView.requestFocus();
                        videoView.start();
                    }

                    break;
                }
            }
        }
    }

}
