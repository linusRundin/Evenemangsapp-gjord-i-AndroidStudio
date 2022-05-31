package com.example.projekt_event_app.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.example.projekt_event_app.event.Event;
import com.example.projekt_event_app.event.EventAdapter;
import com.example.projekt_event_app.event.Events;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;


/**
 * This is the fragment showing a users events.
 * A simple {@link Fragment} subclass.
 * Use the {@link UserEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserEventsFragment extends Fragment {

    private View mv;

    private EventAdapter adapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Events events;

    private ListView eventList;

    private static String userID;

    private UserEventsFragment.OnFragmentInteractionListener mListener;

    /**
     * This Fragment shows the listView where the events that the user is attending is displayed.
     */
    public UserEventsFragment() {
    }

    /**
     * newInstance is called when a new event list is needed.
     * @param id is used t get the events that the user is attending to.
     * @return the fragment
     */
    // TODO: Rename and change types and number of parameters
    public static UserEventsFragment newInstance(String id) {
        UserEventsFragment fragment = new UserEventsFragment();
        Bundle args = new Bundle();
        userID = id;
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
        mv = inflater.inflate(R.layout.fragment_user_events, container, false);

        eventList = mv.findViewById(R.id.userEvents);

        events = new Events();

        String gruppUrl = "https://projekt-app.herokuapp.com/my/attendings/"+userID;
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
                            eventList.setAdapter(adapter);


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

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListener.getDetail(position, events);
            }
        });


        return mv;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void getDetail(int pos, Events events);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserEventsFragment.OnFragmentInteractionListener) {
            mListener = (UserEventsFragment.OnFragmentInteractionListener) context;
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
