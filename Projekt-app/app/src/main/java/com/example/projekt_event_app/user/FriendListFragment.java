package com.example.projekt_event_app.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;


/**
 * This fragment shows a users following in a listView.
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendListFragment extends Fragment {
    private static String userId;

    private ListView listView;

    private ListAdapter listAdapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Users users;

    private View mv;

    private FriendListFragment.OnFragmentInteractionListener mListener;



    public FriendListFragment() {
    }

    /**
     * @param user_Id is the userId that will be used to get the followers of that user.
     * @return A new instance of fragment FriendList.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendListFragment newInstance(String user_Id) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();

        userId = user_Id;

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
        mv = inflater.inflate(R.layout.fragment_friend_list, container, false);


        listView = mv.findViewById(R.id.friendListView);

        String gruppUrl = "https://projekt-app.herokuapp.com/get/friends/" + userId;
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest = new JsonArrayRequest(Request.Method.GET, gruppUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        users = new Users();
                        try {

                            for (int i = 0; i <response.length(); i++) {
                                User user = gson.fromJson(response.getJSONObject(i).toString(), User.class);
                                users.addUser(user);
                            }
                           listAdapter = new ListAdapter(getContext(), R.layout.list_view_item, users.getUsers());
                            listView.setAdapter(listAdapter);


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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = users.getUser(position);
                mListener.sendUserProfile(user);
            }
        });

        return mv;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void sendUserProfile(User user);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FriendListFragment.OnFragmentInteractionListener) {
            mListener = (FriendListFragment.OnFragmentInteractionListener) context;
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
