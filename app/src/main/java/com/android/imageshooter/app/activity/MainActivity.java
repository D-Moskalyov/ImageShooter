package com.android.imageshooter.app.activity;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.agilie.dribbblesdk.domain.Shot;
import com.android.imageshooter.app.R;
import com.android.imageshooter.app.Utils.RetainObject;
import com.android.imageshooter.app.Utils.FeedReaderContract;
import com.android.imageshooter.app.Utils.FeedReaderDBHelper;
import com.android.imageshooter.app.async.ReadDBAsync;
import com.android.imageshooter.app.async.WriteDBAsync;
import com.android.imageshooter.app.fragment.ImageListFragment;
import com.nostra13.universalimageloader.core.ImageLoader;


public class MainActivity extends ActionBarActivity{

    SQLiteDatabase db;
    FeedReaderDBHelper mDbHelper;
    ReadDBAsync readDBAsync;
    WriteDBAsync writeDBAsync;
    int currentPos;
    String sort;

    String tag;
    Menu menu;

    String[] projection = {
            FeedReaderContract.FeedShot._ID,
            FeedReaderContract.FeedShot.COLUMN_NAME_TITLE,
            FeedReaderContract.FeedShot.COLUMN_NAME_DESCRIPTION,
            FeedReaderContract.FeedShot.COLUMN_NAME_PATH
    };

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
            sort = retainObj.getSort();
        }
        else {
            mDbHelper = new FeedReaderDBHelper(this);
            currentPos = 0;
            sort = Shot.SORT_RECENT;
        }

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
        sort = ((ImageListFragment) getSupportFragmentManager().findFragmentByTag(tag)).getSortShots();

        RetainObject retainObj = new RetainObject(db, mDbHelper, readDBAsync, writeDBAsync, currentPos, sort);
        return retainObj;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        MenuItem sortItem;

        if(sort.equals(Shot.SORT_RECENT))
            sortItem = menu.findItem(R.id.item_sort_by_recent);
        else if(sort.equals(Shot.SORT_VIEWS))
            sortItem = menu.findItem(R.id.item_sort_by_viewed);
        else if(sort.equals(Shot.SORT_COMMENTS))
            sortItem = menu.findItem(R.id.item_sort_by_commented);
        else
            sortItem = menu.findItem(R.id.item_sort_by_popular);

        sortItem.setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem sortR = menu.findItem(R.id.item_sort_by_recent);
        MenuItem sortP = menu.findItem(R.id.item_sort_by_popular);
        MenuItem sortV = menu.findItem(R.id.item_sort_by_viewed);
        MenuItem sortC = menu.findItem(R.id.item_sort_by_commented);

        ImageListFragment fragment = ((ImageListFragment)getSupportFragmentManager().findFragmentByTag(tag));

        if(!item.isChecked()) {
            switch (item.getItemId()) {
                case R.id.item_clear_memory_cache:
                    ImageLoader.getInstance().clearMemoryCache();
                    return true;
                case R.id.item_clear_disc_cache:
                    ImageLoader.getInstance().clearDiskCache();
                    return true;
                case R.id.item_sort_by_recent:
                    sortR.setChecked(true);
                    sortP.setChecked(false);
                    sortV.setChecked(false);
                    sortC.setChecked(false);
                    fragment.setSortShots(Shot.SORT_RECENT);
                    fragment.setNumberOfPages(1);
                    fragment.getNextImages();
                    return true;
                case R.id.item_sort_by_popular:
                    sortR.setChecked(false);
                    sortP.setChecked(true);
                    sortV.setChecked(false);
                    sortC.setChecked(false);
                    fragment.setSortShots("");
                    fragment.setNumberOfPages(1);
                    fragment.getNextImages();
                    return true;
                case R.id.item_sort_by_viewed:
                    sortR.setChecked(false);
                    sortP.setChecked(false);
                    sortV.setChecked(true);
                    sortC.setChecked(false);
                    fragment.setSortShots(Shot.SORT_VIEWS);
                    fragment.setNumberOfPages(1);
                    fragment.getNextImages();
                    return true;
                case R.id.item_sort_by_commented:
                    sortR.setChecked(false);
                    sortP.setChecked(false);
                    sortV.setChecked(false);
                    sortC.setChecked(true);
                    fragment.setSortShots(Shot.SORT_COMMENTS);
                    fragment.setNumberOfPages(1);
                    fragment.getNextImages();
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

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
