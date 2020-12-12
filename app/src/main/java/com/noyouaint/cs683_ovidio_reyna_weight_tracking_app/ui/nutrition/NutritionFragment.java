package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.nutrition;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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

public class NutritionFragment extends BaseFragment implements View.OnClickListener{
    String TAG = "nutrition_fragment";
    private DatabaseReference nutritionDatabase;
    DatePickerDialog picker;
    EditText editDateText;

    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        nutritionDatabase = FirebaseDatabase.getInstance().getReference().child("calories");
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);
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
        TextView fragmentThirdInputHeaderView = view.findViewById(R.id.fragmentThirdInputHeader);
        EditText thirdInputTextView = view.findViewById(R.id.fragmentThirdInputValue);
        fragmentThirdInputHeaderView.setVisibility(View.GONE);
        thirdInputTextView.setVisibility(View.GONE);

        setViewText(view);
        getDBData(view);
    }

    public void setViewText(View view) {
        TextView fragInputHeaderView = view.findViewById(R.id.fragment_input_header);
        TextView fragmentInputDateView = view.findViewById(R.id.fragmentInputDate);
        EditText editTextDateView = view.findViewById(R.id.editTextDate);
        TextView fragmentInputSubHeaderView = view.findViewById(R.id.fragmentInputSubHeader);
        EditText editTextInputValueView = view.findViewById(R.id.editTextInputValue);

        SpannableString header = new SpannableString(getResources()
                .getString(R.string.nutrition_fragment_header));
        header.setSpan(new UnderlineSpan(), 0, header.length(), 0);

        fragInputHeaderView.setText(header);
        fragmentInputDateView.setText(getResources().getString(R.string.date_subtitle));
        editTextDateView.setHint(getResources().getString(R.string.date_hint));
        fragmentInputSubHeaderView.setText(getResources().getString(R.string.nutrition_fragment_sub_header));
        editTextInputValueView.setHint(getResources().getString(R.string.nutrition_fragment_calories_hint));
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
        String nutritionUrl = nutritionDatabase + ".json" + "?orderBy=\"$priority\"&limitToLast=10&print=pretty";

        JsonObjectRequest nutritionJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, nutritionUrl, null, response -> {
            Log.d(TAG, "nutrition raw response: " + response.toString());
            List<String> nutritionDbXAxisKeysList = new ArrayList<>();
            Iterator<String> nutritionDbObjectKeys = response.keys();

            while(nutritionDbObjectKeys.hasNext()) {

                nutritionDbXAxisKeysList.add(nutritionDbObjectKeys.next());
            }
            // sort and log nutritionDbXAxisKeysList
            Collections.sort(nutritionDbXAxisKeysList);
            Log.d(TAG, "nutrition sorted keys List: " + nutritionDbXAxisKeysList);

            addTableRows(response, nutritionDbXAxisKeysList);
        }, error -> Log.d(TAG, error.toString()));

        requestQueue.add(nutritionJsonObjectRequest);
    }

    private void addTableRows(JSONObject dbObject, List<String> dbKeysList) {
        // reverse order of keyslist to start at latest entry
        Collections.reverse(dbKeysList);

        // instantiate tableLayout and clear all rows
        TableLayout tableLayout = requireActivity().findViewById(R.id.dbDataInputTableLayout);
        tableLayout.removeAllViews();

        // for every key create a new row with Date and Calories
        for (int i = -1; i < dbKeysList.size(); i++) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(0, i == -1 ? 0 : 15, 0, 15);
            tableRow.setGravity(3);
            tableRow.setWeightSum(1);

            if (i >= 0) {
                // instantiate text views and set Date and Calories from DB
                TextView dateTextView = new TextView(getContext());
                dateTextView.setText(String.format("Date: %s", formatDateForDisplay(dbKeysList.get(i))));
                TextView valueTextView = new TextView(getContext());
                try {
                    valueTextView.setText(String.format("Calories: %s",
                            dbObject.getJSONObject(dbKeysList.get(i)).get("calories")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create view params and set margins
                TableRow.LayoutParams viewParams = new TableRow.LayoutParams();
                viewParams.width = 0;
                viewParams.height = TableRow.LayoutParams.WRAP_CONTENT;
                viewParams.weight = (float) 0.5;

                // set params for text views
                dateTextView.setLayoutParams(viewParams);
                valueTextView.setLayoutParams(viewParams);

                // add views to table row
                tableRow.addView(dateTextView);
                tableRow.addView(valueTextView);
            } else {
                TextView subtitleView = new TextView(getContext());
                subtitleView.setText(getResources().getString(R.string.nutrition_fragment_table_header));

                // create view params and set margins
                TableRow.LayoutParams viewParams = new TableRow.LayoutParams();
                viewParams.width = TableRow.LayoutParams.WRAP_CONTENT;
                viewParams.height = TableRow.LayoutParams.WRAP_CONTENT;


                subtitleView.setLayoutParams(viewParams);
                tableRow.addView(subtitleView);
            }

            // add row and table layout params to table layout
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            tableLayoutParams.setMargins(15,  0, 0, 0);
            tableLayout.addView(tableRow, tableLayoutParams);
        }
    }

    private String formatDateForDisplay(String rawDate) {
        // split string by dash
        String[] dateArray = rawDate.split("-");

        // return string in MM/DD format
        return String.format("%s/%s", dateArray[1], dateArray[2]);
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

        // get text value from edittexts
        Editable dateInputValue = editDateText.getText();
        Double valueInputValue = Double.parseDouble(valueView.getText().toString());
        valueView.getText().clear();

        // create nested object needed for proper db formatting
        Map<String, Object> nutritionTop = new HashMap<>();
        Map<String, Double> nutritionNested = new HashMap<>();
        nutritionNested.put("calories", valueInputValue);
        nutritionTop.put(formatDateForDB(dateInputValue.toString()), nutritionNested);

        // update DB
        nutritionDatabase.updateChildren(nutritionTop);

        // after db update, pull down newly updated data from db
        getDBData(view);
    }
}