/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import cz.mroczis.netmonster.core.model.cell.CellCdma;
import cz.mroczis.netmonster.core.model.cell.CellGsm;
import cz.mroczis.netmonster.core.model.cell.CellLte;
import cz.mroczis.netmonster.core.model.cell.CellNr;
import cz.mroczis.netmonster.core.model.cell.CellTdscdma;
import cz.mroczis.netmonster.core.model.cell.CellWcdma;
import cz.mroczis.netmonster.core.model.cell.ICell;
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class NetMonsterCellConverter {

    public Cell convert(ICell netMonsterCell) {
        Cell cell = new Cell();
        cell.setNeighboring(!(netMonsterCell.getConnectionStatus() instanceof PrimaryConnection));
        if (netMonsterCell instanceof CellGsm) {
            CellGsm gsmCell = (CellGsm) netMonsterCell;
            cell.setNetMonsterGsmCell(gsmCell.getNetwork().getMcc(), gsmCell.getNetwork().getMnc(), fixIntNull(gsmCell.getLac()), fixLongNull(gsmCell.getCid()));
        } else if (netMonsterCell instanceof CellWcdma) {
            CellWcdma wcdmaCell = (CellWcdma) netMonsterCell;
            cell.setNetMonsterWcdmaCell(wcdmaCell.getNetwork().getMcc(), wcdmaCell.getNetwork().getMnc(), fixIntNull(wcdmaCell.getLac()),
                    fixLongNull(wcdmaCell.getCi()), fixIntNull(wcdmaCell.getCid()), fixIntNull(wcdmaCell.getRnc()), fixIntNull(wcdmaCell.getPsc()));
        } else if (netMonsterCell instanceof CellLte) {
            CellLte lteCell = (CellLte) netMonsterCell;
            cell.setNetMonsterLteCell(lteCell.getNetwork().getMcc(), lteCell.getNetwork().getMnc(), fixIntNull(lteCell.getTac()), fixLongNull(lteCell.getEci()), fixIntNull(lteCell.getPci()));
        } else if (netMonsterCell instanceof CellNr) {
            CellNr nrCell = (CellNr) netMonsterCell;
            cell.setNetMonsterNrCell(nrCell.getNetwork().getMcc(), nrCell.getNetwork().getMnc(), fixIntNull(nrCell.getTac()), fixLongNull(nrCell.getNci()), fixIntNull(nrCell.getPci()));
        } else if (netMonsterCell instanceof CellTdscdma) {
            CellTdscdma tdscdmaCell = (CellTdscdma) netMonsterCell;
            cell.setNetMonsterTdscdmaCell(tdscdmaCell.getNetwork().getMcc(), tdscdmaCell.getNetwork().getMnc(), fixIntNull(tdscdmaCell.getLac()),
                    fixLongNull(tdscdmaCell.getCi()), fixIntNull(tdscdmaCell.getCpid()));
        } else if (netMonsterCell instanceof CellCdma) {
            CellCdma cdmaCell = (CellCdma) netMonsterCell;
            cell.setNetMonsterCdmaCell(cdmaCell.getSid(), fixIntNull(cdmaCell.getNid()), fixLongNull(cdmaCell.getBid()));
        } else {
            throw new UnsupportedOperationException("Cell type not supported `" + netMonsterCell.getClass().getName() + "`");
        }
        return cell;
    }

    public String createCellKey(Cell cell) {
        StringBuilder sb = new StringBuilder();
        sb.append(cell.getMcc())
                .append("_").append(cell.getMnc())
                .append("_").append(cell.getLac())
                .append("_").append(cell.getCid());
        return sb.toString();
    }

    public String createCellKey(ICell cell) {
        StringBuilder sb = new StringBuilder();
        if (cell instanceof CellGsm) {
            CellGsm gsmCell = (CellGsm) cell;
            sb.append(gsmCell.getNetwork() != null ? StringUtils.toInteger(gsmCell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(gsmCell.getNetwork() != null ? StringUtils.toInteger(gsmCell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(gsmCell.getLac())
                    .append("_").append(gsmCell.getCid());
        } else if (cell instanceof CellWcdma) {
            CellWcdma wcdmaCell = (CellWcdma) cell;
            sb.append(wcdmaCell.getNetwork() != null ? StringUtils.toInteger(wcdmaCell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(wcdmaCell.getNetwork() != null ? StringUtils.toInteger(wcdmaCell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(wcdmaCell.getLac())
                    .append("_").append(wcdmaCell.getCi());
        } else if (cell instanceof CellLte) {
            CellLte lteCell = (CellLte) cell;
            sb.append(lteCell.getNetwork() != null ? StringUtils.toInteger(lteCell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(lteCell.getNetwork() != null ? StringUtils.toInteger(lteCell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(lteCell.getTac())
                    .append("_").append(lteCell.getEci());
        } else if (cell instanceof CellNr) {
            CellNr nrCell = (CellNr) cell;
            sb.append(nrCell.getNetwork() != null ? StringUtils.toInteger(nrCell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(nrCell.getNetwork() != null ? StringUtils.toInteger(nrCell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(nrCell.getTac())
                    .append("_").append(nrCell.getNci());
        } else if (cell instanceof CellTdscdma) {
            CellTdscdma tdscdmaCell = (CellTdscdma) cell;
            sb.append(tdscdmaCell.getNetwork() != null ? StringUtils.toInteger(tdscdmaCell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(tdscdmaCell.getNetwork() != null ? StringUtils.toInteger(tdscdmaCell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID)
                    .append("_").append(tdscdmaCell.getLac())
                    .append("_").append(tdscdmaCell.getCi());
        } else if (cell instanceof CellCdma) {
            CellCdma cdmaCell = (CellCdma) cell;
            sb.append(Cell.UNKNOWN_CID)
                    .append("_").append(cdmaCell.getSid())
                    .append("_").append(cdmaCell.getNid())
                    .append("_").append(cdmaCell.getBid());
        } else {
            Exception ex = new UnsupportedOperationException("Cell identity type not supported `" + cell.getClass().getName() + "` = `" + cell.toString() + "`");
            Timber.e(ex);
            MyApplication.handleSilentException(ex);
        }
        return sb.toString();
    }

    private long fixLongNull(Integer value) {
        return value == null ? Cell.UNKNOWN_CID_LONG : value.longValue();
    }

    private long fixLongNull(Long value) {
        return value == null ? Cell.UNKNOWN_CID_LONG : value.longValue();
    }

    private int fixIntNull(Integer value) {
        return value == null ? Cell.UNKNOWN_CID : value.intValue();
    }
}
