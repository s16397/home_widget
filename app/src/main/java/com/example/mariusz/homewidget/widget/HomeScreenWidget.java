package com.example.mariusz.homewidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.mariusz.homewidget.R;
import com.example.mariusz.homewidget.image.ImageService;
import com.example.mariusz.homewidget.music.MusicPlayerService;

/**
 * Implementation of App Widget functionality.
 */
public class HomeScreenWidget extends AppWidgetProvider {

    private static final String TAG = "HomeScreenWidget";
    public static final String ACTION_PLAY = "com.example.mariusz.homewidget.MUSIC_PLAYER_PLAY";
    public static final String ACTION_PAUSE = "com.example.mariusz.homewidget.MUSIC_PLAYER_PAUSE";
    public static final String ACTION_NEXT_TRACK = "com.example.mariusz.homewidget.MUSIC_PLAYER_NEXT";
    public static final String ACTION_PREV_TRACK = "com.example.mariusz.homewidget.MUSIC_PLAYER_PREV";
    public static final String ACTION_WEB_BROWSER = "com.example.mariusz.homewidget.WEB_BROWSER";
    public static final String ACTION_CHANGE_IMAGE = "com.example.mariusz.homewidget.CHANGE_IMAGE";
    private static final String WEB_ADDRESS = "WEB_ADDRESS";
    private static int counter = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.home_screen_widget);

        createWebBrowserPendingIntent(context, remoteViews, "https://www.onet.pl",
                R.id.web_button );

        createChangeImagePendingIntent(context, remoteViews, R.id.image_button);

        createMusicPlayerActionPendingIntent(context, remoteViews, ACTION_PLAY, R.id.play_button);
        createMusicPlayerActionPendingIntent(context, remoteViews, ACTION_PAUSE, R.id.pause_button);
        createMusicPlayerActionPendingIntent(context, remoteViews, ACTION_NEXT_TRACK,
                R.id.next_button);
        createMusicPlayerActionPendingIntent(context, remoteViews, ACTION_PREV_TRACK,
                R.id.prev_button);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d(TAG,"Received ACTION: " + action);
        if (action.contains("MUSIC_PLAYER")) {
            Intent musicPlayerIntent = new Intent(context, MusicPlayerService.class);
            musicPlayerIntent.setAction(action);
            context.startService(musicPlayerIntent);
        } else if (action == ACTION_WEB_BROWSER) {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(intent.getStringExtra(WEB_ADDRESS)));
            context.startActivity(viewIntent);
        } else if(action == ACTION_CHANGE_IMAGE) {
            Intent changeImageIntent = new Intent(context, ImageService.class);
            changeImageIntent.setAction(action);
            context.startService(changeImageIntent);
        }
        ComponentName homeWidget = new ComponentName(context, HomeScreenWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(homeWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void createMusicPlayerActionPendingIntent(Context context, RemoteViews remoteViews,
                                                      String action, int viewId) {
        int temp = counter++;
        Intent musicPlayerIntent = new Intent(context, HomeScreenWidget.class);
        musicPlayerIntent.setAction(action);
        PendingIntent musicPlayerPendingIntent = PendingIntent.getBroadcast(context,
                temp, musicPlayerIntent, 0);
        remoteViews.setOnClickPendingIntent(viewId, musicPlayerPendingIntent);
    }

    private void createWebBrowserPendingIntent(Context context, RemoteViews remoteViews,
                                               String webAddress, int viewId) {

        Intent webBrowserIntent = new Intent(context, HomeScreenWidget.class);
        webBrowserIntent.setAction(ACTION_WEB_BROWSER);
        webBrowserIntent.putExtra(WEB_ADDRESS, webAddress);
        PendingIntent webBrowserPendingIntent = PendingIntent.getBroadcast(context, counter++,
                webBrowserIntent, 0);
        remoteViews.setOnClickPendingIntent(viewId, webBrowserPendingIntent);
    }

    private void createChangeImagePendingIntent(Context context, RemoteViews remoteViews, int viewId) {
        Intent changeImageIntent = new Intent(context, HomeScreenWidget.class);
        changeImageIntent.setAction(ACTION_CHANGE_IMAGE);
        PendingIntent changeImagePendingIntent = PendingIntent.getBroadcast(context, counter++,
                changeImageIntent, 0);
        remoteViews.setOnClickPendingIntent(viewId, changeImagePendingIntent);
    }

}

