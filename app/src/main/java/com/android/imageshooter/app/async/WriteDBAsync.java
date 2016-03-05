package com.android.imageshooter.app.async;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.android.imageshooter.app.Utils.FeedReaderContract;
import com.android.imageshooter.app.Utils.FeedReaderDBHelper;
import com.android.imageshooter.app.Utils.ShotInfos;
import com.android.imageshooter.app.activity.MainActivity;
import com.android.imageshooter.app.fragment.ImageListFragment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteDBAsync extends AsyncTask {

    MainActivity activity;
    SQLiteDatabase db;
    FeedReaderDBHelper mDbHelper;
    public void link(MainActivity act){
        activity = act;
    }
    public void unLink(){
        activity = null;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        db = activity.getDb();
        mDbHelper = activity.getmDbHelper();

        ImageListFragment fr = (ImageListFragment)activity.getSupportFragmentManager().findFragmentByTag(activity.getTag());
        List<ShotInfos> shotInfosList = fr.getShotInfosList();
        Log.i("wait-notify", "getWritableDatabase before");
        db = mDbHelper.getWritableDatabase();
        Log.i("wait-notify", "getWritableDatabase after");

        if(shotInfosList != null && shotInfosList.size() > 0){
            db.beginTransaction();

            try {
                db.delete(FeedReaderContract.FeedShot.TABLE_NAME, null, null);
                ContentValues values = new ContentValues();

                for(ShotInfos shotInfos : shotInfosList){
                    values.put(FeedReaderContract.FeedShot.COLUMN_NAME_TITLE, shotInfos.getTitle());
                    values.put(FeedReaderContract.FeedShot.COLUMN_NAME_DESCRIPTION, shotInfos.getDescription());
                    values.put(FeedReaderContract.FeedShot.COLUMN_NAME_PATH, shotInfos.getURL());

                    db.insert(FeedReaderContract.FeedShot.TABLE_NAME, "null", values);
                }

                Log.i("wait-notify", "setTransactionSuccessful before");
                db.setTransactionSuccessful();
                Log.i("wait-notify", "setTransactionSuccessful after");
            }
            finally {
                Log.i("wait-notify", "endTransaction+close before");
                db.endTransaction();
                mDbHelper.close();
                Log.i("wait-notify", "endTransaction+close after");

                synchronized(mDbHelper) {
                    Log.i("wait-notify", "notify from write");
                    mDbHelper.notify();
                }
            }

        }
        Log.i("wait-notify", "return from write");
        return null;
    }
}
