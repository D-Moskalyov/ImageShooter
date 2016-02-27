package com.android.imageshooter.app;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.agilie.dribbblesdk.domain.Shot;
import com.agilie.dribbblesdk.service.auth.AuthCredentials;
import com.agilie.dribbblesdk.service.auth.DribbbleAuthHelper;
import com.agilie.dribbblesdk.service.auth.DribbbleConstants;
import com.agilie.dribbblesdk.service.retrofit.DribbbleServiceGenerator;
import com.google.api.client.auth.oauth2.Credential;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    //private static final String DRIBBBLE_CLIENT_ID = "1cb175a8b3171b2084b2d03f036928bf8fb09f09a2b4fdff109744fc774ea112";
    //private static final String DRIBBBLE_CLIENT_SECRET = "9667b3a0904bdff92648ee8341d3ce8c93ec4b857751b00063519886e9db4490";
    private static final String DRIBBBLE_CLIENT_ACCESS_TOKEN = "28499cacc1e937ae8a611ee402c4900fe97ce0b5bc536c53df67e20ca78126d6";
    //private static final String DRIBBBLE_CLIENT_REDIRECT_URL = "";

    //String authToken;

    private static final int NUMBER_OF_PAGES = 1;
    private static final int SHOTS_PER_PAGE = 50;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
        .build();

        ImageLoader.getInstance().init(config);

        setContentView(R.layout.activity_main);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNextImages();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void getNextImages(){
        Call<List<Shot>> shotsCall = DribbbleServiceGenerator
                .getDribbbleShotService(DRIBBBLE_CLIENT_ACCESS_TOKEN)
                .fetchShots(NUMBER_OF_PAGES, SHOTS_PER_PAGE);
        shotsCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Response<List<Shot>> response) {
                List<Shot> shots = response.body();
;                for (Shot shot : shots) {
                    String GsonString = shot.toGson();
                    GsonString = "";
                }
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Throwable t) {
                swipeContainer.setRefreshing(false);
            }
        });


    }
}
