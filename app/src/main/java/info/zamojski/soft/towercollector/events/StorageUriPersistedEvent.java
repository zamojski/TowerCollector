/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import info.zamojski.soft.towercollector.utils.StorageUtils.PendingAction;

public class StorageUriPersistedEvent {
    private final PendingAction pendingAction;

    public StorageUriPersistedEvent(PendingAction pendingAction) {
        this.pendingAction = pendingAction;
    }

    public PendingAction getPendingAction() {
        return pendingAction;
    }
}
