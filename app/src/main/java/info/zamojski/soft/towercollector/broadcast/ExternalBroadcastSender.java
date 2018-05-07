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
import timber.log.Timber;

public class ExternalBroadcastSender implements Runnable {


    private final String measurementsCollectedAction = "info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED";
    private final String measurementsExtraKey = "measurements";

    private IJsonFormatter formatter;

    private void sendMeasurementsCollectedBroadcast(List<Measurement> measurements) {
        Timber.i("sendMeasurementsCollectedBroadcast(): Sending broadcast to external apps");
        if (formatter == null) {
            formatter = new JsonBroadcastFormatter();
        }
        try {
            String extra = formatter.formatList(measurements);
            // Send broadcast
            Intent intent = new Intent();
            intent.setAction(measurementsCollectedAction);
            intent.putExtra(measurementsExtraKey, extra);
            MyApplication.getApplication().sendBroadcast(intent);
            Timber.d("sendMeasurementsCollectedBroadcast(): Broadcasted " + extra);
        } catch (JSONException ex) {
            Timber.e(ex, "sendMeasurementsCollectedBroadcast(): Failed to serialize list of measurements to JSON");
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
