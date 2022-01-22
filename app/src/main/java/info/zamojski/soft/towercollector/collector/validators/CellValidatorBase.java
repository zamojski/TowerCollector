package info.zamojski.soft.towercollector.collector.validators;

import info.zamojski.soft.towercollector.collector.validators.specific.CdmaCellValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.GsmCellValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.LteCellValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.NrCellValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.TdscdmaCellValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.WcdmaCellValidator;

public abstract class CellValidatorBase {

    private GsmCellValidator gsmValidator;
    private WcdmaCellValidator wcdmaValidator;
    private LteCellValidator lteValidator;
    private CdmaCellValidator cdmaValidator;
    private NrCellValidator nrValidator;
    private TdscdmaCellValidator tdscdmaValidator;

    protected GsmCellValidator getGsmValidator() {
        if (gsmValidator == null) {
            gsmValidator = new GsmCellValidator();
        }
        return gsmValidator;
    }

    protected WcdmaCellValidator getWcdmaValidator() {
        if (wcdmaValidator == null) {
            wcdmaValidator = new WcdmaCellValidator();
        }
        return wcdmaValidator;
    }

    protected LteCellValidator getLteValidator() {
        if (lteValidator == null) {
            lteValidator = new LteCellValidator();
        }
        return lteValidator;
    }

    protected CdmaCellValidator getCdmaValidator() {
        if (cdmaValidator == null) {
            cdmaValidator = new CdmaCellValidator();
        }
        return cdmaValidator;
    }

    protected NrCellValidator getNrValidator() {
        if (nrValidator == null) {
            nrValidator = new NrCellValidator();
        }
        return nrValidator;
    }

    protected TdscdmaCellValidator getTdscdmaValidator() {
        if (tdscdmaValidator == null) {
            tdscdmaValidator = new TdscdmaCellValidator();
        }
        return tdscdmaValidator;
    }
}
