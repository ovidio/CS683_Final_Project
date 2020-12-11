package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.R;
import com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.BaseFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ActivityFragment extends BaseFragment implements View.OnClickListener {
    String TAG = "activity_fragment";
    private DatabaseReference activityDatabase;
    DatePickerDialog picker;
    EditText editDateText;

    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activityDatabase = FirebaseDatabase.getInstance().getReference().child("activity");
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        view = setBackground(view);
        Button dbSaveButton = view.findViewById(R.id.updateDBDataButton);
        dbSaveButton.setOnClickListener(this);

        editDateText = view.findViewById(R.id.editTextDate);
        editDateText.setInputType(InputType.TYPE_NULL);
        editDateText.setOnClickListener(v -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(getActivity(),
                    (view1, year1, monthOfYear, dayOfMonth) -> editDateText.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1)), year, month, day);
            picker.show();
        });

        return view;
    }

    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        setViewText(view);
        getDBData(view);
    }

    public void setViewText(View view) {
        TextView fragInputHeaderView = view.findViewById(R.id.fragment_input_header);
        TextView fragmentInputDateView = view.findViewById(R.id.fragmentInputDate);
        EditText editTextDateView = view.findViewById(R.id.editTextDate);
        TextView fragmentInputSubHeaderView = view.findViewById(R.id.fragmentInputSubHeader);
        EditText editTextInputValueView = view.findViewById(R.id.editTextInputValue);

        TextView fragmentThirdInputHeaderView = view.findViewById(R.id.fragmentThirdInputHeader);
        EditText thirdInputTextView = view.findViewById(R.id.fragmentThirdInputValue);
        fragmentThirdInputHeaderView.setVisibility(View.VISIBLE);
        thirdInputTextView.setVisibility(View.VISIBLE);

        fragInputHeaderView.setText(getResources().getString(R.string.activity_fragment_header));
        fragmentInputDateView.setText(getResources().getString(R.string.date_subtitle));
        fragmentInputSubHeaderView.setText(getResources().getString(R.string.activity_fragment_sub_header));
        fragmentThirdInputHeaderView.setText(getResources().getString(R.string.activity_fragment_third_header));
        editTextDateView.setHint(getResources().getString(R.string.date_hint));
        editTextInputValueView.setHint(getResources().getString(R.string.activity_fragment_activity_hint));
        thirdInputTextView.setHint(getResources().getString(R.string.activity_fragment_activity_hint));
    }

    private void getDBData(View view){
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
        String activityUrl = activityDatabase + ".json" + "?orderBy=\"$priority\"&limitToLast=10&print=pretty";

        JsonObjectRequest activityJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, activityUrl, null, response -> {
            Log.d(TAG, "activity raw response: " + response.toString());
            List<String> activityDbXAxisKeysList = new ArrayList<>();
            Iterator<String> activityDbObjectKeys = response.keys();

            while(activityDbObjectKeys.hasNext()) {

                activityDbXAxisKeysList.add(activityDbObjectKeys.next());
            }
            // sort and log activityDbXAxisKeysList
            Collections.sort(activityDbXAxisKeysList);
            Log.d(TAG, "activity sorted keys List: " + activityDbXAxisKeysList);

            addTableRows(response, activityDbXAxisKeysList);
        }, error -> Log.d(TAG, error.toString()));

        requestQueue.add(activityJsonObjectRequest);
    }

    private void addTableRows(JSONObject dbObject, List<String> dbKeysList) {
        // reverse order of keyslist to start at latest entry
        Collections.reverse(dbKeysList);

        // instantiate tableLayout and clear all rows
        TableLayout tableLayout = requireActivity().findViewById(R.id.dbDataInputTableLayout);
        tableLayout.removeAllViews();

        // for every key create a new row with Date and Activity
        for (int i = -1; i < dbKeysList.size(); i++) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(5, 15, 15, 15);

            if (i >= 0) {
                String currentKeyValue = dbKeysList.get(i);

                // instantiate text views and set Date and Activity from DB
                TextView dateTextView = new TextView(getContext());
                TextView valueTextView = new TextView(getContext());
                TextView thirdValueView = new TextView(getContext());
                dateTextView.setText(String.format("Date: %s", formatDateForDisplay(currentKeyValue)));
                try {
                    valueTextView.setText(String.format("Length: %s",
                            dbObject.getJSONObject(currentKeyValue).get("len_of_exer")));
                    thirdValueView.setText(String.format("Level: %s",
                            dbObject.getJSONObject(currentKeyValue).get("level_exer")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create view params and set margins
                TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                viewParams.setMargins(15, 0, 5, 0);

                // set params for text views
                dateTextView.setLayoutParams(viewParams);
                valueTextView.setLayoutParams(viewParams);
                thirdValueView.setLayoutParams(viewParams);

                // add views to table row
                tableRow.addView(dateTextView);
                tableRow.addView(valueTextView);
                tableRow.addView(thirdValueView);
            } else {
                TableRow subTitleTableRow = new TableRow(getContext());
                subTitleTableRow.setPadding(5, 15, 45, 15);
                TextView subTitleTextView = new TextView(getContext());
                subTitleTextView.setText(getResources().getString(R.string.activity_fragment_table_header));

                TextView subtitleView = new TextView(getContext());
                subtitleView.setText(getResources().getString(R.string.activity_fragment_table_header));

                // create view params and set margins
                TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                viewParams.setMargins(45, 0, 0, 0);

                subtitleView.setLayoutParams(viewParams);
                tableRow.addView(subtitleView);
            }

            // add row and table layout params to table layout
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            tableLayoutParams.setMargins(45, i == -1 ? 30 : 0, 0, 0);
            tableLayout.addView(tableRow, tableLayoutParams);
        }
    }

    private String formatDateForDisplay(String rawDate) {
        // split string by dash
        String[] dateArray = rawDate.split("-");

        // return string in MM/DD format
        return dateArray[1] + "/" + dateArray[2];
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateForDB(String rawDate) {
        // split string by forward slash
        String[] dateArray = rawDate.split("/");

        return dateArray[2] + "-" + dateArray[1] + "-" + formatDayForDB(dateArray[0]);
    }

    private String formatDayForDB(String day) {
        if (Integer.parseInt(day) < 10) {
            return "0" + day;
        }

        return day;
    }

    @Override
    public void onClick(View view) {
        // grab edittext view
        EditText valueView = requireActivity().findViewById(R.id.editTextInputValue);
        EditText thirdValueView = view.findViewById(R.id.fragmentThirdInputValue);

        // get text value from edittexts
        Editable dateInputValue = editDateText.getText();
        Integer valueInputValue = Integer.parseInt(valueView.getText().toString());
        Integer thirdInputValue = Integer.parseInt(thirdValueView.getText().toString());
        valueView.getText().clear();
        thirdValueView.getText().clear();

    // create nested object needed for proper db formatting
        Map<String, Object> activityTop = new HashMap<>();
        Map<String, Integer> activityNested1 = new HashMap<>();
        Map<String, Integer> activityNested2 = new HashMap<>();
        activityNested1.put("len_of_exer", valueInputValue);
        activityNested2.put("level_exer", thirdInputValue);
        ArrayList<Map<String, Integer>> nestedItems = new ArrayList<>();
        nestedItems.add(activityNested1);
        nestedItems.add(activityNested2);

        activityTop.put(formatDateForDB(dateInputValue.toString()), nestedItems);

        // update DB
        activityDatabase.updateChildren(activityTop);

        // after db update, pull down newly updated data from db
        getDBData(view);
    }
}