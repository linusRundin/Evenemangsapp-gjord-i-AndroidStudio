package com.example.projekt_event_app.commments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.event.Event;
import com.example.projekt_event_app.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Fragment used to show comments in a adapted listview.
 */
public class CommentsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static Event currentEvent;

    private View mv;

    private CommentsAdapter adapter;

    private ListView commentsView;
    private Button addCommentButton;
    private EditText newComment;

    private Comments comments;

    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     *
     * Sets currentEvent to the event given in the newInstance function.
     *
     * @return A new instance of fragment CommentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(Event event) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        currentEvent = event;
        fragment.setArguments(args);
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
        mv = inflater.inflate(R.layout.fragment_comments, container, false);
        comments = new Comments();
        commentsView = mv.findViewById(R.id.commentList);
        addCommentButton = mv.findViewById(R.id.AddCommentButton);
        newComment = mv.findViewById(R.id.CommentContent);

        String gruppUrl = "https://projekt-app.herokuapp.com/get/comments/" + currentEvent.getId();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest = new JsonArrayRequest(Request.Method.GET, gruppUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        try {

                            for (int i = 0; i <response.length(); i++) {
                                Comment comment = gson.fromJson(response.getJSONObject(i).toString(), Comment.class);
                                comments.addComment(comment);
                            }
                            adapter = new CommentsAdapter(getContext(), R.layout.comment, comments.getComments());
                            commentsView.setAdapter(adapter);


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


        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gruppUrl = "https://projekt-app.herokuapp.com/add/comment";
                RequestQueue queue = Volley.newRequestQueue(getContext());
                JSONObject postObject = new JSONObject();
                try {
                    postObject.put("id", currentEvent.getId());
                    postObject.put("content", newComment.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest postUserRequest = new JsonObjectRequest(Request.Method.POST, gruppUrl, postObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


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


                queue.add(postUserRequest);




            }
        });

        final SwipeRefreshLayout pullToRefresh = mv.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               updateComments();
                pullToRefresh.setRefreshing(false);
            }
        });


        return mv;
    }

    /**
     * Request comments corresponding to currentEvent, and update the listview
     * according to the new comments using the CommentsAdapter.
     */
    public void updateComments(){
        String gruppUrl2 = "https://projekt-app.herokuapp.com/get/comments/" + currentEvent.getId();
        RequestQueue queue2 = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest2 = new JsonArrayRequest(Request.Method.GET, gruppUrl2, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        Comments comm = new Comments();
                        try {

                            for (int i = 0; i <response.length(); i++) {
                                Comment comment = gson.fromJson(response.getJSONObject(i).toString(), Comment.class);
                                comm.addComment(comment);
                            }
                            adapter = new CommentsAdapter(getContext(), R.layout.comment, comm.getComments());
                            commentsView.setAdapter(adapter);


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

        queue2.add(postUserRequest2);



    }
}
