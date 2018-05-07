/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.logging;

import timber.log.Timber;

public class ConsoleLoggingTree extends Timber.DebugTree {

    private static final int DISABLED = 0;

    private int priority = DISABLED;

    public final static ConsoleLoggingTree INSTANCE = new ConsoleLoggingTree();

    private ConsoleLoggingTree() {
        // Avoid instantiation
    }

    public ConsoleLoggingTree setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        return (this.priority > 0 && this.priority <= priority);
    }
}
