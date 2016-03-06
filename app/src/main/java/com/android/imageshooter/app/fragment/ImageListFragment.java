package com.android.imageshooter.app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.agilie.dribbblesdk.domain.Shot;
import com.agilie.dribbblesdk.service.retrofit.DribbbleServiceGenerator;
import com.android.imageshooter.app.R;
import com.android.imageshooter.app.Utils.ShotInfos;
import com.android.imageshooter.app.Utils.FeedReaderDBHelper;
import com.android.imageshooter.app.Utils.ShotPathString;
import com.android.imageshooter.app.activity.MainActivity;
import com.android.imageshooter.app.async.ReadDBAsync;
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

public class ImageListFragment extends Fragment {

    private static final String DRIBBBLE_CLIENT_ACCESS_TOKEN = "28499cacc1e937ae8a611ee402c4900fe97ce0b5bc536c53df67e20ca78126d6";
    private static int NUMBER_OF_PAGES = 1;
    private static final int SHOTS_PER_PAGE = 50;
    private static String SORT_SHOTS = Shot.SORT_RECENT;

    private SwipeRefreshLayout swipeContainer;
    protected AbsListView listView;
    protected ArrayList<ShotInfos> shotInfosList;
    FeedReaderDBHelper mDbHelper;

    protected boolean pauseOnScroll = false;
    protected boolean pauseOnFling = true;

    ImageListFragment imageListFragment;

    HashMap<String, Object> queryMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDbHelper = ((MainActivity)getActivity()).getmDbHelper();

        View rootView = inflater.inflate(R.layout.fg_image_list, container, false);
        imageListFragment = this;
        shotInfosList = new ArrayList<ShotInfos>();
        listView = (ListView) rootView.findViewById(android.R.id.list);

        ((ListView) listView).setAdapter(new ImageAdapter(getActivity(), imageListFragment));

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);

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

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
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

    public void getNextImages(){
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
                    if(getActivity() != null) {
                        ((ListView) listView).setAdapter(new ImageAdapter(getActivity(), imageListFragment));
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
            Toast.makeText(getActivity(), "ShotsFromCache", Toast.LENGTH_SHORT);
            if(shotInfosList == null || shotInfosList.size() == 0) {
                getShotsInfoFromDB();
            }else if(swipeContainer != null & swipeContainer.isRefreshing())
                swipeContainer.setRefreshing(false);
        }

    }

    public ArrayList<ShotInfos> getShotInfosList() {
        return shotInfosList;
    }

    private static class ImageAdapter extends BaseAdapter {

        private DisplayImageOptions options;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private LayoutInflater inflater;
        ImageListFragment fragment;

        public ImageAdapter(Context context, ImageListFragment fragment) {
            inflater = LayoutInflater.from(context);
            this.fragment = fragment;
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

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(fragment.getActivity()));
        }

        @Override
        public int getCount() {
            return fragment.shotInfosList.size();
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

            if (fragment.shotInfosList.get(position).getDescription() != null) {
                holder.textDesc.setText(Html.fromHtml(fragment.shotInfosList.get(position).getDescription()));
                holder.textDesc.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else
                holder.textDesc.setVisibility(View.INVISIBLE);
            holder.textTitle.setText(fragment.shotInfosList.get(position).getTitle());

            ImageLoader.getInstance().displayImage(fragment.shotInfosList.get(position).getURL(), holder.image, options, animateFirstListener);

            return view;
        }


    }

    static class ViewHolder {
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

    private void getShotsInfoFromDB(){
        ReadDBAsync readDBAsync = new ReadDBAsync();
        readDBAsync.link((MainActivity)getActivity());
        readDBAsync.execute();
        synchronized(mDbHelper) {
            try {
                Log.i("wait-notify", "wait from Frag read");
                mDbHelper.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(swipeContainer != null & swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
            listView.setSelection(((MainActivity) getActivity()).getCurrentPos());
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public AbsListView getListView() {
        return listView;
    }

    public void setListView(AbsListView listView) {
        this.listView = listView;
    }

    public static String getSortShots() {
        return SORT_SHOTS;
    }

    public static void setSortShots(String sortShots) {
        SORT_SHOTS = sortShots;
    }

    public static int getNumberOfPages() {
        return NUMBER_OF_PAGES;
    }

    public static void setNumberOfPages(int numberOfPages) {
        NUMBER_OF_PAGES = numberOfPages;
    }
}
