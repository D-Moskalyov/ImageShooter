package com.android.imageshooter.app.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.widget.*;
import com.agilie.dribbblesdk.domain.Shot;
import com.agilie.dribbblesdk.service.retrofit.DribbbleServiceGenerator;
import com.android.imageshooter.app.R;
import com.android.imageshooter.app.Utils.*;
import com.android.imageshooter.app.async.ReadDBAsync;
import com.android.imageshooter.app.async.WriteDBAsync;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.*;


public class MainActivity extends ActionBarActivity{

    private SQLiteDatabase db;
    private FeedReaderDBHelper mDbHelper;
    private ReadDBAsync readDBAsync;
    private WriteDBAsync writeDBAsync;
    private ImageAdapter imageAdapter;

    private MainActivity mainActivity;
    private Menu menu;
    private SwipeRefreshLayout swipeContainer;
    private AbsListView listView;


    private static ArrayList<ShotInfos> shotInfosList;
    private HashMap<String, Object> queryMap;
    private int currentPos;

    private String[] projection = {
            FeedReaderContract.FeedShot._ID,
            FeedReaderContract.FeedShot.COLUMN_NAME_TITLE,
            FeedReaderContract.FeedShot.COLUMN_NAME_DESCRIPTION,
            FeedReaderContract.FeedShot.COLUMN_NAME_PATH
    };



    private static final String DRIBBBLE_CLIENT_ACCESS_TOKEN = "28499cacc1e937ae8a611ee402c4900fe97ce0b5bc536c53df67e20ca78126d6";
    private static int NUMBER_OF_PAGES = 1;
    private static final int SHOTS_PER_PAGE = 50;
    private static String SORT_SHOTS;

    private boolean pauseOnScroll = false;
    private boolean pauseOnFling = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        imageAdapter = new ImageAdapter(this);

        RetainObject retainObj = (RetainObject) getLastCustomNonConfigurationInstance();
        if(retainObj != null) {
            readDBAsync = retainObj.getReadDBAsync();
            writeDBAsync = retainObj.getWriteDBAsync();
            db = retainObj.getDb();
            mDbHelper = retainObj.getDbHelper();
            currentPos = retainObj.getCurrentPos();
            SORT_SHOTS = retainObj.getSort();
        }
        else {
            mDbHelper = new FeedReaderDBHelper(this);
            currentPos = 0;
            SORT_SHOTS = Shot.SORT_RECENT;
        }


        shotInfosList = new ArrayList<ShotInfos>();
        listView = (ListView) this.findViewById(android.R.id.list);

        ((ListView) listView).setAdapter(imageAdapter);

