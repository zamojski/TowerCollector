/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import info.zamojski.soft.towercollector.collector.converters.CellIdentityConverter;
import info.zamojski.soft.towercollector.collector.converters.CellLocationConverter;
import info.zamojski.soft.towercollector.collector.converters.CellLocationSignalConverter;
import info.zamojski.soft.towercollector.collector.converters.CellSignalConverter;
import info.zamojski.soft.towercollector.collector.validators.CellIdentityValidator;
import info.zamojski.soft.towercollector.collector.validators.CellLocationValidator;
import info.zamojski.soft.towercollector.collector.validators.ConditionsValidator;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.WcdmaCellIdentityValidator;

public class MeasurementParserFactory {

    public MeasurementParser CreateApi17Parser(float requiredAccuracy, boolean collectNeighboringCells) {
        LocationValidator locationValidator = new LocationValidator(requiredAccuracy);
        CellIdentityValidator cellValidator = new CellIdentityValidator();
        ConditionsValidator conditionsValidator = new ConditionsValidator();
        SystemTimeValidator systemTimeValidator = new SystemTimeValidator();
        CellIdentityConverter cellIdentityConverter = new CellIdentityConverter(new WcdmaCellIdentityValidator());
        CellSignalConverter cellSignalConverter = new CellSignalConverter();
        return new Api17PlusMeasurementParser(locationValidator, cellValidator, conditionsValidator,
                systemTimeValidator, cellIdentityConverter, cellSignalConverter, collectNeighboringCells);
    }

    public MeasurementParser CreateApi1Parser(float requiredAccuracy, boolean collectNeighboringCells) {
        LocationValidator locationValidator = new LocationValidator(requiredAccuracy);
        CellLocationValidator cellLocationValidator = new CellLocationValidator();
        ConditionsValidator conditionsValidator = new ConditionsValidator();
        SystemTimeValidator systemTimeValidator = new SystemTimeValidator();
        CellLocationConverter cellLocationConverter = new CellLocationConverter();
        CellLocationSignalConverter cellSignalConverter = new CellLocationSignalConverter();
        return new LegacyMeasurementParser(locationValidator, cellLocationValidator, conditionsValidator,
                systemTimeValidator, cellLocationConverter, cellSignalConverter, collectNeighboringCells);
    }
}
