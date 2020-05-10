package com.example.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG ="MainActivity.java" ;
    Intent takePictureIntent;
    String currentPhotoPath, currentVideoPath;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private MediaPlayer player = null;
    private MediaRecorder recorder = null;
    private Button   playButton = null;
    private Button recordButton = null;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    boolean mStartPlaying = true;
    boolean mStartRecording = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final Button btnFoto = findViewById(R.id.bt_foto);
        final Button btnVideo = findViewById(R.id.bt_video);
        playButton = findViewById(R.id.btn_reproduzir);
        recordButton = findViewById(R.id.btn_gravar);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startCameraFoto();
            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startCameraVideo();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    playButton.setText("Stop playing");
                } else {
                    playButton.setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
            }
        );

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    recordButton.setText("Stop recording");
                } else {
                    recordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                File file = new File(currentPhotoPath);
                if (file.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    ImageView imageView = new ImageView(this);
                    myBitmap = RotateBitmap(myBitmap, 90);
                    imageView.setImageBitmap(myBitmap);
                    builder.setView(imageView).show();
                }
            }
            if (requestCode == 2 && resultCode == RESULT_OK) {
                VideoView videoView = new VideoView(this);
                File file = new File(currentVideoPath);
                videoView.setVideoPath(file.getAbsolutePath());
                videoView.start();
                builder.setView(videoView).show();
            }
        }catch(Exception e){

        }
    }

    private File saveFile(int tipo) {
        try {
            if (tipo == 1) {
                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoPath = image.getAbsolutePath();
                return image;
            } else if (tipo == 2) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "mp4" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File video = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".mp4",         /* suffix */
                        storageDir      /* directory */
                );
                currentVideoPath = video.getAbsolutePath();
                return video;
            } else{
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String soundFile = "mp3_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File sound = File.createTempFile(
                        soundFile,  /* prefix */
                        ".3gp",         /* suffix */
                        storageDir      /* directory */
                );
                fileName = sound.getAbsolutePath();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void startCameraFoto() {
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = saveFile(1);
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.camera",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void startCameraVideo(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File photoFile = null;
        photoFile = saveFile(2);
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.camera",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            startActivityForResult(intent, 2);
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        saveFile(3);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

}
