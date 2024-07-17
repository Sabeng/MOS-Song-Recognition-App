package com.example.mos;

//Melisa Karadağ
//Oğulcan Tunç Tayan
//Sude Önder

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 44100;
    private static final int RECORD_SECONDS = 15;

    private static final String SERVER_IP = "10.0.2.2";

    // 10.0.2.2 is an Embedded IP that indicates that the Android Studio Emulator 
    // should go to the localhost of the PC running the development environment.

    // This value should be replaced with the IP of the connected network (also should be same as server's ip)

    private static final int SERVER_PORT = 5432;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private static final String TAG = "MainActivity";

    private Button buttonLetsMos;
    private ImageView imageStar;
    private LinearLayout containerSongInfo;
    private TextView textSongName, textAlbum, textArtist;
    private ValueAnimator animator;
    private TextView text1, text2, text3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button recordButton = findViewById(R.id.recordButton);

        applyGradientToText(recordButton, "Let's MOS");


        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);

        startTextLinearAnimation(text1);
        startTextLinearAnimation(text2);
        startTextLinearAnimation(text3);



        recordButton.setOnClickListener(v -> {
            if (permissionToRecordAccepted) {
                startPulseAnimation();
                startGradientAnimation();
                recordButton.setText("Listening...");
                new RecordAndSendTask().execute();
            }
        });
    }
    
    private void animateText(final TextView textView, int startOffset, final Runnable onAnimationEnd) {
        Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideIn.setStartOffset(startOffset);
        slideIn.setDuration(1000);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        textView.startAnimation(slideIn);
    }
    private void applyGradientToText(Button textView, String text) {
        textView.setText(text);
        textView.measure(0, 0);
        int width = textView.getMeasuredWidth();

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{0xFFffffff, 0xFF8b0000}, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);
    }

    private void startGradientAnimation() {
        final Button button = findViewById(R.id.recordButton);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                button.measure(0, 0);
                int width = button.getMeasuredWidth();
                float translationX = animatedValue * width;

                Shader textShader = new LinearGradient(translationX, 0, translationX + width, button.getTextSize(),
                        new int[]{0xFFffffff, 0xFF8b0000}, null, Shader.TileMode.CLAMP);
                button.getPaint().setShader(textShader);
                button.invalidate();
            }
        });

        animator.start();

        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                animator.cancel();
            }
        }, 15000);
    }

    private void startTextLinearAnimation(TextView text) {
        final Button button = findViewById(R.id.recordButton);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(4000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                text.measure(0, 0);
                int width = text.getMeasuredWidth();
                float translationX = animatedValue * width;

                Shader textShader = new LinearGradient(translationX, 0, translationX + width, button.getTextSize(),
                        new int[]{0xFFffffff, 0xFF8b0000}, null, Shader.TileMode.CLAMP);
                text.getPaint().setShader(textShader);
                text.invalidate();
            }
        });

        animator.start();

    }

    private void startPulseAnimation() {
        final Button button = findViewById(R.id.recordButton);

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.2f, 
                1.0f, 1.2f, 
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f); 
        scaleAnimation.setDuration(500); 
        scaleAnimation.setRepeatCount(Animation.INFINITE); 
        scaleAnimation.setRepeatMode(Animation.REVERSE); 

        button.startAnimation(scaleAnimation);

        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.clearAnimation();
                
                button.setScaleX(1.0f);
                button.setScaleY(1.0f);
            }
        }, 15000);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    private class RecordAndSendTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            Socket socket = null;
            OutputStream outputStream = null;
            AudioRecord audioRecord = null;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                outputStream = new BufferedOutputStream(socket.getOutputStream());

                int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
                    return"";
                }
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

                //bWVsaXNhb2d1bGNhbnN1ZGU=
                byte[] buffer = new byte[minBufferSize];
                audioRecord.startRecording();

                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < RECORD_SECONDS * 1000) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                outputStream.flush();

                byte[] responseBuffer = new byte[1024];
                int read = socket.getInputStream().read(responseBuffer);
                if (read > 0) {
                    result = new String(responseBuffer, 0, read);
                }
            } catch (IOException e) {
                result = "Error: " + e.getMessage();
            } finally {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Button recordButton = findViewById(R.id.recordButton);
            applyGradientToText(recordButton, "Let's MOS");

            if (result.contains("No valid match")) {
                text1.setText("No valid match found!");
                animateText(text1, 0, new Runnable() {
                    @Override
                    public void run() {
                    }
                });

                return;
            }
            String[] splittedText = result.split(":");
            String[] songName = splittedText[1].split("by");
            String[] singer = songName[1].split("\\(");
            String[] type = singer[1].split("\\)");
            text1.setText("Song Name: " + songName[0].replaceAll("'", ""));
            text2.setText("Artist: " + singer[0]);
            text3.setText("Song Type: " + type[0]);



            animateText(text1, 0, new Runnable() {
                @Override
                public void run() {
                    animateText(text2, 500, new Runnable() {
                        @Override
                        public void run() {
                            animateText(text3, 500, null);
                        }
                    });
                }
            });
        }
    }
}
