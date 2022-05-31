package com.example.projekt_event_app.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.example.projekt_event_app.amount_boolean_checker.Amount;
import com.example.projekt_event_app.amount_boolean_checker.BooleanChecker;
import com.example.projekt_event_app.event.Event;
import com.example.projekt_event_app.event.EventAdapter;
import com.example.projekt_event_app.event.Events;
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
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 * This Fragment is used to show a user profile.
 *
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    private static User user;

    private UserFragment.OnFragmentInteractionListener mListener;

    private View mv;

    private TextView AttendanceButton;

    private TextView followersButton;

    private Events eventList;
    
    private ImageView profileImage;

    private Button FollowButton;
    private Button UnFollowButton;

    private TextView eventcreatedtv;
    private TextView followingtv;
    private TextView followerstv;
    private ListView attendingListUser;

    private StorageReference storageReference;

    public UserFragment() {
    }

    /**
     * newInstance is called everytime a new user profile is needed.
     * @param setUser is the user who is going to be displayed
     * @return a new UserFragment
     */
    public static UserFragment newInstance(User setUser) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        user = setUser;
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
        mv = inflater.inflate(R.layout.fragment_user, container, false);

        checkFriend();

        eventList = new Events();
        profileImage = mv.findViewById(R.id.user_profile_pic);

        FollowButton = mv.findViewById(R.id.FollowButton);
        UnFollowButton = mv.findViewById(R.id.UnFollowButton);
        UnFollowButton.setVisibility(View.GONE);

        attendingListUser = mv.findViewById(R.id.eventsAttendingUser);

        eventcreatedtv = mv.findViewById(R.id.EventsCreatedTV);
        followerstv = mv.findViewById(R.id.FollowersTV);
        followingtv = mv.findViewById(R.id.FollowingTV);


        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference ref = storageReference.child(user.getProfile_picture_id() + ".jpg");

        TextView tv = mv.findViewById(R.id.userName);
        tv.setText(user.getName());



        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().into(profileImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        followersButton = mv.findViewById(R.id.FollowersOnClick);

        TextView followingButton = mv.findViewById(R.id.FollowingOnClick);

        followingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToFriendList(user.getId());
            }
        });

        followersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        FollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeFollow();
                FollowButton.setVisibility(View.GONE);
                UnFollowButton.setVisibility(View.VISIBLE);
            }
        });

        UnFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unFollow();
                FollowButton.setVisibility(View.VISIBLE);
                UnFollowButton.setVisibility(View.GONE);
            }
        });
        getAttendings();
        getAmount("events/created");
        getAmount("followers");
        getAmount("following");

        return mv;
    }

    /**
     * This function sends a request to make a follow on a user.
     * It sends with the userID that is gathered to show the profile and connects it with the userID
     * that is sent in the header.
     */
    private void makeFollow(){
        String Url = "https://projekt-app.herokuapp.com/add/friend";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try {
            postObject.put("id", user.getId());
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
     * This function requests the database for information about the amount of events the userId is
     * attending etc.
     * @param url is which adress the different values is at.
     */
    private void getAmount(final String url){

        String Url = "https://projekt-app.herokuapp.com/"+url +"/"+user.getId();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest postEventRequest = new JsonObjectRequest(Request.Method.GET, Url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        Amount amount = gson.fromJson(response.toString(), Amount.class);
                        if (url == "events/created"){
                            eventcreatedtv.setText(amount.getAmount());
                        } else if (url == "followers") {
                            followerstv.setText(amount.getAmount());
                        } else if (url == "following") {
                            followingtv.setText(amount.getAmount());
                        }
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
     * This function sends a request asking the database if the user who is logged in is a friend
     * of the user in the current instance.
     */
    private void checkFriend(){

        String Url = "https://projekt-app.herokuapp.com/check/friend";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            postObject.put("id", user.getId());
            array.put(postObject);
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
                                    FollowButton.setVisibility(View.GONE);
                                    UnFollowButton.setVisibility(View.VISIBLE);
                                } else {
                                    FollowButton.setVisibility(View.VISIBLE);
                                    UnFollowButton.setVisibility(View.GONE);

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
     * This function sends a request to the database making a unfollow on the current users profile.
     */
    private void unFollow(){
        String Url = "https://projekt-app.herokuapp.com/unfriend/friend";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try {
            postObject.put("id", user.getId());
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
     * This function uses the current user to get which events the user is attending.
     */
    private void getAttendings(){
        final ListView attendingList = mv.findViewById(R.id.eventsAttending);

        String gruppUrl = "https://projekt-app.herokuapp.com/my/attendings/"+user.getId();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest = new JsonArrayRequest(Request.Method.GET, gruppUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        
                        try {

                            for (int i = 0; i <response.length(); i++) {
                                Event event = gson.fromJson(response.getJSONObject(i).toString(), Event.class);
                                eventList.addEvent(event);
                            }
                            if (response.length() != 0) {
                                EventAdapter newAdapter = new EventAdapter(getContext(), R.layout.row, eventList.getEvents());
                                attendingListUser.setAdapter(newAdapter);
                            }



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

        attendingListUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListener.getDetail(position, eventList);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void goToEvents(String id);
        void goToFriendList(String id);

        void getDetail(int position, Events eventList);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserFragment.OnFragmentInteractionListener) {
            mListener = (UserFragment.OnFragmentInteractionListener) context;
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
