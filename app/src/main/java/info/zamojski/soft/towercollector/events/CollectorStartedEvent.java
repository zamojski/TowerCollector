/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import android.content.Intent;

public class CollectorStartedEvent {

    private final Intent intent;

    public CollectorStartedEvent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return this.intent;
    }
}
