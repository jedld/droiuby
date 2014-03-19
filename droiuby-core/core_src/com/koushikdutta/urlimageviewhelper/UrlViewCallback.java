package com.koushikdutta.urlimageviewhelper;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public interface UrlViewCallback {
    void onLoaded(View imageView, Drawable loadedDrawable, String url, String method, boolean loadedFromCache);
}
