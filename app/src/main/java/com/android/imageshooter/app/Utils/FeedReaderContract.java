package com.android.imageshooter.app.Utils;


import android.provider.BaseColumns;

public class FeedReaderContract {
    public FeedReaderContract() {}

    public static abstract class FeedShot implements BaseColumns {
        public static final String TABLE_NAME = "shots";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_NULLABLE = COLUMN_NAME_TITLE;
    }

}
