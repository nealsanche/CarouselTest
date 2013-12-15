package com.robotsandpencils.carousel;

import java.util.Locale;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();

        Carousel carousel = (Carousel)findViewById(R.id.carousel);

        carousel.setStretchZ(1f);
        carousel.setStretchX(1.1f);
        carousel.setTiltAngleZ(70);
        carousel.setTiltAngleY(20);
/*
        carousel.setStretchZ(1f/50);
        carousel.setTiltAngleZ(0);
        carousel.setTiltAngleY(0);
*/
        for (int i = 0; i < 5; i++) {
            final CarouselItem child = new CarouselItem(this);
            child.setImageRotation(-10);
            child.setScale(0.5f);
            child.setBlurMode(BlurMode.TO_BACK);
            ImageView image = new ImageView(this);
            image.setImageDrawable(getResources().getDrawable(R.drawable.img_649));
            child.addImage(image);
            carousel.addView(child);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
