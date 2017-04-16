/*
 * Copyright (C) 2014 Caleb Sabatini
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package audio.omgsoundboard.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;

public class MainActivity extends AppCompatActivity {
    private SoundPlayer soundPlayer;
    private AudioManager audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final ImageButton infoButton = (ImageButton) findViewById(R.id.main_screen_top_toolbar_info);
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.about_dialog_title)
                .backgroundColorRes(R.color.colorPrimary)
                .content(Html.fromHtml(getString(R.string.about_dialog_content)))
                .positiveText(R.string.moreinfo_button)
                .negativeText(R.string.about_STFU)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Uri uri = Uri.parse("https://omgsoundboard.github.io/"); // missing 'http://' will cause crash
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
                });
        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Do whatever...
                builder.show();
            }
        });
        infoButton.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Kein Easter Egg!",
                        Toast.LENGTH_LONG).show();
                soundPlayer.stop();


                return true;

            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                //Catch your exception
                // Without System.exit() this will not work.
                System.exit(2);
            }
        });

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        GoogleAnalytics.getInstance(this).setLocalDispatchPeriod(120);
        ((AnalyticsTracker) getApplication()).getTracker(AnalyticsTracker.TrackerName.APP_TRACKER);


        FavStore.init(getPreferences(Context.MODE_PRIVATE));


        final RecyclerView grid = (RecyclerView) findViewById(R.id.grid_view);
        grid.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.num_cols),
                StaggeredGridLayoutManager.VERTICAL));
        grid.setAdapter(new SoundAdapter(SoundStore.getAllSounds(this)));

        SwitchCompat favSwitch = (SwitchCompat) findViewById(R.id.fav_switch);
        favSwitch.setChecked(FavStore.getInstance().getShowFavorites());
        if (favSwitch.isChecked()) {
            ((SoundAdapter) grid.getAdapter()).onlyShowFavorites();
        } else {
            ((SoundAdapter) grid.getAdapter()).showAllSounds(MainActivity.this);
        }
        favSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((SoundAdapter) grid.getAdapter()).onlyShowFavorites();
                } else {
                    ((SoundAdapter) grid.getAdapter()).showAllSounds(MainActivity.this);
                }
                FavStore.getInstance().setShowFavorites(isChecked);
            }
        });
    }

    @Override
    public void onStart() {
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        super.onStart();

    }
    @Override
    public void onResume() {
        super.onResume();
        soundPlayer = new SoundPlayer(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        soundPlayer.release();
    }

    public static class ApplicationContextProvider extends Application {

        /**
         * Keeps a reference of the application context
         */
        private static Context sContext;

        @Override
        public void onCreate() {
            super.onCreate();

            sContext = getApplicationContext();

        }

        /**
         * Returns the application context
         *
         * @return application context
         */
        public static Context getContext() {
            return sContext;
        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            default:
                return false;
        }
    }
}
