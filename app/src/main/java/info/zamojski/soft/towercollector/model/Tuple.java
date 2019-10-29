/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

public class Tuple<T1, T2> implements Serializable {

    private static final long serialVersionUID = -603790109265873600L;
    private final T1 item1;
    private final T2 item2;

    public Tuple(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public final T1 getItem1() {
        return item1;
    }

    public final T2 getItem2() {
        return item2;
    }
}
