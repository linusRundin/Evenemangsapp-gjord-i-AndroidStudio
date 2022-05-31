package com.example.projekt_event_app.register_login;

import android.content.Context;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.fragment.app.Fragment;


/**
 * This fragment is used to register a user.
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private View mv;
    private OnFragmentInteractionListener mListener;

    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;


    private Button registerButton;

    public RegisterFragment() {
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
        mv = inflater.inflate(R.layout.fragment_register, container, false);
        registerButton = mv.findViewById(R.id.Registerbutton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameInput = mv.findViewById(R.id.Username);
                emailInput = mv.findViewById(R.id.EmailInput);
                passwordInput = mv.findViewById(R.id.PasswordInput);
                confirmInput = mv.findViewById(R.id.ConfirmInput);

                if (emailVerifier(emailInput.getText().toString())) {
                    if (passwordVsConfirm(passwordInput.getText().toString(), confirmInput.getText().toString())) {
                        postData();
                        Toast.makeText(getActivity(), "Welcome my dude", Toast.LENGTH_SHORT).show();
                        mListener.swapBack();
                    }else{
                        Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(),"invalid email, try again", Toast.LENGTH_SHORT).show();
                }

            }
        });




        return mv;
    }


    /**
     * This function is used to confirm whether or not two passwords are equal.
     * @param password is the first password
     * @param confirm is the second attempt of first password
     * @return true or false depending on the two strings.
     */
    private boolean passwordVsConfirm(String password, String confirm){
        return password.equals(confirm);
    }

    /**
     * This function is used to confirm whether or not a string is the same format as a email.
     * @param email the string that is suppose to be a email.
     * @return true or false depending on the email.
     */
    private boolean emailVerifier(String email){
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * This function sends a request to attempt to register a user.
     */
    public void postData(){
        String gruppUrl = "https://projekt-app.herokuapp.com/user";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try{
            postObject.put("username", usernameInput.getText().toString());
            postObject.put("password", passwordInput.getText().toString());
            postObject.put("email", emailInput.getText().toString());
            postObject.put("profile_picture_id", "mormors_mat");
        } catch (JSONException e){
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
        });
        queue.add(postUserRequest);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void swapBack();

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegisterFragment.OnFragmentInteractionListener) {
            mListener = (RegisterFragment.OnFragmentInteractionListener) context;
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
