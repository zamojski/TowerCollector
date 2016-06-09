/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.broadcast;

import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.List;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.events.MeasurementsCollectedEvent;
import info.zamojski.soft.towercollector.files.formatters.json.IJsonFormatter;
import info.zamojski.soft.towercollector.files.formatters.json.JsonBroadcastFormatter;
import info.zamojski.soft.towercollector.model.Measurement;
import trikita.log.Log;

public class ExternalIntentBroadcaster implements Runnable {

    private static final String TAG = ExternalIntentBroadcaster.class.getSimpleName();

    private final String measurementsCollectedAction = "info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED";
    private final String measurementsExtraKey = "measurements";

    private IJsonFormatter formatter = new JsonBroadcastFormatter();

    private void sendMeasurementsCollectedBroadcast(List<Measurement> measurements) {
        Log.i("sendMeasurementsCollectedBroadcast(): Sending broadcast to external apps");

        try {
            String extra = formatter.formatList(measurements);
            // Send broadcast
            Intent intent = new Intent();
            intent.setAction(measurementsCollectedAction);
            intent.putExtra(measurementsExtraKey, extra);
            MyApplication.getApplication().sendBroadcast(intent);
            Log.d("sendMeasurementsCollectedBroadcast(): Broadcasted " + extra);
        } catch (JSONException ex) {
            Log.e("serialize(): Failed to serialize list of measurements to JSON", ex);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(MeasurementsCollectedEvent event) {
        sendMeasurementsCollectedBroadcast(event.getMeasurements());
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        EventBus.getDefault().register(this);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }
}
