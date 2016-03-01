package com.android.imageshooter.app.Utils;


import com.agilie.dribbblesdk.domain.Shot;

public class ShotPathString {

    public static String getShotPathString(Shot shot){
        String path;
        path = shot.getImages().getHidpi();
        if(path == null || path.equals("")) {
            path = shot.getImages().getNormal();
            if(path == null || path.equals("")) {
                path = shot.getImages().getTeaser();
            }
        }
        return path;
    }
}
