package com.example.projekt_event_app.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.example.projekt_event_app.amount_boolean_checker.BooleanChecker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Fragment containing all the information of one given event, chosen in the startfragment from the listview.
 */
public class EventDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static Events mParam2;
    private static int position;

    private View mv;

    // TODO: Rename and change types of parameters
    private int mParam1;

    private StorageReference storageReference;

    private String name;
    private TextView description;
    private TextView attendance;
    private Event event;
    private List allattending;

    private TextView locationText;
    private TextView dateText;
    private TextView timeText;
    private TextView tv;
    private TextView nameText;

    private ImageView imageView;

    private Switch attendingSwitch;


    private EventDetailFragment.OnFragmentInteractionListener mListener;

    //private Events mParam2;

    public EventDetailFragment() {
        // Required empty public constructor
    }

    /**
     * creats a new eventDatailFragment and sets position and mParam2 to the given parameters.
     *
     * @param param1 Parameter 1.
     *
     * @return A new instance of fragment EventDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailFragment newInstance(int param1, Events events) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARAM1, param1);
        position = param1;
        mParam2 = events;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            //mParam1 = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mv = inflater.inflate(R.layout.fragment_event_detail, container, false);


        tv = mv.findViewById(R.id.DetailText);
        description = mv.findViewById(R.id.descriptionView);
        attendance = mv.findViewById(R.id.attendingView);

        locationText = mv.findViewById(R.id.placeText);
        dateText = mv.findViewById(R.id.endDateText);
        timeText = mv.findViewById(R.id.endTimeText);

        nameText = mv.findViewById(R.id.nameText);


        attendingSwitch = mv.findViewById(R.id.attendingSwitch);
        attendingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    makeAttend();
                    attendingSwitch.setText("Attending!");
                }
                else{
                    unAttend();
                    attendingSwitch.setText("Attend!");

                }
            }
        });

        imageView = mv.findViewById(R.id.detailImageView);


        event = mParam2.getEvent(position);
        event.getName();
        tv.setText(event.getName());
        nameText.setText(event.getHost());

        description.setText(event.getDescription());

        dateText.setText(event.getStart_Date());
        timeText.setText(event.getTime());

        locationText.setText(event.getLocation());

        allattending = event.getAttendance();

        checkAttending();


        int numberattending;

        if (allattending == null) {
            numberattending = 0;
        } else {
            numberattending = allattending.size();
        }

        attendance.setText("Attending: "+ numberattending);

        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToAttendListFrag(event.getId());
            }
        });


        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference ref = storageReference.child(event.getImageId() + ".jpg");


        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().into(imageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        Button button = mv.findViewById(R.id.goToComments);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToComments(event);

            }
        });

        final SwipeRefreshLayout pullToRefresh = mv.findViewById(R.id.eventUpdate);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateEvent();
                pullToRefresh.setRefreshing(false);
            }
        });
        return mv;
    }

    /**
     * sets view items to the information given from the request to the server.
     */
    private void updateEvent(){

        String Url = "https://projekt-app.herokuapp.com/get/event/" + event.getId();
        RequestQueue queue = Volley.newRequestQueue(getContext());


        JsonObjectRequest postUserRequest = new JsonObjectRequest(Request.Method.GET, Url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();

                        Event updatedEvent = gson.fromJson(response.toString(), Event.class);

                        tv.setText(updatedEvent.getName());
                        nameText.setText(updatedEvent.getHost());

                        description.setText(updatedEvent.getDescription());

                        dateText.setText(updatedEvent.getStart_Date());
                        timeText.setText(updatedEvent.getTime());

                        locationText.setText(updatedEvent.getLocation());

                        allattending = updatedEvent.getAttendance();

                        int numberattending;

                        if (allattending == null) {
                            numberattending = 0;
                        } else {
                            numberattending = allattending.size();
                        }

                        attendance.setText("Attending: " + numberattending);


                        storageReference = FirebaseStorage.getInstance().getReference("uploads");
                        StorageReference ref = storageReference.child(updatedEvent.getImageId() + ".jpg");


                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(getContext()).load(uri).fit().centerCrop().into(imageView);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
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

    /**
     * Sends the current events id to the server, which then updates the attendance list with the
     * user sending the request.
     */
    private void makeAttend(){

        String Url = "https://projekt-app.herokuapp.com/event/accept";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try {
            postObject.put("id", event.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest postEventRequest = new JsonObjectRequest(Request.Method.POST, Url, postObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Toast.makeText(getContext(), "Welcome to the event my dude", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences preferences = getContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String retrivedToken = preferences.getString("TOKEN", null);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };
        queue.add(postEventRequest);

    }

    /**
     * Sends a request with the current event to the server, which returns if the current user is attending the event or not.
     * and sets the switch in the view according the booleanChecker.
     */
    private void checkAttending(){
        String Url = "https://projekt-app.herokuapp.com/event/check";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try {
            postObject.put("id", event.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postUserRequest = new JsonObjectRequest(Request.Method.POST, Url, postObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();

                        for (int i = 0; i <response.length(); i++) {
                            BooleanChecker booleanChecker = gson.fromJson(response.toString(), BooleanChecker.class);
                            if( booleanChecker.getCheck().equals("true")){
                                attendingSwitch.setChecked(true);
                            } else {
                                attendingSwitch.setChecked(false);
                            }
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

    /**
     * Sends a request to the server to delete the current user from the attendence list on the given event.
     */
    private void unAttend(){

        String Url = "https://projekt-app.herokuapp.com/event/delete";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try {
            postObject.put("id", event.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest postEventRequest = new JsonObjectRequest(Request.Method.POST, Url, postObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Toast.makeText(getContext(), "Welcome to the event my dude", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences preferences = getContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String retrivedToken = preferences.getString("TOKEN", null);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };
        queue.add(postEventRequest);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void goToAttendListFrag(int event_id);
        void goToComments(Event event);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EventDetailFragment.OnFragmentInteractionListener) {
            mListener = (EventDetailFragment.OnFragmentInteractionListener) context;
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


