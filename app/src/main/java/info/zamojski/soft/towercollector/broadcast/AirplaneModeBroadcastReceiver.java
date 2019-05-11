/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.events.AirplaneModeChangedEvent;
import timber.log.Timber;

public class AirplaneModeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
            boolean airplaneModeEnabled = intent.getBooleanExtra("state", false);
            Timber.d("onReceive(): Airplane mode enabled = %s", airplaneModeEnabled);
            EventBus.getDefault().postSticky(new AirplaneModeChangedEvent(airplaneModeEnabled));
        }
    }
}
