package com.android.imageshooter.app.Utils;

import android.database.sqlite.SQLiteDatabase;
import com.android.imageshooter.app.async.ReadDBAsync;
import com.android.imageshooter.app.async.WriteDBAsync;


public class RetainObject {
    ReadDBAsync readDBAsync;
    WriteDBAsync writeDBAsync;
    FeedReaderDBHelper dbHelper;
    SQLiteDatabase db;
    int currentPos;
    String sort;

    public RetainObject(SQLiteDatabase db, FeedReaderDBHelper dbHelper, ReadDBAsync readDBAsync, WriteDBAsync writeDBAsync, int currentPos, String sort) {
        this.db = db;
        this.dbHelper = dbHelper;
        this.readDBAsync = readDBAsync;
        this.writeDBAsync = writeDBAsync;
        this.currentPos = currentPos;
        this.sort = sort;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public FeedReaderDBHelper getDbHelper() {
        return dbHelper;
    }

    public ReadDBAsync getReadDBAsync() {
        return readDBAsync;
    }

    public WriteDBAsync getWriteDBAsync() {
        return writeDBAsync;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public String getSort() {
        return sort;
    }

}
