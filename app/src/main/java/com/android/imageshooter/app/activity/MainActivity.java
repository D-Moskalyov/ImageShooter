package com.android.imageshooter.app.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.agilie.dribbblesdk.domain.Shot;
import com.agilie.dribbblesdk.service.auth.AuthCredentials;
import com.agilie.dribbblesdk.service.auth.DribbbleAuthHelper;
import com.agilie.dribbblesdk.service.auth.DribbbleConstants;
import com.agilie.dribbblesdk.service.retrofit.DribbbleServiceGenerator;
import com.android.imageshooter.app.R;
import com.android.imageshooter.app.ShotInfos;
import com.android.imageshooter.app.fragment.ImageListFragment;
import com.google.api.client.auth.oauth2.Credential;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    //private static final String DRIBBBLE_CLIENT_ID = "1cb175a8b3171b2084b2d03f036928bf8fb09f09a2b4fdff109744fc774ea112";
    //private static final String DRIBBBLE_CLIENT_SECRET = "9667b3a0904bdff92648ee8341d3ce8c93ec4b857751b00063519886e9db4490";
    //private static final String DRIBBBLE_CLIENT_ACCESS_TOKEN = "28499cacc1e937ae8a611ee402c4900fe97ce0b5bc536c53df67e20ca78126d6";
    //private static final String DRIBBBLE_CLIENT_REDIRECT_URL = "";

    //String authToken;

//    private static int NUMBER_OF_PAGES = 1;
//    private static final int SHOTS_PER_PAGE = 50;
//
//    private SwipeRefreshLayout swipeContainer;

//    MainActivity context = this;
//    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//        .cacheInMemory(true)
//        .cacheOnDisk(true)
//        .build();
//
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
//                .defaultDisplayImageOptions(defaultOptions)
//        .build();
//
//        ImageLoader.getInstance().init(config);

        //setContentView(R.layout.activity_main);

//        SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) this.findViewById(R.id.swiperefresh);

//        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                int i = 1;
//                i = 5;
//            }
//        });

        Fragment fr;
        String tag;

        tag = ImageListFragment.class.getSimpleName();
        fr = getSupportFragmentManager().findFragmentByTag(tag);
        if (fr == null) {
            fr = new ImageListFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag).commit();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



}
