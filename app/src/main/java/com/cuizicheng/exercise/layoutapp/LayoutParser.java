package com.cuizicheng.exercise.layoutapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuizicheng on 2017/3/2.
 */

public class LayoutParser {
    static final String ATTR_RATIO = "ratio";
    static final String ATTR_GRIDS = "grids";

    static String demoJson = "{ratio:'1:1',grids:[{start:[0,0],end:[0.5,0.5]},{start:[0.5,0],end:[1,0.5]},{start:[0,0.5],end:[1,1]}]}";


    static String demoJson2() {

        return DemoJsonBuildHelper.layout("16:9")
                .grid("[0,0]", "[0.3,0.35]")
                .grid("[0.3,0]", "[0.6,0.35]")
                .grid("[0.6,0]", "[1,0.35]")
                .grid("[0,0.35]", "[0.2,0.6]")
                .grid("[0.2,0.35]", "[0.8,0.6]")
                .grid("[0.8,0.35]", "[1,0.6]")
                .grid("[0,0.6]", "[0.35,1]")
                .grid("[0.35,0.6]", "[0.65,1]")
                .grid("[0.65,0.6]", "[1,1]").complete();

    }

    static String demoJson3() {

        return DemoJsonBuildHelper.layout("3:4")
                .grid("[0,0]", "[0.8,0.6]")
                .grid("[0.8,0]", "[1,0.3]")
                .grid("[0.8,0.3]", "[1,0.6]")
                .grid("[0,0.6]", "[0.8,1]")
                .grid("[0.8,0.6]", "[1,1]").complete();

    }

    static String demoJson4() {
        return DemoJsonBuildHelper.layout("3:4")
                .grid("[0,0]", "[1,0.7]")
                .grid("[0,0.7]", "[0.3,1]")
                .grid("[0.3,0.7]", "[0.6,1]")
                .grid("[0.6,0.7]", "[1,1]").complete();
    }


    public static Layout parserDemo() throws JSONException {
        return parser(demoJson3());
    }

    public static Layout parser(String json) throws JSONException {
        Log.d("layout", json);
        JSONObject jsonObject = new JSONObject(json);
        return parserJsonImpl(jsonObject);
    }


    private static Layout parserJsonImpl(JSONObject jObj) throws JSONException {
        String ratio = jObj.getString(ATTR_RATIO);
        String[] s = ratio.split(":");

        JSONArray jsonArray = jObj.getJSONArray(ATTR_GRIDS);
        Grid[] grids = new Grid[jsonArray.length()];

        JSONObject gridObj;
        JSONArray startObj;
        JSONArray endObj;
        Grid grid;
        for (int i = 0; i < jsonArray.length(); i++) {
            gridObj = jsonArray.getJSONObject(i);
            startObj = gridObj.getJSONArray("start");
            endObj = gridObj.getJSONArray("end");

            grid = new Grid(i, new float[]{(float) startObj.getDouble(0), (float) startObj.getDouble(1)}, new float[]{(float) endObj.getDouble(0), (float) endObj.getDouble(1)});

            grids[i] = grid;
            Log.d("layout", grid.toString());
        }

        return new Layout(Float.parseFloat(s[0]), Float.parseFloat(s[1]), grids);

    }

    private static class DemoJsonBuildHelper {
        StringBuilder sb;
        boolean hasGrid;

        private DemoJsonBuildHelper() {
            sb = new StringBuilder();
        }

        static DemoJsonBuildHelper layout(String ratio) {
            DemoJsonBuildHelper helper = new DemoJsonBuildHelper();
            helper.sb.append("{").append("ratio:").append("\"").append(ratio).append("\"");
            return helper;
        }

        DemoJsonBuildHelper grid(String start, String end) {

            if (sb != null) {
                if (!hasGrid) {
                    sb.append(",grids:[");
                    sb.append("{start:").append(start).append(",end:").append(end).append("}");
                    hasGrid = true;
                } else {
                    sb.append(",{start:").append(start).append(",end:").append(end).append("}");
                }
            }
            return this;
        }

        String complete() {
            if (sb != null) {
                sb.append("]}");
                return sb.toString();
            }

            return null;
        }

    }
}
