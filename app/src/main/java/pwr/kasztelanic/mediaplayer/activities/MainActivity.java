package pwr.kasztelanic.mediaplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pwr.kasztelanic.mediaplayer.R;
import pwr.kasztelanic.mediaplayer.adapter.SongAdapter;
import pwr.kasztelanic.mediaplayer.listener.RecyclerTouchListener;
import pwr.kasztelanic.mediaplayer.model.MediaManager;
import pwr.kasztelanic.mediaplayer.model.Track;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends Activity implements SensorEventListener
{
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int SENSOR_SENSITIVITY = 4;
    private static final long VIBRATE_LENGTH = 50;
    public static final String SONG_TITLE = "song_title";
    public static final String SONG_ARTIST = "song_artist";
    public static final String SONG_ALBUM = "song_album";
    public static final String SONG_TRACK = "song_track";
    public static final String SONG_DURATION = "song_duration";
    public static final String SONG_COVER = "song_cover";
    public static final String SONG_PATH = "song_path";
    private List<Track> songList = new ArrayList<>();
    private Random random = new Random();
    private RecyclerView recyclerView;
    private SongAdapter sAdapter;
    private MediaManager mediaManager;
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor proximity;

    private boolean checkAndRequestPermissions()
    {
        if (SDK_INT >= Build.VERSION_CODES.M)
        {
            int permissionStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permissionStorage != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!listPermissionsNeeded.isEmpty())
            {
                requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            } else
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();
        loadMusic();
        setUpRecyclerView();
        setUpProximitySensor();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.about_mi:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView()
    {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        sAdapter = new SongAdapter(this, songList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(sAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new MovieTouchlistener()));
    }

    private void setUpProximitySensor()
    {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    private void loadMusic()
    {
        mediaManager = new MediaManager(this);
        songList = mediaManager.getMp3Files();
    }


    private void startSongActivity(Context context, int position)
    {
        Intent i = new Intent(context, SongActivity.class);
        i.putExtra(MainActivity.SONG_ALBUM, songList.get(position).getAlbum());
        i.putExtra(MainActivity.SONG_ARTIST, songList.get(position).getArtist());
        i.putExtra(MainActivity.SONG_TITLE, songList.get(position).getTitle());
        i.putExtra(MainActivity.SONG_TRACK, songList.get(position).getTrack());
        i.putExtra(MainActivity.SONG_DURATION, songList.get(position).getDuration());
        i.putExtra(MainActivity.SONG_COVER, songList.get(position).getCover());
        i.putExtra(MainActivity.SONG_PATH, songList.get(position).getPath());
        startActivity(i);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY)
        {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY)
            {
                int randomSong = random.nextInt(songList.size());
                vibrator.vibrate(VIBRATE_LENGTH);
                startSongActivity(this, randomSong);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    private class MovieTouchlistener implements RecyclerTouchListener.ClickListener
    {
        @Override
        public void onClick(View view, int position)
        {
            startSongActivity(view.getContext(), position);
        }

        @Override
        public void onLongClick(View view, int position)
        {
        }
    }
}
