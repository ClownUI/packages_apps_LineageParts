/*
 * SPDX-FileCopyrightText: 2015 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.lineagestats;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

public class ReportingService extends IntentService {
    static final String TAG = "clownStats";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public ReportingService() {
        super(ReportingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JobScheduler js = getSystemService(JobScheduler.class);

        Context context = getApplicationContext();

        String deviceId = Utilities.getUniqueID(context);
        String deviceName = Utilities.getDevice();
        String deviceCrVersion = Utilities.getModVersion();
        String deviceBuildDate = Utilities.getBuildDate();
        String deviceAndroidVersion = Utilities.getAndroidVersion();
        String deviceTag = Utilities.getTag();
        String deviceCountry = Utilities.getCountryCode(context);
        String deviceCarrier = Utilities.getCarrier(context);
        String deviceCarrierId = Utilities.getCarrierId(context);

        final int clownOldJobId = AnonymousStats.getLastJobId(context);
        final int clownOrgJobId = AnonymousStats.getNextJobId(context);

        if (DEBUG) Log.d(TAG, "scheduling job id: " + clownOrgJobId);

        PersistableBundle clownBundle = new PersistableBundle();
        clownBundle.putString(StatsUploadJobService.KEY_DEVICE_NAME, deviceName);
        clownBundle.putString(StatsUploadJobService.KEY_UNIQUE_ID, deviceId);
        clownBundle.putString(StatsUploadJobService.KEY_CR_VERSION, deviceCrVersion);
        clownBundle.putString(StatsUploadJobService.KEY_BUILD_DATE, deviceBuildDate);
        clownBundle.putString(StatsUploadJobService.KEY_ANDROID_VERSION, deviceAndroidVersion);
        clownBundle.putString(StatsUploadJobService.KEY_TAG, deviceTag);
        clownBundle.putString(StatsUploadJobService.KEY_COUNTRY, deviceCountry);
        clownBundle.putString(StatsUploadJobService.KEY_CARRIER, deviceCarrier);
        clownBundle.putString(StatsUploadJobService.KEY_CARRIER_ID, deviceCarrierId);
        clownBundle.putLong(StatsUploadJobService.KEY_TIMESTAMP, System.currentTimeMillis());

        // set job types
        clownBundle.putInt(StatsUploadJobService.KEY_JOB_TYPE,
                StatsUploadJobService.JOB_TYPE_CLOWN);

        // schedule clown stats upload
        js.schedule(new JobInfo.Builder(clownOrgJobId, new ComponentName(getPackageName(),
                StatsUploadJobService.class.getName()))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1000)
                .setExtras(clownBundle)
                .setPersisted(true)
                .build());

        // cancel old job in case it didn't run yet
        js.cancel(clownOldJobId);

        // reschedule
        AnonymousStats.updateLastSynced(this);
        ReportingServiceManager.setAlarm(this);
    }
}
