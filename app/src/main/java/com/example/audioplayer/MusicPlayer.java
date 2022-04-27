package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends AppCompatActivity {

    ListView listView;
    String[] items;
    MediaPlayer mPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        //Инициализация View
        init();

        //Скрытие ActionBar
        getSupportActionBar().hide();

        //Получения разрешения на запись и воспроизведение и вызов функции, для получения всех mp3 файлов папки res/raw
        runtimePermission();
    }

    //Функция инициализации View элементов
    private void init(){
        listView = (ListView) findViewById(R.id.listViewSong);
    }

    //Функции для получения mp3 файлов папки res/raw
    public ArrayList<String> findSong (Field[] fields) {
        ArrayList<String> arrayList = new ArrayList<>();

        //Цикл для заполнения листа с песнями
        for(int count=0; count < fields.length; count++){

                String filename = fields[count].getName();
                int id = getApplicationContext().getResources().getIdentifier(filename, "raw", "com.example.audioplayer");
                Log.i("Raw ID: ", String.valueOf(id));

                arrayList.add(filename);

                Log.i("Raw Asset: ", fields[count].getName());
        }

        return  arrayList;
    }

    //Функции вывода mp3 файлов папки res/raw
    void displaySongs() {

        //Лист с файлами песен из res/raw
        final ArrayList<String> mySongs = findSong(R.raw.class.getFields());

        //Задаем количество элементов в строковом массиве
        items = new String[mySongs.size()];

        //Заполнение массива с названиями песен
        for (int i = 0; i < mySongs.size(); i++)
        {
            items[i] = mySongs.get(i);
            Log.i("Items: ", mySongs.get(i));
        }

        customAdapter customAdapter = new customAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String songname = (String) listView.getItemAtPosition(position);
                String songname = mySongs.get(position);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songname", songname)
                        .putExtra("pos",position));
            }
        });

    }

    public void runtimePermission()
    {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }




    class customAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View myView = getLayoutInflater().inflate(R.layout.list_item,null);
            TextView textsong = myView.findViewById(R.id.txtsongname);
            ImageView imgsong = myView.findViewById(R.id.imgsong);
            imgsong.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_music));
            textsong.setSelected(true);
            textsong.setText(items[i]);

            return myView;
        }
    }
}

