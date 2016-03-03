package com.android.imageshooter.app.async;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.android.imageshooter.app.Utils.FeedReaderContract;
import com.android.imageshooter.app.Utils.FeedReaderDBHelper;
import com.android.imageshooter.app.Utils.ShotInfos;
import com.android.imageshooter.app.activity.MainActivity;
import com.android.imageshooter.app.fragment.ImageListFragment;

import java.util.List;

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
        db = mDbHelper.getWritableDatabase();

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

                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
                mDbHelper.close();
            }

        }

        return null;
    }
}
