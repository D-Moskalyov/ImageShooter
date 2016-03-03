package com.android.imageshooter.app.async;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.android.imageshooter.app.Utils.FeedReaderContract;
import com.android.imageshooter.app.Utils.FeedReaderDBHelper;
import com.android.imageshooter.app.Utils.ShotInfos;
import com.android.imageshooter.app.activity.MainActivity;
import com.android.imageshooter.app.fragment.ImageListFragment;

import java.util.List;

public class ReadDBAsync extends AsyncTask {

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

        db = mDbHelper.getReadableDatabase();

        ImageListFragment fr = (ImageListFragment)activity.getSupportFragmentManager().findFragmentByTag(activity.getTag());
        List<ShotInfos> shotInfosList = fr.getShotInfosList();

        Cursor c = db.query(FeedReaderContract.FeedShot.TABLE_NAME, activity.getProjection(), null, null, null, null, null);
        //ArrayList<ShotInfos> shotInfosList = new ArrayList<ShotInfos>();
        if(c != null){
            c.moveToFirst();
            shotInfosList.add(new ShotInfos(
                    c.getString(c.getColumnIndex(FeedReaderContract.FeedShot.COLUMN_NAME_DESCRIPTION)),
                    c.getString(c.getColumnIndex(FeedReaderContract.FeedShot.COLUMN_NAME_TITLE)),
                    c.getString(c.getColumnIndex(FeedReaderContract.FeedShot.COLUMN_NAME_PATH))));
            while (c.moveToNext()){
                shotInfosList.add(new ShotInfos(
                        c.getString(c.getColumnIndex(FeedReaderContract.FeedShot.COLUMN_NAME_DESCRIPTION)),
                        c.getString(c.getColumnIndex(FeedReaderContract.FeedShot.COLUMN_NAME_TITLE)),
                        c.getString(c.getColumnIndex(FeedReaderContract.FeedShot.COLUMN_NAME_PATH))));
            }
        }

        mDbHelper.close();

        return null;
    }


}
