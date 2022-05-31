package com.alamondai.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {


    Button btnPlay, btnNext, btnPrevious, btnFastForward, btnFastBackWard;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;



    ImageView imageView;


    String songName;

    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;

    ArrayList<File> mySongs;

    Thread updateSeekBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        btnPlay = (Button) findViewById(R.id.BtnPlay);
        btnNext = (Button) findViewById(R.id.BtnNext);
        btnPrevious = (Button) findViewById(R.id.BtnPrevious);
        btnFastForward = (Button) findViewById(R.id.BtnFastForward);
        btnFastBackWard = (Button) findViewById(R.id.BtnFastRewind);


        txtSongName = (TextView) findViewById(R.id.SongTxt);
        txtSongStart = (TextView) findViewById(R.id.TxtSongStart);
        txtSongEnd = (TextView) findViewById(R.id.TxtSongEnd);

        seekMusicBar = (SeekBar) findViewById(R.id.SeekBar);


        imageView = (ImageView) findViewById(R.id.MusicImage);



        if (mediaPlayer != null) {


            mediaPlayer.start();
            mediaPlayer.release();
        }


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getIntegerArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos");
        txtSongName.setSelected(true);



        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);


        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();


        songEndTime();

        updateSeekBar = new Thread() {
            @Override
            public void run() {

                int TotalDuration = mediaPlayer.getDuration();
                int CurrentPosition = 0;

                while (CurrentPosition < TotalDuration) {
                    try {

                        sleep(500);
                        CurrentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(CurrentPosition);

                    } catch (InterruptedException | IllegalStateException e) {

                        e.printStackTrace();
                    }
                }

            }
        };


        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();


        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });


        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                String currentTime = createDuration(mediaPlayer.getCurrentPosition());


                txtSongStart.setText(currentTime);
                handler.postDelayed(this, delay);

            }
        }, delay);



        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (mediaPlayer.isPlaying()) {

                    btnPlay.setBackgroundResource(R.drawable.play_song_icon);

                    mediaPlayer.pause();

                } else {


                    btnPlay.setBackgroundResource(R.drawable.pause_song_icon);


                    mediaPlayer.start();


                    TranslateAnimation moveAnim = new TranslateAnimation(-25, 25, -25, 25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(1);


                    imageView.startAnimation(moveAnim);

                }
            }
        });


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mediaPlayer.stop();
                mediaPlayer.release();


                position = ((position + 1) % mySongs.size());

                Uri uri1 = Uri.parse(mySongs.get(position).toString());

                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);


                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);

                mediaPlayer.start();

                songEndTime();

                startAnimation(imageView, 360f);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position - 1) % mySongs.size());
                if (position < 0)
                    position = mySongs.size() - 1;

                Uri uri1 = Uri.parse(mySongs.get(position).toString());

                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                songEndTime();

                startAnimation(imageView, -360f);

            }

        });

        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);

                }
            }
        });

        btnFastBackWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {

                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);

                }
            }
        });

    }

    public void startAnimation(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);
        objectAnimator.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();

    }
    public String createDuration(int duration) {

        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time = time + min + ":";

        if (sec < 10) {

            time += "0";

        }
        time += sec;
        return time;

    }

    public void songEndTime() {
        String endTime = createDuration(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);
    }
}
