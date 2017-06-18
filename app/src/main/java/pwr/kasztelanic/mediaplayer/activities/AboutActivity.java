package pwr.kasztelanic.mediaplayer.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import pwr.kasztelanic.mediaplayer.BuildConfig;
import pwr.kasztelanic.mediaplayer.R;

public class AboutActivity extends Activity
{
    TextView versionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        versionTV = (TextView) findViewById(R.id.about_version);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        versionTV.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
    }
}