        swipeContainer = (SwipeRefreshLayout) this.findViewById(R.id.swiperefresh);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNextImages();
            }
        });

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
                if (NUMBER_OF_PAGES == 1)
                    getNextImages();
                else {
                    getShotsInfoFromDB();
                }
            }
        });

        queryMap = new HashMap<String, Object>();

    }

    @Override
    protected void onPause() {
        super.onPause();

        currentPos = listView.getFirstVisiblePosition();

        WriteDBAsync writeDBAsync = new WriteDBAsync();
        writeDBAsync.link(this);
        writeDBAsync.execute();

        synchronized(mDbHelper) {
            try {
                mDbHelper.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        applyScrollListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnimateFirstDisplayListener.displayedImages.clear();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if(readDBAsync != null)
            readDBAsync.unLink();
        if(writeDBAsync != null)
            writeDBAsync.unLink();

        RetainObject retainObj = new RetainObject(db, mDbHelper, readDBAsync, writeDBAsync, currentPos, SORT_SHOTS);
        return retainObj;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        MenuItem sortItem;

        if(SORT_SHOTS.equals(Shot.SORT_RECENT))
            sortItem = menu.findItem(R.id.item_sort_by_recent);
        else if(SORT_SHOTS.equals(Shot.SORT_VIEWS))
            sortItem = menu.findItem(R.id.item_sort_by_viewed);
        else if(SORT_SHOTS.equals(Shot.SORT_COMMENTS))
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
                    SORT_SHOTS = Shot.SORT_RECENT;
                    NUMBER_OF_PAGES = 1;
                    swipeContainer.setRefreshing(true);
                    getNextImages();
                    return true;
                case R.id.item_sort_by_popular:
                    sortR.setChecked(false);
                    sortP.setChecked(true);
                    sortV.setChecked(false);
                    sortC.setChecked(false);
                    SORT_SHOTS = "";
                    NUMBER_OF_PAGES = 1;
                    swipeContainer.setRefreshing(true);
                    getNextImages();
                    return true;
                case R.id.item_sort_by_viewed:
                    sortR.setChecked(false);
                    sortP.setChecked(false);
                    sortV.setChecked(true);
                    sortC.setChecked(false);
                    SORT_SHOTS = Shot.SORT_VIEWS;
                    NUMBER_OF_PAGES = 1;
                    swipeContainer.setRefreshing(true);
                    getNextImages();
                    return true;
                case R.id.item_sort_by_commented:
                    sortR.setChecked(false);
                    sortP.setChecked(false);
                    sortV.setChecked(false);
                    sortC.setChecked(true);
                    SORT_SHOTS = Shot.SORT_COMMENTS;
                    NUMBER_OF_PAGES = 1;
                    swipeContainer.setRefreshing(true);
                    getNextImages();
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }



    private void getNextImages(){
        if(isOnline()) {
            queryMap.put("page", NUMBER_OF_PAGES);
            queryMap.put("per_page", SHOTS_PER_PAGE);
            if(!SORT_SHOTS.equals(""))
                queryMap.put("sort", SORT_SHOTS);
            else
                queryMap.remove("sort");
            Call<List<Shot>> shotsCall = DribbbleServiceGenerator
                    .getDribbbleShotService(DRIBBBLE_CLIENT_ACCESS_TOKEN)
                    .fetchShots(queryMap);
            shotsCall.enqueue(new Callback<List<Shot>>() {
                @Override
                public void onResponse(Response<List<Shot>> response) {
                    List<Shot> shots = response.body();
                    shotInfosList.clear();
                    for (Shot shot : shots) {
                        String path = ShotPathString.getShotPathString(shot);
                        int dotIndx = path.lastIndexOf(".");
                        String ext = path.substring(dotIndx, path.length());
                        if (!ext.equals(".gif"))
                            shotInfosList.add(new ShotInfos(shot.getDescription(), shot.getTitle(), path));
                    }
                    NUMBER_OF_PAGES++;
                    if(this != null) {
                        ((ListView) listView).setAdapter(imageAdapter);
                        swipeContainer.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    swipeContainer.setRefreshing(false);
                }
            });
        }
        else {
            Toast.makeText(mainActivity, "ShotsFromCache", Toast.LENGTH_SHORT).show();
            if(shotInfosList == null || shotInfosList.size() == 0) {
                getShotsInfoFromDB();
            }else if(swipeContainer != null & swipeContainer.isRefreshing())
                swipeContainer.setRefreshing(false);
        }

    }

    private void getShotsInfoFromDB(){
        ReadDBAsync readDBAsync = new ReadDBAsync();
        readDBAsync.link(mainActivity);
        readDBAsync.execute();
        synchronized(mDbHelper) {
            try {
                mDbHelper.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(swipeContainer != null & swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
            listView.setSelection(currentPos);
        }
    }



    private static class ImageAdapter extends BaseAdapter {

        private DisplayImageOptions options;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private LayoutInflater inflater;

        public ImageAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new SimpleBitmapDisplayer())
                    .build();
            ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(options)
                    .diskCacheFileCount(200)
                    .build();

            ImageLoader.getInstance().init(configuration);
        }

        @Override
        public int getCount() {
            return shotInfosList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = inflater.inflate(R.layout.item_list_image, parent, false);
                holder = new ViewHolder();
                holder.textDesc = (TextView) view.findViewById(R.id.textDesc);
                holder.textTitle = (TextView) view.findViewById(R.id.textTitle);
                holder.image = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (shotInfosList.get(position).getDescription() != null) {
                holder.textDesc.setText(Html.fromHtml(shotInfosList.get(position).getDescription()));
                holder.textDesc.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else
                holder.textDesc.setVisibility(View.INVISIBLE);
            holder.textTitle.setText(shotInfosList.get(position).getTitle());

            ImageLoader.getInstance().displayImage(shotInfosList.get(position).getURL(), holder.image, options, animateFirstListener);

            return view;
        }

    }

    private static class ViewHolder {
        TextView textDesc;
        TextView textTitle;
        ImageView image;
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                final ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewParent viewParent = imageView.getParent();
                        if (viewParent instanceof RelativeLayout) {
                            RelativeLayout r = (RelativeLayout) viewParent;
                            TextView textTitle = (TextView) r.findViewById(R.id.textTitle);
                            TextView textDesc = (TextView) r.findViewById(R.id.textDesc);

                            int w = imageView.getWidth();
                            int hT = textTitle.getHeight();
                            int hD = textDesc.getHeight();

                            RelativeLayout.LayoutParams paramsT = new RelativeLayout.LayoutParams(w, hT);
                            paramsT.addRule(10);
                            paramsT.addRule(14);
                            RelativeLayout.LayoutParams paramsD = new RelativeLayout.LayoutParams(w, hD);
                            paramsD.addRule(12);
                            paramsD.addRule(14);

                            textTitle.setLayoutParams(paramsT);
                            textDesc.setLayoutParams(paramsD);
                        }
                    }
                });
            }
        }

    }

    private void applyScrollListener() {
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll, pauseOnFling));
    }



    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    public static ArrayList<ShotInfos> getShotInfosList() {
        return shotInfosList;
    }

}
