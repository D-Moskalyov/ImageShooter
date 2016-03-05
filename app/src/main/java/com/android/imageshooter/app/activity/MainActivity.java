package com.android.imageshooter.app.activity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imageshooter.app.R;
import com.android.imageshooter.app.Utils.RetainObject;
import com.android.imageshooter.app.Utils.ShotInfos;
import com.android.imageshooter.app.Utils.FeedReaderContract;
import com.android.imageshooter.app.Utils.FeedReaderDBHelper;
import com.android.imageshooter.app.async.ReadDBAsync;
import com.android.imageshooter.app.async.WriteDBAsync;
import com.android.imageshooter.app.fragment.ImageListFragment;
import okhttp3.internal.http.RetryableSink;

import java.util.*;
import java.util.concurrent.TimeoutException;


public class MainActivity extends FragmentActivity {

    SQLiteDatabase db;
    FeedReaderDBHelper mDbHelper;
    ReadDBAsync readDBAsync;
    WriteDBAsync writeDBAsync;
    int currentPos;

    String tag;

    String[] projection = {
            FeedReaderContract.FeedShot._ID,
            FeedReaderContract.FeedShot.COLUMN_NAME_TITLE,
            FeedReaderContract.FeedShot.COLUMN_NAME_DESCRIPTION,
            FeedReaderContract.FeedShot.COLUMN_NAME_PATH
    };

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

        RetainObject retainObj = (RetainObject) getLastCustomNonConfigurationInstance();
        if(retainObj != null) {
            readDBAsync = retainObj.getReadDBAsync();
            writeDBAsync = retainObj.getWriteDBAsync();
            db = retainObj.getDb();
            mDbHelper = retainObj.getDbHelper();
            currentPos = retainObj.getCurrentPos();
        }
        else {
            mDbHelper = new FeedReaderDBHelper(this);
            currentPos = 0;
        }

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

        tag = ImageListFragment.class.getSimpleName();
        fr = getSupportFragmentManager().findFragmentByTag(tag);
        if (fr == null) {
            fr = new ImageListFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();

        currentPos = ((ImageListFragment)getSupportFragmentManager().findFragmentByTag(tag)).getListView().getFirstVisiblePosition();

        WriteDBAsync writeDBAsync = new WriteDBAsync();
        writeDBAsync.link(this);
        writeDBAsync.execute();

        synchronized(mDbHelper) {
            try {
                Log.i("wait-notify", "wait from Main write");
                mDbHelper.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if(readDBAsync != null)
            readDBAsync.unLink();
        if(writeDBAsync != null)
            writeDBAsync.unLink();

        RetainObject retainObj = new RetainObject(db, mDbHelper, readDBAsync, writeDBAsync, currentPos);
        return retainObj;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        ImageListFragment fr = ((ImageListFragment)getSupportFragmentManager().findFragmentByTag(tag));
//        View v = fr.getView().findViewById(android.R.id.list);
//        TextView tVD = (TextView) v.findViewById(R.id.textDesc);
//        TextView tVT = (TextView) v.findViewById(R.id.textTitle);
//        ImageView iV = (ImageView) v.findViewById(R.id.image);
//        int w = iV.getWidth();
//        tVD.setWidth(w);
//        tVT.setWidth(w);
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


    public SQLiteDatabase getDb() {
        return db;
    }

    public FeedReaderDBHelper getmDbHelper() {
        return mDbHelper;
    }

    public String[] getProjection() {
        return projection;
    }

    public String getTag() {
        return tag;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }
}
