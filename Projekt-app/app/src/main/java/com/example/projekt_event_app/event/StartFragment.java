package com.example.projekt_event_app.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;


/**
 * The mainFragment which displays all the events, using the eventAdapter.
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {

    private View mv;
    private ArrayList<String> mDescriptions;
    private ArrayList<String> mName;
    private ArrayList<Uri> mImageUris;

    private TextView textHost;
    private TextView textName;

    private EventAdapter adapter;

    private Event gstring;

    private ArrayList<Event> eventList = new ArrayList<>();

    private ListView listView;

    private StartFragment.OnFragmentInteractionListener mListener;

    public StartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment StartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mv = inflater.inflate(R.layout.fragment_start, container, false);
        mListener.selectIconHome();
        final Events events = new Events();
        mDescriptions = new ArrayList<>();
        mName = new ArrayList<>();
        listView = mv.findViewById(R.id.ListView);
        textHost = mv.findViewById(R.id.EventHost);
        textName = mv.findViewById(R.id.EventName);

        //gatherInformation();
        String gruppUrl = "https://projekt-app.herokuapp.com/event/all";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest = new JsonArrayRequest(Request.Method.GET, gruppUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        try {

                            for (int i = 0; i <response.length(); i++) {
                                Event event = gson.fromJson(response.getJSONObject(i).toString(), Event.class);
                                events.addEvent(event);
                            }
                            adapter = new EventAdapter(getContext(), R.layout.row, events.getEvents());
                            listView.setAdapter(adapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {

            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences preferences = getContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String retrivedToken  = preferences.getString("TOKEN",null);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }

        };

        queue.add(postUserRequest);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListener.getDetail(position, events);
            }
        });

        return mv;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void sendToBlacklist();
        void selectIconHome();
        void getDetail(int pos, Events events);
        void addEvent(Event event);
        Event getEvent(int i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StartFragment.OnFragmentInteractionListener) {
            mListener = (StartFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
