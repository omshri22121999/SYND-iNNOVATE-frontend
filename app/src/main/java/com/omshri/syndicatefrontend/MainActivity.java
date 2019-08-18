package com.omshri.syndicatefrontend;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv;
    private ImageButton mSpeakBtn;
    private ListView lView;
    private ArrayList<String> lTextItems= new ArrayList<>();
    private ListAdapter adapter;
    private EditText etIpaddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lView=(ListView)findViewById(R.id.listView);
        adapter = new ListAdapter(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, lTextItems);
        mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);
        mVoiceInputTv.setVisibility(View.GONE);
        etIpaddress = (EditText) findViewById(R.id.et_ipaddress);
        lView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please tell your request");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.v("Check",result.toString());
                    mVoiceInputTv.setText(result.get(0));
                    lTextItems.add(result.get(0));
                    adapter.notifyDataSetChanged();
                    requestData(result.get(0));
                    scrollMyListViewToBottom();
                }
                break;
            }
        }
    }

    protected void requestData(String req){
        RequestQueue queue = Volley.newRequestQueue(this);
        String st = etIpaddress.getText().toString();
        String url ="http://"+st+":5050/get_action?query="+req;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SpannableString s = new SpannableString(response.replace("\"",""));
                        s.setSpan(new ForegroundColorSpan(Color.parseColor("#FFA500")),0,response.length()-2,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.v("Hello",response);
                        lTextItems.add(response.replace("\"","").replace("\n","")+":-");
                        adapter.notifyDataSetChanged();
                        scrollMyListViewToBottom();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Useless!",Toast.LENGTH_SHORT);
            }
        });
        queue.add(stringRequest);

    }
    private void scrollMyListViewToBottom() {
        lView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                lView.setSelection(adapter.getCount() - 1);
            }
        });
    }
}

class ListAdapter extends ArrayAdapter<String> {

    public ListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (convertView == null){
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }
        String word = getItem(position);
        assert word != null;
        SpannableString s = new SpannableString(word.replace(":-",""));
        if(word.contains(":-")){
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#FFA500")),0,word.length()-2,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else{
            s.setSpan(new ForegroundColorSpan(Color.BLUE),0,word.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        textView.setText(s);
        return convertView;

    }
}