package com.example.projekt_event_app.register_login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.navigate.MainActivity;
import com.example.projekt_event_app.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This Activity handles LoginFragment and RegisterFragment.
 */
public class LoginActivity extends AppCompatActivity implements RegisterFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener {

    private RegisterFragment registerFragment;
    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerFragment = new RegisterFragment();
        loginFragment = new LoginFragment();
        checkToken();
    }

    /**
     * This function checks if the user is already logged in. Checking witch the token.
     */
    private void checkToken(){
        String gruppUrl = "https://projekt-app.herokuapp.com/active";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest postUserRequest = new JsonObjectRequest(Request.Method.POST, gruppUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getSupportFragmentManager().beginTransaction().replace(R.id.LoginFrame, loginFragment).commit();
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
    public void clickedOnRegister() {
        getSupportFragmentManager().beginTransaction().replace(R.id.LoginFrame,
                registerFragment).addToBackStack(null).commit();
    }

    @Override
    public void clickedOnLogin() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void swapBack() {
        getSupportFragmentManager().beginTransaction().replace(R.id.LoginFrame,
                loginFragment).addToBackStack(null).commit();
    }

    @Override
    public void closeKeyBoard() {
    }
}
