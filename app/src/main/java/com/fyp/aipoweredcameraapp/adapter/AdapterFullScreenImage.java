package com.fyp.aipoweredcameraapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.fyp.aipoweredcameraapp.R;
import com.fyp.aipoweredcameraapp.widget.TouchImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFullScreenImage extends PagerAdapter {

    private Activity act;
    private List<String> imagePaths;
    private LayoutInflater inflater;

    // constructor
    public AdapterFullScreenImage(Activity activity, List<String> imagePaths) {
        this.act = activity;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        TouchImageView imgDisplay;
        inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.activity_fullscreen_image, container, false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
        Picasso.get().load(imagePaths.get(position)).into(imgDisplay);
        //Tools.displayImageOriginal(act, imgDisplay, );

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

}
