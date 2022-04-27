package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AudioRec extends AppCompatActivity {

    final String TAG = "myLogs";

    AudioRecord recorder;
    AudioTrack at;

    Chronometer mChronometr;
    boolean isReading = false;

    TextView txt_status;

    public static final int SAMPLING_RATE = 44100;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT);
    public static final String AUDIO_RECORDING_FILE_NAME = "recording.raw";

    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
            + "/" + AUDIO_RECORDING_FILE_NAME;

    byte audioData[] = new byte[BUFFER_SIZE];
    BufferedOutputStream os;


    //Массив байт для чтения файлов
    byte[] byteData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_rec);

        //Скрыть ActionBar
        getSupportActionBar().hide();

        txt_status = (TextView) findViewById(R.id.txt_status);
        mChronometr = (Chronometer) findViewById(R.id.mChronometr);
        txt_status.setText("Ожидание");

        //Получение разрешений на запись и чтение внешней карты памяти, а также доступ к аудио
        runtimePermission();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Инициализация Класса AudioRecord
        recorder = new AudioRecord(AUDIO_SOURCE,
                SAMPLING_RATE, CHANNEL_IN_CONFIG,
                AUDIO_FORMAT, BUFFER_SIZE);

        os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(filePath));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Файл для записи не найден ", e);
        }
    }

    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        Log.d(TAG, "Все отлично, разрешения получены!");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                        Log.d(TAG, "Все плохо, доступ не хочу тебе давать!");
                    }
                }).check();
    }

    public void Cancel_record(View v) {
        txt_status.setText("Ожидание");
        Log.d(TAG, "Остановка записи с диктофона");
        try {
            os.close();
            recorder.stop();
            recorder.release();
            Log.v(TAG, "Запись сохранена");
            isReading = false;
            mChronometr.stop();
            mChronometr.setBase(SystemClock.elapsedRealtime());
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при очищении класса  AudioRecord", e);
        }
    }

    public void stop_record(View v) {
        txt_status.setText("Пауза");
        Log.d(TAG, "Пауза записи аудио");
        recorder.stop();
        isReading = false;
        mChronometr.stop();
    }

    public void startRecording(View v) {
        txt_status.setText("Идёт запись...");
        isReading = true;

        mChronometr.setBase(SystemClock.elapsedRealtime());
        mChronometr.start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                Log.v(TAG, "Идёт запись...");

                recorder.startRecording();

                while (isReading) {
                    int status = recorder.read(audioData, 0, audioData.length);

                    if (status == AudioRecord.ERROR_INVALID_OPERATION ||
                            status == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "Ошибка при чтении аудиоданных!");
                        return;
                    }

                    try {
                        os.write(audioData, 0, audioData.length);
                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка сохранения записи! ", e);
                        return;
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isReading = false;
        if (recorder != null) {
            recorder.release();
        }

        if (at != null) {
            at.release();
        }

        txt_status.setText("Ожидание");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isReading = false;
        if (recorder != null) {
            recorder.release();
        }

        if (at != null) {
            at.pause();
        }

        txt_status.setText("Ожидание");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        recorder = new AudioRecord(AUDIO_SOURCE,
                SAMPLING_RATE, CHANNEL_IN_CONFIG,
                AUDIO_FORMAT, BUFFER_SIZE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isReading = false;
        if (recorder != null) {
            recorder.release();
        }

        if (at!=null) {
            at.pause();
        }

        txt_status.setText("Ожидание");
    }

    public void PlayShortAudioFileViaAudioTrack(View v) throws Exception {
        txt_status.setText("Воспроизведение");

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (filePath == null)
                    return;

                //Reading the file..
                byte[] byteData = null;

                File file = null;

                file = new File(filePath);

                byteData = new byte[(int) file.length()];

                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    in.read(byteData);
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Set and push to audio track..
                int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
                if (at != null) {
                    at.play();
                    // Write the byte array to the track
                    at.write(byteData, 0, byteData.length);
                    at.stop();
                    at.release();
                    at = null;
                } else
                    Log.d("TCAudio", "Ошибка инициализации класса AudioTrack ");

            }
        }).start();
    }

    public void stop_audio(View v) {
        if (at!=null) {
            txt_status.setText("Ожидание");
            at.pause();
        }
    }

}