package com.fyp.aipoweredcameraapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.fyp.aipoweredcameraapp.adapter.AdapterFullScreenImage;
import com.fyp.aipoweredcameraapp.utils.Tools;

import java.util.ArrayList;

public class ActivityFullScreenImage extends AppCompatActivity {

    public static final String EXTRA_POS = "key.EXTRA_POS";
    public static final String EXTRA_IMGS = "key.EXTRA_IMGS";

    private AdapterFullScreenImage adapter;
    private ViewPager viewPager;
    private TextView text_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        viewPager = (ViewPager) findViewById(R.id.pager);
        text_page = (TextView) findViewById(R.id.text_page);

        ArrayList<String> items = new ArrayList<>();
        Intent i = getIntent();
        final int position = i.getIntExtra(EXTRA_POS, 0);
        items = i.getStringArrayListExtra(EXTRA_IMGS);
        adapter = new AdapterFullScreenImage(ActivityFullScreenImage.this, items);
        final int total = adapter.getCount();
        viewPager.setAdapter(adapter);

        text_page.setText(String.format(getString(R.string.image_processed)));

        // displaying selected image first
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                if(pos==0)
                    text_page.setText(String.format(getString(R.string.image_processed)));
                else
                    text_page.setText(String.format(getString(R.string.image_original)));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        ((ImageButton) findViewById(R.id.btnClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}

