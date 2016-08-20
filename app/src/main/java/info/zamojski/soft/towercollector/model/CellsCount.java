/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

public class CellsCount implements Serializable {

    private static final long serialVersionUID = -8747857482375767011L;

    private int main;
    private int neighboring;

    public CellsCount(int main, int neighboring) {
        this.main = main;
        this.neighboring = neighboring;
    }

    public CellsCount() {
    }

    public int getMain() {
        return main;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getNeighboring() {
        return neighboring;
    }

    public void setNeighboring(int neighboring) {
        this.neighboring = neighboring;
    }

    @Override
    public String toString() {
        return "CellsCount [main=" + main + ", neighboring=" + neighboring + ']';
    }
}
