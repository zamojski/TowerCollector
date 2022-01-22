/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import cz.mroczis.netmonster.core.model.cell.CellCdma;
import cz.mroczis.netmonster.core.model.cell.CellGsm;
import cz.mroczis.netmonster.core.model.cell.CellLte;
import cz.mroczis.netmonster.core.model.cell.CellNr;
import cz.mroczis.netmonster.core.model.cell.CellTdscdma;
import cz.mroczis.netmonster.core.model.cell.CellWcdma;
import cz.mroczis.netmonster.core.model.cell.ICell;

public class NetMonsterCellValidator extends CellValidatorBase {

    public boolean isValid(ICell cell) {
        if (cell instanceof CellGsm) {
            CellGsm gsmCell = (CellGsm) cell;
            return getGsmValidator().isValid(gsmCell);
        }
        if (cell instanceof CellWcdma) {
            CellWcdma wcdmaCell = (CellWcdma) cell;
            return getWcdmaValidator().isValid(wcdmaCell);
        }
        if (cell instanceof CellLte) {
            CellLte lteCell = (CellLte) cell;
            return getLteValidator().isValid(lteCell);
        }
        if (cell instanceof CellCdma) {
            CellCdma cdmaCell = (CellCdma) cell;
            return getCdmaValidator().isValid(cdmaCell);
        }
        if (cell instanceof CellNr) {
            CellNr nrCell = (CellNr) cell;
            return getNrValidator().isValid(nrCell);
        }
        if (cell instanceof CellTdscdma) {
            CellTdscdma tdscdmaCell = (CellTdscdma) cell;
            return getTdscdmaValidator().isValid(tdscdmaCell);
        }
        throw new UnsupportedOperationException("Cell identity type not supported `" + cell.getClass().getName() + "`");
    }
}
