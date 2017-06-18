package pwr.kasztelanic.mediaplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import pwr.kasztelanic.mediaplayer.R;
import pwr.kasztelanic.mediaplayer.helpers.ImageDecoder;
import pwr.kasztelanic.mediaplayer.model.Track;
import pwr.kasztelanic.mediaplayer.model.Utils;

public class SongActivity extends Activity implements View.OnClickListener
{
    @BindView(R.id.sartist)
    protected TextView artistTV;
    @BindView(R.id.salbum)
    protected TextView albumTV;
    @BindView(R.id.stitle)
    protected TextView titleTV;
    @BindView(R.id.send)
    protected TextView endTV;
    @BindView(R.id.snow)
    protected TextView nowTV;
    @BindView(R.id.scover)
    protected ImageView coverIV;
    @BindView(R.id.playBtn)
    protected ImageButton playBtn;
    @BindView(R.id.stopBtn)
    protected ImageButton stopBtn;
    @BindView(R.id.fwrdBtn)
    protected ImageButton fwrdBtn;
    @BindView(R.id.bwrdBtn)
    protected ImageButton bwrdBtn;
    @BindView(R.id.seekBar)
    protected SeekBar seekBar;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String path;
    private MediaPlayer mediaPlayer;
    private int BUTTON_SEEK_MILLISECONDS = 10000;
    private Handler handler;
    private Runnable viewUpdater = new Runnable()
    {
        @Override
        public void run()
        {
            int duration = mediaPlayer.getDuration();
            int currentDuration = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentDuration * seekBar.getMax() / duration);
            nowTV.setText(Utils.durationFromMillis(currentDuration));
            performViewUpdates();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        title = intent.getStringExtra(MainActivity.SONG_TITLE);
        artist = intent.getStringExtra(MainActivity.SONG_ARTIST);
        album = intent.getStringExtra(MainActivity.SONG_ALBUM);
        duration = intent.getLongExtra(MainActivity.SONG_DURATION, 0);
        path = intent.getStringExtra(MainActivity.SONG_PATH);
        titleTV.setText(title);
        artistTV.setText(artist);
        albumTV.setText(album);
        endTV.setText(Utils.durationFromMillis(duration));
        Track track = new Track(path);
        new ImageDecoder().decode(track, coverIV);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try
        {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e)
        {
            Toast.makeText(this, R.string.could_not_load_file, Toast.LENGTH_SHORT).show();
        }
        handler = new Handler();
        playBtn.setOnClickListener(this);
        fwrdBtn.setOnClickListener(this);
        bwrdBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
        mediaPlayer.start();
        mediaPlayer.pause();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(viewUpdater);
    }

    private void playPauseMusic()
    {
        if (mediaPlayer.isPlaying())
            pauseMusic();
        else
            playMusic();
    }

    private void playMusic()
    {
        mediaPlayer.start();
        playBtn.setImageResource(R.mipmap.ic_pause_black_24dp);
        performViewUpdates();
    }

    private void pauseMusic()
    {
        mediaPlayer.pause();
        playBtn.setImageResource(R.mipmap.ic_play_arrow_black_24dp);
    }

    private void stopMusic()
    {
        pauseMusic();
        mediaPlayer.seekTo(0);
    }

    private void seekAudio(int milliseconds)
    {
        if (mediaPlayer.getDuration() > milliseconds && milliseconds > 0)
            mediaPlayer.seekTo(milliseconds);
    }

    private void seekAudioFromCurrentPosition(int milliseconds)
    {

        if (0 > mediaPlayer.getCurrentPosition() + milliseconds)
            mediaPlayer.seekTo(0);
        else if (mediaPlayer.getCurrentPosition() + milliseconds > mediaPlayer.getDuration())
        {
            mediaPlayer.pause();
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        } else
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + milliseconds);
        performViewUpdates();
    }

    private void performViewUpdates()
    {
        handler.postDelayed(viewUpdater, 100);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.playBtn:
                playPauseMusic();
                break;
            case R.id.stopBtn:
                stopMusic();
                break;
            case R.id.fwrdBtn:
                seekAudioFromCurrentPosition(BUTTON_SEEK_MILLISECONDS);
                break;
            case R.id.bwrdBtn:
                seekAudioFromCurrentPosition(-BUTTON_SEEK_MILLISECONDS);
                break;
            default:
                break;
        }
    }

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            handler.removeCallbacks(viewUpdater);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            handler.removeCallbacks(viewUpdater);
            if (seekBar != null)
                seekAudio(seekBar.getProgress() * mediaPlayer.getDuration() / seekBar.getMax());
            performViewUpdates();
        }
    }
}
