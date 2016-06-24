package com.github.wrdlbrnft.simplejson.parsers.base;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;
import com.github.wrdlbrnft.simplejson.parsers.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class IntegerParser implements ElementParser<Integer> {

    @Override
    public Integer fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            return object.getInt(key);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Integer value with key \"" + key + "\" from json: " + object, e);
        }
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Integer value) throws SimpleJsonException {
        try {
            object.put(key, value);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add Integer value " + value + " with key \"" + key + "\" to json: " + object, e);
        }
    }

    @Override
    public Integer fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            return array.getInt(index);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Integer value with index " + index + " from json array: " + array, e);
        }
    }

    @Override
    public void toJsonArray(JSONArray array, Integer value) throws SimpleJsonException {
        array.put(value);
    }
}
