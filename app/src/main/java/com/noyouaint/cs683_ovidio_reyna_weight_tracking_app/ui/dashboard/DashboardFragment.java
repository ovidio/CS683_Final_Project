package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.dashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.anychart.enums.HoverMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class DashboardFragment extends Fragment {
    String TAG = "dashboard_fragment";
    private DatabaseReference weightDatabase, nutritionDatabase;
    private ProgressBar spinner;
    Boolean lineChartComplete = false;
    Boolean barChartComplete = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        weightDatabase = FirebaseDatabase.getInstance().getReference().child("weight");
        nutritionDatabase = FirebaseDatabase.getInstance().getReference().child("calories");



        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        spinner = (ProgressBar) getView().findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        // Instantiate the cache
        Cache cache = new DiskBasedCache(requireActivity().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        RequestQueue requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(view.getContext());
        String weightUrl = weightDatabase + ".json" + "?orderBy=\"$priority\"&limitToLast=7&print=pretty";
        String nutritionUrl = nutritionDatabase + ".json" + "?orderBy=\"$priority\"&limitToLast=7&print=pretty";

        JsonObjectRequest weightJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, weightUrl, null, response -> {
            Log.d(TAG, "weight raw response: " + response.toString());
            List<String> weightDbXAxisKeysList = new ArrayList<>();
            Iterator<String> weightDbObjectKeys = response.keys();

            while(weightDbObjectKeys.hasNext()) {

                weightDbXAxisKeysList.add(weightDbObjectKeys.next());
            }
            // sort and log weightDbXAxisKeysList
            Collections.sort(weightDbXAxisKeysList);
            Log.d(TAG, "weight sorted keys List: " + weightDbXAxisKeysList);

            PopulateLineChart(response, weightDbXAxisKeysList);
        }, error -> Log.d(TAG, error.toString()));

        JsonObjectRequest nutritionJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, nutritionUrl, null, response -> {
            Log.d(TAG, "nutrition raw response: " + response.toString());
            List<String> nutritionDbXAxisKeysList = new ArrayList<>();
            Iterator<String> nutritionDbObjectKeys = response.keys();
            while(nutritionDbObjectKeys.hasNext()) {

                nutritionDbXAxisKeysList.add(nutritionDbObjectKeys.next());
            }

            Collections.sort(nutritionDbXAxisKeysList);
            Log.d(TAG, "nutrition sorted keys List: " + nutritionDbXAxisKeysList);

            PopulateBarChart(response, nutritionDbXAxisKeysList);
        }, error -> Log.d(TAG, error.toString()));

        // Add the request to the RequestQueue.
        requestQueue.add(weightJsonObjectRequest);
        requestQueue.add(nutritionJsonObjectRequest);
    }

    private static class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value) {
            super(x, value);
        }

    }

    private void PopulateLineChart(JSONObject dbObject, @NotNull List<String> xAxisKeys) {
        AnyChartView weightChartView = (AnyChartView) getView().findViewById(R.id.any_chart_weight_view);
        APIlib.getInstance().setActiveAnyChartView(weightChartView);
        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Last 7 Days of Weigh-ins");

        cartesian.yAxis(0).title("Weight");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> seriesData = new ArrayList<>();
        for (int i = 0; i < xAxisKeys.size(); i++) {
            Number seriesNumber = null;
            try {
                seriesNumber = (Number) dbObject.getJSONObject(xAxisKeys.get(i)).get("weight");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            seriesData.add(new CustomDataEntry(xAxisKeys.get(i), seriesNumber));
        }

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("Weight");
        series1.stroke("5 red");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        weightChartView.setChart(cartesian);
        lineChartComplete = true;
        if (barChartComplete) {
            removeSpinner();
        }
    }
    private void PopulateBarChart(JSONObject dbObject, @NotNull List<String> xAxisKeys) {
        AnyChartView nutritionChartView = getView().findViewById(R.id.any_chart_nutrition_View);
        APIlib.getInstance().setActiveAnyChartView(nutritionChartView);

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        for (int i = 0; i < xAxisKeys.size(); i++) {
            Number seriesNumber = null;
            try {
                seriesNumber = (Number) dbObject.getJSONObject(xAxisKeys.get(i)).get("calories");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            data.add(new CustomDataEntry(xAxisKeys.get(i), seriesNumber));
        }

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator:\\,}");
        column.normal().fill("gold");

        cartesian.animation(true);
        cartesian.title("Last 7 Days of Calorie Consumption");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:\\,}");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Calories");
        cartesian.yAxis(0).title("Date");

        nutritionChartView.setChart(cartesian);

        barChartComplete = true;
        if (lineChartComplete) {
            removeSpinner();
        }
    }

    private void removeSpinner() {
        spinner.setVisibility(View.GONE);
    }
}