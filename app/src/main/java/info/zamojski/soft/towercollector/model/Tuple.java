/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

public class Tuple<T> implements Serializable {

    private static final long serialVersionUID = -603790109265873600L;
    private final T[] array;

    public Tuple(T... array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        this.array = array;
    }

    public T get(int index) {
        if (index < 0 || index >= array.length)
            throw new ArrayIndexOutOfBoundsException(index);
        return array[index];
    }

}
