/*
 * Taken From MaterialFBook - ZeeRooo Thanks
 */

package com.proximadev.flyso.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.proximadev.flyso.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{

    private VideoView mVideoView;
    private int position = 0;
    private SeekBar mSeekbar;
    private String mVideoUrl, mVideoName;
    private TextView mElapsedTime, mRemainingTime;
    private SpinKitView mSpinKitView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(prefs.getBoolean("RotationLock",true))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video);

        mInterstitialAd = new InterstitialAd(VideoActivity.this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit2));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mSpinKitView = findViewById(R.id.spin_kit_video);

        mVideoUrl = getIntent().getStringExtra("VideoUrl");
        mVideoName = getIntent().getStringExtra("VideoName");

        mVideoView = findViewById(R.id.video_view);
        mSeekbar = findViewById(R.id.progress);
        mElapsedTime = findViewById(R.id.elapsed_time);
        mRemainingTime = findViewById(R.id.remaining_time);

        mSeekbar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSeekbar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }

        mVideoView.setVideoURI(Uri.parse(mVideoUrl));

        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mVideoView.seekTo(position);
            mSeekbar.setMax(mVideoView.getDuration());
            mSeekbar.postDelayed(Update, 1000);
            mElapsedTime.postDelayed(Update, 1000);
            mRemainingTime.postDelayed(Update, 1000);
            if (position == 0)
                mVideoView.start();
        });

        mVideoView.setOnCompletionListener(mp -> super.onBackPressed());

        findViewById(R.id.pauseplay_btn).setOnClickListener(this);
        findViewById(R.id.previous_btn).setOnClickListener(this);
        findViewById(R.id.download_btn).setOnClickListener(this);
        findViewById(R.id.share_btn).setOnClickListener(this);
        findViewById(R.id.copy_btn).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mVideoView.seekTo(progress);
            }
        });
    }

    private void getVideo(){
        try{
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mVideoUrl));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("FlySo",mVideoName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
            }
            Toasty.success(this, getString(R.string.downloading), 1).show();
        }
        catch (Exception ex){
            ex.printStackTrace();
            Toasty.error(this, getString(R.string.error), 0).show();
        }
    }

    private boolean isPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                RequestStoragePermission();
                return false;
            }
        } else {
            // Permission is automatically granted on sdk < 23 upon installation
            return true;
        }
    }

    private void RequestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private final Runnable Update = new Runnable() {
        @Override
        public void run() {
            if(mSeekbar != null) {
                mSeekbar.setProgress(mVideoView.getCurrentPosition());
            }
            if(mVideoView.isPlaying()) {
                if (mSpinKitView.getVisibility()== View.VISIBLE){
                    mSpinKitView.setVisibility(View.GONE);
                }
                mSeekbar.postDelayed(Update, 1000);
                mElapsedTime.setText(Time(mVideoView.getCurrentPosition()));
                mRemainingTime.setText(Time(mVideoView.getDuration() - mVideoView.getCurrentPosition()));
            }
        }};

    private String Time(long ms) {
        return String.format(Locale.getDefault(), "%d:%d", TimeUnit.MILLISECONDS.toMinutes(ms), TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((ms))));
    }

    @Override
    public void onPause() {
        super.onPause();
        position = mVideoView.getCurrentPosition();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.seekTo(position);
        mVideoView.start();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                super.onBackPressed();
                break;
            case R.id.pauseplay_btn:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    ((ImageButton) view).setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mVideoView.start();
                    ((ImageButton) view).setImageResource(android.R.drawable.ic_media_pause);
                }
                break;
            case R.id.previous_btn:
                mVideoView.seekTo(0);
                mSeekbar.setProgress(0);
                break;
            case R.id.download_btn:
                if (isPermissionsGranted()) {
                    if (mInterstitialAd != null && mInterstitialAd.isLoaded())
                        mInterstitialAd.show();
                    getVideo();
                }
                break;
            case R.id.share_btn:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"FlySo-Share Link:");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mVideoUrl);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                break;
            case R.id.copy_btn:
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newUri(getContentResolver(), "FlySo-Shared Link:", Uri.parse(mVideoUrl));
                    if (clipboard != null && clip != null) {
                        clipboard.setPrimaryClip(clip);
                        Toasty.success(this, getString(R.string.copied_to_clipboard), 0).show();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                    Toasty.error(VideoActivity.this, getString(R.string.cant_copy),0).show();
                }
                break;
        }
    }
}
