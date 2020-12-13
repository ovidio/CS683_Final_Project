package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.weight;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class WeightFragment extends BaseFragment implements View.OnClickListener{
    String TAG = "weight_fragment";
    private DatabaseReference weightDatabase;
    DatePickerDialog picker;
    EditText editDateText;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // set DB to correct child
        weightDatabase = FirebaseDatabase.getInstance().getReference().child("weight");

        // inflate and set background color for fragment
        View view = inflater.inflate(R.layout.fragment_weight, container, false);
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
        TextView customFragmentTableHeader = view.findViewById(R.id.customFragmentTableHeader);

        SpannableString header = new SpannableString(getResources()
                .getString(R.string.nutrition_fragment_header));
        header.setSpan(new UnderlineSpan(), 0, header.length(), 0);

        fragInputHeaderView.setText(header);
        fragmentInputDateView.setText(getResources().getString(R.string.date_subtitle));
        fragmentInputSubHeaderView.setText(getResources().getString(R.string.weight_fragment_sub_header));
        customFragmentTableHeader.setText(getResources().getString(R.string.weight_fragment_table_header));
        editTextDateView.setHint(getResources().getString(R.string.date_hint));
        editTextInputValueView.setHint(getResources().getString(R.string.weight_fragment_weight_hint));

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
        String weightUrl = weightDatabase + ".json" + "?orderBy=\"$priority\"&limitToLast=30&print=pretty";

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

            addTableRows(response, weightDbXAxisKeysList);
        }, error -> Log.d(TAG, error.toString()));

        requestQueue.add(weightJsonObjectRequest);
    }

    private void addTableRows(JSONObject dbObject, List<String> dbKeysList) {
        // reverse order of keyslist to start at latest entry
        Collections.reverse(dbKeysList);

        // instantiate tableLayout and clear all rows
        TableLayout tableLayout = requireActivity().findViewById(R.id.dbDataInputTableLayout);
        tableLayout.removeAllViews();

        // for every key create a new row with Date and Weight
        for (int i = 0; i < dbKeysList.size(); i++) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(0, 15, 0, 15);
            tableRow.setGravity(3);
            tableRow.setWeightSum(1);

            // instantiate text views and set Date and Weight from DB
            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(String.format("Date: %s", formatDateForDisplay(dbKeysList.get(i))));
            TextView valueTextView = new TextView(getContext());
            try {
                valueTextView.setText(String.format("Weight: %s",
                        dbObject.getJSONObject(dbKeysList.get(i)).get("weight")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // create view params and set margins
            TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT);
            viewParams.width = 0;
            viewParams.height = TableRow.LayoutParams.WRAP_CONTENT;
            viewParams.weight = (float) 0.5;

            // set params for text views
            dateTextView.setLayoutParams(viewParams);
            valueTextView.setLayoutParams(viewParams);

            // add views to table row
            tableRow.addView(dateTextView);
            tableRow.addView(valueTextView);

            // add row and table layout params to table layout
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            tableLayoutParams.setMargins(15, 0, 0, 0);
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

        // get text value from edittexts
        Editable dateInputValue = editDateText.getText();
        Double valueInputValue = Double.parseDouble(valueView.getText().toString());
        valueView.getText().clear();

        // create nested object needed for proper db formatting
        Map<String, Object> weightTop = new HashMap<>();
        Map<String, Double> weightNested = new HashMap<>();
        weightNested.put("weight", valueInputValue);
        weightTop.put(formatDateForDB(dateInputValue.toString()), weightNested);

        // update DB
        weightDatabase.updateChildren(weightTop);

        // after db update, pull down newly updated data from db
        getDBData(view);
    }
}