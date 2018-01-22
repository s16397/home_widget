package com.example.mariusz.homewidget.image;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.example.mariusz.homewidget.R;
import com.example.mariusz.homewidget.widget.HomeScreenWidget;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ImageService extends Service {

    private static final String TAG = "ImageService";
    private List<Integer> imagesIds;
    private int currentImageIndex = 0;

    private final Map<String, Consumer<Intent>> ACTION_TO_HANDLER_MAPPER =
            ImmutableMap.<String, Consumer<Intent>>builder()
                    .put(HomeScreenWidget.ACTION_CHANGE_IMAGE, this::handleChangeImage)
                    .build();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        ACTION_TO_HANDLER_MAPPER.getOrDefault(action, this::handleDefault).accept(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleChangeImage(Intent intent) {
        if (imagesIds == null) {
            imagesIds = loadImagesIds();
            currentImageIndex = imagesIds.size() - 1;
            setWidgetImageView(currentImageIndex);
        } else {
            setNextWidgetImageView();
        }
    }

    private void handleDefault(Intent intent) {

    }

    private void setWidgetImageView(int imageIndex) {
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.home_screen_widget);
        ComponentName thisWidget = new ComponentName(context, HomeScreenWidget.class);
        int imageResourceId = imagesIds.get(imageIndex);
        remoteViews.setImageViewResource(R.id.imageView, imageResourceId);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    private void setNextWidgetImageView() {
        currentImageIndex = (currentImageIndex + 1) % imagesIds.size();
        setWidgetImageView(currentImageIndex);
    }

    private List<Integer> loadImagesIds() {
        List<Integer> imagesIds = new ArrayList<>();
        Field[] fields = R.mipmap.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Log.d(TAG,"LOAD FILE: " +fields[i].getName());
                imagesIds.add(fields[i].getInt(fields[i]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Loaded imageIds: " + imagesIds);
        return imagesIds;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
