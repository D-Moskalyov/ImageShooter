package com.android.imageshooter.app.Utils;

import android.database.sqlite.SQLiteDatabase;
import com.android.imageshooter.app.async.ReadDBAsync;
import com.android.imageshooter.app.async.WriteDBAsync;


public class RetainObject {
    ReadDBAsync readDBAsync;
    WriteDBAsync writeDBAsync;
    FeedReaderDBHelper dbHelper;
    SQLiteDatabase db;

    public RetainObject(SQLiteDatabase db, FeedReaderDBHelper dbHelper, ReadDBAsync readDBAsync, WriteDBAsync writeDBAsync) {
        this.db = db;
        this.dbHelper = dbHelper;
        this.readDBAsync = readDBAsync;
        this.writeDBAsync = writeDBAsync;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    public FeedReaderDBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(FeedReaderDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public ReadDBAsync getReadDBAsync() {
        return readDBAsync;
    }

    public void setReadDBAsync(ReadDBAsync readDBAsync) {
        this.readDBAsync = readDBAsync;
    }

    public WriteDBAsync getWriteDBAsync() {
        return writeDBAsync;
    }

    public void setWriteDBAsync(WriteDBAsync writeDBAsync) {
        this.writeDBAsync = writeDBAsync;
    }
}
