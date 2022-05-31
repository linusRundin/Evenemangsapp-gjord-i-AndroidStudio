package com.example.projekt_event_app.register_login;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.fragment.app.Fragment;


/**
 * This Fragment is used to login a user.
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private View mv;

    private Button registerloginbutton;
    private Button loginbutton;

    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private EditText passwordInput;

    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
    }

    public static void newInstance() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mv = inflater.inflate(R.layout.fragment_login, container, false);

        usernameInput = mv.findViewById(R.id.Username);
        passwordInput = mv.findViewById(R.id.PasswordInput);

        loginbutton = mv.findViewById(R.id.LoginButton);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });
        registerloginbutton = mv.findViewById(R.id.Registerloginbutton);
        registerloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.clickedOnRegister();
            }
        });

        return mv;
    }

    /**
     * This is the function who sends the request to the database for an attempt to login a user.
     */
    private void login(){
        String gruppUrl = "https://projekt-app.herokuapp.com/user/login";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try{
            postObject.put("username", usernameInput.getText().toString());
            postObject.put("password", passwordInput.getText().toString());
        } catch (JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest postUserRequest = new JsonObjectRequest(Request.Method.POST, gruppUrl, postObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("access_token");
                            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP",Context.MODE_PRIVATE);
                            preferences.edit().putString("TOKEN",token).apply();
                            mListener.clickedOnLogin();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "token fuck", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(postUserRequest);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void clickedOnRegister();
        void clickedOnLogin();
        void closeKeyBoard();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
