package com.example.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnplay, btnnext, btnprev, btnfr, btnff, btnreplay;
    TextView txtsname, txtstart, txtsstop;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    Boolean repeat = false;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<String> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null)
        {
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer.isPlaying())
        {
            btnplay.setBackgroundResource(R.drawable.ic_play);
            mediaPlayer.pause();
        }
        else
        {
            btnplay.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Вызов функции инициализации
        initView();

        getSupportActionBar().hide();getSupportActionBar().hide();

        if (mediaPlayer != null)
        {
            //Остановка воспроизведения
            mediaPlayer.stop();
            //Удаляем все загруженные звуки из MediaPlayer и освобождаем память
            mediaPlayer.release();
        }

        //Прием интента от MusicPlayer с данными о выбранной песне
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //Получает из Бандла лист с песнями
        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        //Получение названия песни
        String songName = intent.getStringExtra("songname");
        //Получение позициии в списке с песнями
        position = bundle.getInt("pos", 0);
        txtsname.setSelected(true);

        ImgLogo(position);

        int id = this.getResources().getIdentifier(songName, "raw", "com.example.audioplayer");
        //Выводим на экран название песни
        sname = mySongs.get(position);
        txtsname.setText(sname);

        //Создание и запус MediaPlayer
        mediaPlayer = MediaPlayer.create(getApplicationContext(), id);
        mediaPlayer.start();

        //Поток для обновления SeekBar
        updateseekbar = new Thread()
        {
            @Override
            public void run() {
                //Получаем продолжительность файла
                int totalDuration = mediaPlayer.getDuration();
                //Переменная для текущей продолжительности
                int currentPosition = 0;

                //Выполняем до тех пор пока текущая продолжительность меньше полной
                while (currentPosition<totalDuration)
                {
                    try {
                        sleep(500);
                        //Получаем текущую продолжительность
                        currentPosition = mediaPlayer.getCurrentPosition();
                        //Установка ползунка SeekBar
                        seekmusic.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };

        //Задаем максимальное значение SeekBar
        seekmusic.setMax(mediaPlayer.getDuration());
        //Запуск потока обновления SeekBar
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //переход к заданной позиции
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        //Вывод и установка конечного времени
        String endTime =createTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtstart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        //Слушатель для кнопки play/pause
        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else
                {
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        //Слушатель для кнопки next
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                sname = mySongs.get(position);
                int id = getResources().getIdentifier(sname, "raw", "com.example.audioplayer");
                mediaPlayer = MediaPlayer.create(getApplicationContext(), id);
                txtsname.setText(sname);

                //Вывод и установка конечного времени
                String endTime =createTime(mediaPlayer.getDuration());
                txtsstop.setText(endTime);


                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId != -1)
                {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        //Слушатель для кнопки prev
        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                sname = mySongs.get(position);
                int id = getResources().getIdentifier(sname, "raw", "com.example.audioplayer");
                mediaPlayer = MediaPlayer.create(getApplicationContext(), id);
                txtsname.setText(sname);

                //Вывод и установка конечного времени
                String endTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(endTime);


                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId != -1)
                {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnreplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeat){
                    repeat = false;
                    btnreplay.setBackgroundResource(R.drawable.ic_repeat);
                } else {
                    repeat = true;
                    btnreplay.setBackgroundResource(R.drawable.ic_repeat_one);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(repeat){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    sname = mySongs.get(position);
                    int id = getResources().getIdentifier(sname, "raw", "com.example.audioplayer");
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), id);
                    txtsname.setText(sname);
                    mediaPlayer.start();
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    startAnimation(imageView);
                    int audiosessionId = mediaPlayer.getAudioSessionId();
                    if(audiosessionId != -1)
                    {
                        visualizer.setAudioSessionId(audiosessionId);
                    }
                } else {
                    btnnext.performClick();
                }
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if(audiosessionId != -1)
        {
            visualizer.setAudioSessionId(audiosessionId);
        }

        //Слушатель для кнопки перемотки вперед
        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        //Слушатель для кнопки перемотки назад
        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
            }
        });
    }

    //Функция инициализации
    private void initView(){
        btnprev = findViewById(R.id.btnprev);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        btnplay = findViewById(R.id.playbtn);
        btnnext = findViewById(R.id.btnnext);
        txtsname = findViewById(R.id.txtsn);
        txtstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        visualizer = findViewById(R.id.blast);
        seekmusic = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageview);
        btnreplay = findViewById(R.id.btn_replay);
    }

    //Функция для расчета времени для SeekBar
    private String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";
        if (sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }

    private void ImgLogo(int position){
        switch (position){
            case 0:
                imageView.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.believer));
                break;
            case 1:
                imageView.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.enemy));
                break;
            case 2:
                imageView.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.mando));
                break;
            case 3:
                imageView.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.mijuice));
                break;
            default:
                imageView.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.logo));
                break;
        }
    }

    private void startAnimation(View view) {

        ImgLogo(position);

        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
}