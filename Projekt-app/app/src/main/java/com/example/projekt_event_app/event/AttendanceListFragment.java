package com.example.projekt_event_app.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.example.projekt_event_app.user.ListAdapter;
import com.example.projekt_event_app.R;
import com.example.projekt_event_app.user.User;
import com.example.projekt_event_app.user.Users;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;


/**
 * This Fragment displays a given events attendance in a costume listview.
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListAdapter adapter;
    private ListView listView;
    private Users users;

    private static int eventId;

    private View mv;

    private AttendanceListFragment.OnFragmentInteractionListener mListener;

    public AttendanceListFragment() {
        // Required empty public constructor
    }

    /**
     * Takes an int containing the id of the event which attendance will be displayed and sets
     * the global variable eventId.
     * @return A new instance of fragment AttendanceListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendanceListFragment newInstance(int event_id) {
        AttendanceListFragment fragment = new AttendanceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        eventId = event_id;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mv = inflater.inflate(R.layout.fragment_attendance_list, container, false);
        getUsers();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = users.getUser(position);
                mListener.sendUserProfile(user);
            }
        });

        return mv;
    }

    /**
     * requests the attendance on the given event and sets the listview according to the response
     * using the classes ListAdapter, user and users.
     */
    private void getUsers() {
        users = new Users();
        listView = mv.findViewById(R.id.profileListView);


        String gruppUrl = "https://projekt-app.herokuapp.com/get/attendance/"+eventId;
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest = new JsonArrayRequest(Request.Method.GET, gruppUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        try {

                            for (int i = 0; i <response.length(); i++) {
                                User user = gson.fromJson(response.getJSONObject(i).toString(), User.class);
                                users.addUser(user);
                            }
                            adapter = new ListAdapter(getContext(), R.layout.list_view_item, users.getUsers());
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

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void sendUserProfile(User user);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AttendanceListFragment.OnFragmentInteractionListener) {
            mListener = (AttendanceListFragment.OnFragmentInteractionListener) context;
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

