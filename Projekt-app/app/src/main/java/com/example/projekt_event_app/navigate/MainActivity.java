package com.example.projekt_event_app.navigate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.example.projekt_event_app.commments.CommentsFragment;
import com.example.projekt_event_app.event.AttendanceListFragment;
import com.example.projekt_event_app.event.Event;
import com.example.projekt_event_app.event.EventCreatorFragment;
import com.example.projekt_event_app.event.EventDetailFragment;
import com.example.projekt_event_app.event.Events;
import com.example.projekt_event_app.event.StartFragment;
import com.example.projekt_event_app.register_login.LoginActivity;
import com.example.projekt_event_app.register_login.ProfileFragment;
import com.example.projekt_event_app.user.FriendListFragment;
import com.example.projekt_event_app.user.User;
import com.example.projekt_event_app.user.UserEventsFragment;
import com.example.projekt_event_app.user.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


/**
 * This activity handles the majority of the fragments. Handles which fragments go to eachother etc.
 */
public class MainActivity extends AppCompatActivity implements StartFragment.OnFragmentInteractionListener,
        EventCreatorFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener,
EventDetailFragment.OnFragmentInteractionListener, AttendanceListFragment.OnFragmentInteractionListener,
        UserFragment.OnFragmentInteractionListener, UserEventsFragment.OnFragmentInteractionListener,
FriendListFragment.OnFragmentInteractionListener{

    private StartFragment startFragment;
    private BottomNavigationView bottomNav;
    private ArrayList<Event> eventsList;

    private UserEventsFragment userEventsFragment;

    private EventDetailFragment detail;
    private AttendanceListFragment attendanceListFragment;
    private UserFragment userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View v = findViewById(R.id.placeHolder);
        eventsList = new ArrayList<>();

        startFragment = new StartFragment();
        detail = new EventDetailFragment();



        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.setSelectedItemId(R.id.nav_home);

    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new StartFragment();
                            break;
                        case R.id.nav_event_creator:
                            selectedFragment = new EventCreatorFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeHolder,
                            selectedFragment).addToBackStack(null).commit();
                    return true;
                }
            };


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This function sends a request trying to logout the current user.
     */
    @Override
    public void sendToBlacklist() {
        String gruppUrl = "https://projekt-app.herokuapp.com/user/logout";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest postUserRequest = new JsonObjectRequest(Request.Method.POST, gruppUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        }) {

            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String retrivedToken  = preferences.getString("TOKEN",null);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }

        };
        queue.add(postUserRequest);
    }

    @Override
    public void selectIconHome() {
        bottomNav.getMenu().getItem(1).setChecked(true);
    }

    @Override
    public void getDetail(int pos, Events events) {
        if (findViewById(R.id.placeHolder) != null) {
            detail = EventDetailFragment.newInstance(pos, events);
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.placeHolder, detail).addToBackStack(null).commit();

        }
    }

    @Override
    public void selectIconMess() {
        bottomNav.getMenu().getItem(0).setChecked(true);

    }
    @Override
    public void selectIconProfile() {
        bottomNav.getMenu().getItem(2).setChecked(true);

    }

    @Override
    public void goToFriendList(String id) {
        FriendListFragment friendListFragment = new FriendListFragment();
        friendListFragment = FriendListFragment.newInstance(id);
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.placeHolder, friendListFragment).addToBackStack(null).commit();

    }

    @Override
    public void goToComments(Event event) {
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment = commentsFragment.newInstance(event);
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.placeHolder, commentsFragment).addToBackStack(null).commit();

    }

    @Override
    public void addEvent(Event event) {
        eventsList.add(event);
    }

    @Override
    public Event getEvent(int i) {
        return eventsList.get(i);
    }

    @Override
    public void goToAttendListFrag(int eventId) {
        attendanceListFragment = new AttendanceListFragment();
        if (findViewById(R.id.placeHolder) != null) {
            attendanceListFragment = AttendanceListFragment.newInstance(eventId);
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.placeHolder, attendanceListFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void sendUserProfile(User user) {
        userFragment = new UserFragment();
        if (findViewById(R.id.placeHolder) != null) {
            userFragment = UserFragment.newInstance(user);
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.placeHolder, userFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void goToEvents(String id) {
        userEventsFragment = new UserEventsFragment();
        if (findViewById(R.id.placeHolder) != null) {
            userEventsFragment = userEventsFragment.newInstance(id);
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.placeHolder, userEventsFragment).addToBackStack(null).commit();
        }

    }

}
