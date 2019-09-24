/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import timber.log.Timber;

public class LocationModeOrProvidersChangedReceiver extends BroadcastReceiver {

    private final CollectorService collectorService;

    public LocationModeOrProvidersChangedReceiver(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (LocationManager.MODE_CHANGED_ACTION.equals(action) || LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
            boolean isGpsProviderEnabled = GpsUtils.isGpsEnabled(context);
            Timber.d("onReceive(): Location or GPS enabled = %s", isGpsProviderEnabled);
            collectorService.setGpsStatus(isGpsProviderEnabled ? GpsStatus.Initializing : GpsStatus.Disabled);
        }
    }
}
