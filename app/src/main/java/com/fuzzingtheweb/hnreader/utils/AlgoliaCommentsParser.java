package com.fuzzingtheweb.hnreader.utils;


import com.fuzzingtheweb.hnreader.models.Comment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlgoliaCommentsParser {

    private static final String LOG_TAG = AlgoliaCommentsParser.class.getSimpleName();

    public List<Comment> getCommentsList(JSONObject jsonObject) throws JSONException {
        List<Comment> commentList = new ArrayList<>();

        return commentList;
    }

//    // TODO: do this
//    public List<StatusEntry> getBusStatusEntryList(JSONObject jsonObject) throws JSONException {
//        List<StatusEntry> busStatusEntryList = new ArrayList<StatusEntry>();
//
//        return busStatusEntryList;
//    }
//
//    public List<BusStop> getBusStopList(JSONObject jsonObject) throws JSONException {
//        List<BusStop> bustStopList = new ArrayList<BusStop>();
//        JSONArray stops = jsonObject.getJSONArray("stops");
//        for (int i = 0; i < stops.length(); i++) {
//            JSONObject stop = (JSONObject) stops.get(i);
//            bustStopList.add(new BusStop(
//                    stop.getDouble("latitude"),
//                    stop.getDouble("longitude"),
//                    stop.getDouble("distance"),
//                    stop.getString("name"),
//                    stop.getString("locality"),
//                    stop.getString("indicator"),
//                    stop.getString("bearing"),
//                    stop.getString("mode"),
//                    stop.getString("smscode"),
//                    stop.getString("atcocode")
//            ));
//        }
//        return bustStopList;
//    }
//
//    public List<Platform> getPlatformList(JSONObject jsonObject) throws JSONException {
//
//        List<Platform> platformList = new ArrayList<Platform>();
//
//        if (jsonObject == null) {
//            return platformList;
//        }
//
//        String stationName = jsonObject.getString("station_name");
//        Iterator lineIterator = jsonObject.getJSONObject("lines").keys();
//        while (lineIterator.hasNext()) {
//
//            JSONObject platforms = jsonObject.getJSONObject("lines")
//                    .getJSONObject((String) lineIterator.next()).getJSONObject("platforms");
//            Iterator platformIterator = platforms.keys();
//
//            while (platformIterator.hasNext()) {
//                String direction = (String) platformIterator.next();
//
//                Platform platform = new Platform(stationName);
//                platform.setDirection(direction);
//
//                JSONArray entryListJSON = platforms.getJSONObject(direction).getJSONArray("departures");
//                List<StatusEntry> statusEntryList = getEntryList(entryListJSON);
//                platform.setStatusEntryList(statusEntryList);
//                platformList.add(platform);
//            }
//        }
//        return platformList;
//    }
//
//    public List<StatusEntry> getEntryList(JSONArray jsonArray) throws JSONException {
//        List<StatusEntry> statusEntryList = new ArrayList<StatusEntry>();
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject entryJSON = (JSONObject) jsonArray.get(i);
//            statusEntryList.add(new StatusEntry(
//                    entryJSON.getString("location"),
//                    entryJSON.getString("destination_name"),
//                    entryJSON.getInt("best_departure_estimate_mins")));
//        }
//        return statusEntryList;
//    }
//
//    public List<LineStation> getStationsList(String line, JSONObject jsonObject) throws JSONException {
//        JSONArray stationListJSON = jsonObject.getJSONArray("stations");
//        List<LineStation> lineStationList = new ArrayList<LineStation>();
//        for (int i = 0; i < stationListJSON.length(); i++) {
//            JSONObject stationJSON = (JSONObject) stationListJSON.get(i);
//            String stationName = stationJSON.getString("name");
//            String stationCode = stationJSON.getString("station_code");
//            lineStationList.add(new LineStation(stationName, stationCode, line));
//        }
//        return lineStationList;
//    }
}
