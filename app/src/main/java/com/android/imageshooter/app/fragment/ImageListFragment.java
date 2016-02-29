package com.android.imageshooter.app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.agilie.dribbblesdk.domain.Shot;
import com.agilie.dribbblesdk.service.retrofit.DribbbleServiceGenerator;
import com.android.imageshooter.app.R;
import com.android.imageshooter.app.ShotInfos;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ImageListFragment extends Fragment {

    private static final String DRIBBBLE_CLIENT_ACCESS_TOKEN = "28499cacc1e937ae8a611ee402c4900fe97ce0b5bc536c53df67e20ca78126d6";
    private static int NUMBER_OF_PAGES = 1;
    private static final int SHOTS_PER_PAGE = 50;

    private SwipeRefreshLayout swipeContainer;
    protected AbsListView listView;
    protected ArrayList<ShotInfos> shotInfosList;

    protected boolean pauseOnScroll = false;
    protected boolean pauseOnFling = true;

    ImageListFragment imageListFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        return rootView;
        //return super.onCreateView(inflater, container, savedInstanceState);
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

    void getNextImages(){
        Call<List<Shot>> shotsCall = DribbbleServiceGenerator
                .getDribbbleShotService(DRIBBBLE_CLIENT_ACCESS_TOKEN)
                .fetchShots(NUMBER_OF_PAGES, SHOTS_PER_PAGE);
        shotsCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Response<List<Shot>> response) {
                List<Shot> shots = response.body();
                shotInfosList.clear();
                for (Shot shot : shots) {
                    shotInfosList.add(new ShotInfos(shot.getDescription(), shot.getTitle(), shot.getImages().getHidpi()));
                }
                //intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageListFragment.INDEX);
                NUMBER_OF_PAGES++;
                ((ListView) listView).setAdapter(new ImageAdapter(getActivity(), imageListFragment));
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Throwable t) {
                swipeContainer.setRefreshing(false);
            }
        });


    }

    private static class ImageAdapter extends BaseAdapter {

        private DisplayImageOptions options;
        //private static final String[] IMAGE_URLS = new String[50];
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private LayoutInflater inflater;
        //Context context;
        ImageListFragment fragment;

        public ImageAdapter(Context context, ImageListFragment fragment) {
            inflater = LayoutInflater.from(context);
            this.fragment = fragment;
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                    .build();

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(fragment.getActivity()));
            //this.context = context;
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

            holder.textDesc.setText(fragment.shotInfosList.get(position).getDescription());
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
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }

    }

    private void applyScrollListener() {
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll, pauseOnFling));
    }
}
