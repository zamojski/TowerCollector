/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.events.PowerSaveModeChangedEvent;
import info.zamojski.soft.towercollector.utils.BatteryUtils;
import timber.log.Timber;

public class BatterySaverBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
            boolean powerSaveModeEnabled = BatteryUtils.isPowerSaveModeEnabled(context);
            Timber.d("onReceive(): Power save mode enabled = %s", powerSaveModeEnabled);
            EventBus.getDefault().postSticky(new PowerSaveModeChangedEvent(powerSaveModeEnabled));
        }
    }
}
