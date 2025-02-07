package com.adobe.wknd.androidapp.loader;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

import com.adobe.wknd.androidapp.config.Config;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Prepares remote images in a cache to be used in TextView and/or ImageView UI elements.
 */
public class RemoteImagesCache implements Html.ImageGetter {

    private static final RemoteImagesCache INSTANCE = new RemoteImagesCache();
    private final Map<String, Drawable> drawablesByPath = new HashMap<>();

    public static RemoteImagesCache getInstance() {
        return INSTANCE;
    }

    @Override
    public Drawable getDrawable(String path) {
        Drawable drawable = drawablesByPath.get(path);
        return drawable;
    }

    public void prepareDrawableFor(Config config, String path) {
        if (drawablesByPath.containsKey(path)) {
            return;
        }
        String urlStr = config.getContentApiEndpoint() + path;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (config.getContentApiUser() != null) {
                String auth = config.getContentApiUser() + ":" + config.getContentApiPassword();
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuth));
            }
            InputStream is = (InputStream) connection.getContent();
            Drawable drawable = Drawable.createFromStream(is, new File(path).getName());
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawablesByPath.put(path, drawable);

            Log.i("RemoteImagesCache", "Loaded drawable for " + path);
        } catch (Exception e) {
            Log.e("RemoteImagesCache", "Exception while loading " + urlStr + ": " + e, e);
        }
    }

}
