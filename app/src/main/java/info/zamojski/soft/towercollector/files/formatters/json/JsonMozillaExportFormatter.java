/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import info.zamojski.soft.towercollector.model.Measurement;

public class JsonMozillaExportFormatter extends JsonMozillaFormatterBase implements IJsonFormatter {

    @Override
    public String formatHeader() {
        return "{\n\"items\":[\n";
    }

    @Override
    public String formatList(List<Measurement> ms) throws JSONException {
        StringBuilder sb = new StringBuilder();
        List<JSONObject> rawItems = formatItems(ms);
        boolean notFirst = false;
        for (JSONObject rawItem : rawItems) {
            if (notFirst) {
                sb.append(",\n");
            }
            notFirst = true;
            sb.append(rawItem.toString());
        }
        return sb.toString();
    }

    @Override
    public String formatNewSegment() {
        return ",\n";
    }

    @Override
    public String formatFooter() {
        return "\n]\n}";
    }
}
