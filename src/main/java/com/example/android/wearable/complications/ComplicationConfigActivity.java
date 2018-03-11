/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.complications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.concurrent.Executors;

public class ComplicationConfigActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "WatchFaceConfigActivity";

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;

    public enum ComplicationLocation {
        LEFT,
        RIGHT
    }

    private int mLeftComplicationId;
    private int mRightComplicationId;
    private int mSelectedComplicationId;
    private ComponentName mWatchFaceComponentName;

    private ProviderInfoRetriever mProviderInfoRetriever;

    private ImageView mLeftComplicationBackground;
    private ImageView mRightComplicationBackground;

    private ImageButton mLeftComplication;
    private ImageButton mRightComplication;

    private Drawable mDefaultAddComplicationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);

        mDefaultAddComplicationDrawable = getDrawable(R.drawable.add_complication);

        mSelectedComplicationId = -1;

        mLeftComplicationId =
                CustomWatchFaceService.getComplicationId(ComplicationLocation.LEFT);
        mRightComplicationId =
                CustomWatchFaceService.getComplicationId(ComplicationLocation.RIGHT);

        mWatchFaceComponentName =
                new ComponentName(getApplicationContext(), CustomWatchFaceService.class);

        mLeftComplicationBackground = (ImageView) findViewById(R.id.left_complication_background);
        mLeftComplication = (ImageButton) findViewById(R.id.left_complication);
        mLeftComplication.setOnClickListener(this);

        mLeftComplication.setImageDrawable(mDefaultAddComplicationDrawable);
        mLeftComplicationBackground.setVisibility(View.INVISIBLE);

        mRightComplicationBackground = (ImageView) findViewById(R.id.right_complication_background);
        mRightComplication = (ImageButton) findViewById(R.id.right_complication);
        mRightComplication.setOnClickListener(this);

        mRightComplication.setImageDrawable(mDefaultAddComplicationDrawable);
        mRightComplicationBackground.setVisibility(View.INVISIBLE);

        mProviderInfoRetriever =
                new ProviderInfoRetriever(getApplicationContext(), Executors.newCachedThreadPool());
        mProviderInfoRetriever.init();

        retrieveInitialComplicationsData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mProviderInfoRetriever.release();
    }

    public void retrieveInitialComplicationsData() {

        final int[] complicationIds = CustomWatchFaceService.getComplicationIds();

        mProviderInfoRetriever.retrieveProviderInfo(
                new ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                    @Override
                    public void onProviderInfoReceived(
                            int watchFaceComplicationId,
                            @Nullable ComplicationProviderInfo complicationProviderInfo) {

                        Log.d(TAG, "onProviderInfoReceived: " + complicationProviderInfo);

                        updateComplicationViews(watchFaceComplicationId, complicationProviderInfo);
                    }
                },
                mWatchFaceComponentName,
                complicationIds);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(mLeftComplication)) {
            Log.d(TAG, "Left Complication click()");
            launchComplicationHelperActivity(ComplicationLocation.LEFT);

        } else if (view.equals(mRightComplication)) {
            Log.d(TAG, "Right Complication click()");
            launchComplicationHelperActivity(ComplicationLocation.RIGHT);
        }
    }

    private void launchComplicationHelperActivity(ComplicationLocation complicationLocation) {

        mSelectedComplicationId =
                CustomWatchFaceService.getComplicationId(complicationLocation);

        if (mSelectedComplicationId >= 0) {

            int[] supportedTypes =
                    CustomWatchFaceService.getSupportedComplicationTypes(
                            complicationLocation);

            startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                            getApplicationContext(),
                            mWatchFaceComponentName,
                            mSelectedComplicationId,
                            supportedTypes),
                    ComplicationConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);

        } else {
            Log.d(TAG, "Complication not supported by watch face.");
        }
    }

    public void updateComplicationViews(
            int watchFaceComplicationId, ComplicationProviderInfo complicationProviderInfo) {
        Log.d(TAG, "updateComplicationViews(): id: " + watchFaceComplicationId);
        Log.d(TAG, "\tinfo: " + complicationProviderInfo);

        if (watchFaceComplicationId == mLeftComplicationId) {
            if (complicationProviderInfo != null) {
                mLeftComplication.setImageIcon(complicationProviderInfo.providerIcon);
                mLeftComplicationBackground.setVisibility(View.VISIBLE);

            } else {
                mLeftComplication.setImageDrawable(mDefaultAddComplicationDrawable);
                mLeftComplicationBackground.setVisibility(View.INVISIBLE);
            }

        } else if (watchFaceComplicationId == mRightComplicationId) {
            if (complicationProviderInfo != null) {
                mRightComplication.setImageIcon(complicationProviderInfo.providerIcon);
                mRightComplicationBackground.setVisibility(View.VISIBLE);

            } else {
                mRightComplication.setImageDrawable(mDefaultAddComplicationDrawable);
                mRightComplicationBackground.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {

            ComplicationProviderInfo complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);
            Log.d(TAG, "Provider: " + complicationProviderInfo);

            if (mSelectedComplicationId >= 0) {
                updateComplicationViews(mSelectedComplicationId, complicationProviderInfo);
            }
        }
    }
}
