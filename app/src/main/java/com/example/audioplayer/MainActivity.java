package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton btn_dict, btn_music_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        getSupportActionBar().hide();

        //Слушатель для кнопки музыкального плеера
        btn_music_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Создание интента для перехода к странице с музыкальным плеером
                Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
                startActivity(intent);
            }
        });

        //Слушатель для кнопки диктофона
        btn_dict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Создание интента для перехода к странице с диктофоном
                Intent intent = new Intent(MainActivity.this,  AudioRec.class);
                startActivity(intent);

            }
        });

    }

    //Функция инициализации View элементов
    private void init(){
        //Кнопка для перехода в часть приложения с диктофоном
        btn_dict = (ImageButton) findViewById(R.id.btn_dict);
        //Кнопка для перехода в часть приложения с музыкальным плеером
        btn_music_play = (ImageButton) findViewById(R.id.btn_music_play);
    }
}