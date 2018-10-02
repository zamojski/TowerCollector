/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.ICellUtils;
import info.zamojski.soft.towercollector.providers.MozillaCellUtils;

public class JsonMozillaUploadFormatter extends JsonMozillaFormatterBase implements IJsonFormatter {

    private static final ICellUtils cellUtils;

    static {
        cellUtils = new MozillaCellUtils();
    }

    @Override
    public String formatHeader() {
        return "";
    }

    @Override
    public String formatList(List<Measurement> ms) throws JSONException {
        if (ms.size() == 0) {
            return new JSONObject().toString();
        }
        JSONArray items = new JSONArray();
        List<JSONObject> rawItems = formatItems(ms);
        for (JSONObject rawItem : rawItems) {
            items.put(rawItem);
        }
        JSONObject root = new JSONObject();
        root.put("items", items);
        return root.toString();
    }

    @Override
    public String formatNewSegment() {
        return "";
    }

    @Override
    public String formatFooter() {
        return "";
    }
}
