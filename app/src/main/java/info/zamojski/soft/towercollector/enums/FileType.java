/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.enums;

import java.util.ArrayList;
import java.util.List;

public enum FileType {
    Unknown, Csv, CsvOcid, Gpx, JsonMls, Kml, Kmz, Compress;

    public static FileType[] valuesOf(String[] values) {
        List<FileType> fileTypes = new ArrayList<>();
        for (String value : values) {
            try {
                fileTypes.add(valueOf(value));
            } catch (IllegalArgumentException ex) {
                // ignore as this means incompatibility
            }
        }
        return fileTypes.toArray(new FileType[0]);
    }

    public static List<String> toNames(List<FileType> values) {
        List<String> fileTypes = new ArrayList<>();
        for (FileType value : values) {
            fileTypes.add(value.name());
        }
        return fileTypes;
    }
}
