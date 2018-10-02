/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import timber.log.Timber;

public class BatteryStatusBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            String prefAction = MyApplication.getPreferencesProvider().getCollectorLowBatteryAction();
            Timber.d("onReceive(): Low battery received, taking %s action", prefAction);
            if (context.getString(R.string.preferences_collector_low_battery_action_entries_value_stop).equals(prefAction)) {
                Intent stopIntent = new Intent(context, CollectorService.class);
                context.stopService(stopIntent);
            }
        }
    }
}
