package com.example.projekt_event_app.register_login;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
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
import com.example.projekt_event_app.event.Event;
import com.example.projekt_event_app.event.EventAdapter;
import com.example.projekt_event_app.event.Events;
import com.example.projekt_event_app.firebase.Upload;
import com.example.projekt_event_app.user.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;


/**
 * This fragment is used to show the current logged in user.
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;

    private View mv;

    private ImageView profilPic;

    private ProfileFragment.OnFragmentInteractionListener mListener;

    private StorageReference storageReference;

    private Uri imageuri;

    public User user;

    private StorageReference mStorageRef;
    private DatabaseReference mDatebaseRef;

    private StorageTask mUploadTask;

    private String uniqueID;
    
    private Events eventList;

    private Button attendingsProfileButton;

    private Button updateButton;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    private static final String[] PERMISSION_EX_STORE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String[] PERMISSION_CAMERA = {
            Manifest.permission.CAMERA
    };


    private TextView eventCount;
    private TextView followersCount;
    private TextView followingCount;

    private TextView followers;
    private TextView following;
    private TextView events;




    public ProfileFragment() {
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

        mv = inflater.inflate(R.layout.fragment_profile, container, false);
        mListener.selectIconProfile();
        getUser();
        followersCount = mv.findViewById(R.id.followersCount);
        followers = mv.findViewById(R.id.profileFollowers);
        following = mv.findViewById(R.id.profileFollowing);

        followingCount = mv.findViewById(R.id.followingCount);
        events = mv.findViewById(R.id.profileEvents);
        eventCount = mv.findViewById(R.id.eventsCount);

        eventList = new Events();





        Button logoutButton = mv.findViewById(R.id.LogoutButton);





        profilPic = mv.findViewById(R.id.profile_pic);

        updateButton = mv.findViewById(R.id.updateButton);
        updateButton.setVisibility(View.INVISIBLE);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatebaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        uniqueID = UUID.randomUUID().toString();


        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", getContext().MODE_PRIVATE);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.sendToBlacklist();
            }
        });

        profilPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
                updateProfilePicid();
                updateButton.setVisibility(View.INVISIBLE);
            }
        });















        return mv;
    }

    private void getAmount(final String url, User user){

        String Url = "https://projekt-app.herokuapp.com/"+url +"/"+ user.getId();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest postEventRequest = new JsonObjectRequest(Request.Method.GET, Url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        Amount amount = gson.fromJson(response.toString(), Amount.class);
                        if (url == "events/created"){
                            eventCount.setText(amount.getAmount());
                        } else if (url == "followers") {
                            followersCount.setText(amount.getAmount());
                        } else if (url == "following") {
                            followingCount.setText(amount.getAmount());
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


    public interface OnFragmentInteractionListener {
        void sendToBlacklist();

        void selectIconProfile();

        void goToFriendList(String id);

        void goToEvents(String id);

        void getDetail(int position, Events events);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.OnFragmentInteractionListener) {
            mListener = (ProfileFragment.OnFragmentInteractionListener) context;
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

    /**
     * This function is used to shw a menu with two different options.
     * @param v is which view this is used on.
     */
    private void showPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                if (ContextCompat.checkSelfPermission(getContext(), PERMISSION_CAMERA[0]) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSION_CAMERA, 1000);
                }
                return true;
            case R.id.item2:
                if (ContextCompat.checkSelfPermission(getContext(), PERMISSION_EX_STORE[0]) == PackageManager.PERMISSION_GRANTED) {
                    fileChooser();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSION_EX_STORE, 2000);
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * This function is used to get information about the current logged in user.
     */
    private void getUser() {
        String gruppUrl = "https://projekt-app.herokuapp.com/get/profile";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest postUserRequest = new JsonArrayRequest(Request.Method.GET, gruppUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        try {
                            user = gson.fromJson(response.getJSONObject(0).toString(), User.class);
                            getAttendings(user.getId());
                            getAmount("events/created", user);
                            getAmount("followers", user);
                            getAmount("following", user);
                            storageReference = FirebaseStorage.getInstance().getReference("uploads");
                            StorageReference ref = storageReference.child(user.getProfile_picture_id() + ".jpg");

                            following.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mListener.goToFriendList(user.getId());
                                }
                            });

                            followers.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });


                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(getContext()).load(uri).fit().centerCrop().into(profilPic);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

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
                String retrivedToken = preferences.getString("TOKEN", null);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }

        };

        queue.add(postUserRequest);


    }

    /**
     * This function uses the current user to get which events the user is attending.
     * @param userId is the current users id.
     */
    private void getAttendings(String userId){
        final ListView attendingList = mv.findViewById(R.id.eventsAttending);

        String gruppUrl = "https://projekt-app.herokuapp.com/my/attendings/"+userId;
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
                                attendingList.setAdapter(newAdapter);
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

        attendingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mListener.getDetail(position, eventList);
            }
        });
    }

    /**
     * This function is used to get access to the camera.
     */
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageuri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    /**
     * This function is used to choose a picture from storage.
     */
    private void fileChooser(){
        Intent intent  = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageuri = data.getData();

            Picasso.with(getContext()).load(imageuri).fit().centerCrop().into(profilPic);
            updateButton.setVisibility(View.VISIBLE);

        }

        else if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            //set the image captured to our ImageView
            Picasso.with(getContext()).load(imageuri).fit().centerCrop().into(profilPic);
            updateButton.setVisibility(View.VISIBLE);
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    /**
     * This function uploads the current picture in imageuri to the Firebase database.
     */
    private void uploadFile() {
        if (imageuri != null) {
            StorageReference fileReference = mStorageRef.child(uniqueID
                    + "." + getFileExtension(imageuri));

            mUploadTask = fileReference.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                }
                            }, 500);

                            Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                            Upload upload = new Upload(uniqueID,
                                    taskSnapshot.getUploadSessionUri().toString());
                            String uploadId = mDatebaseRef.push().getKey();
                            mDatebaseRef.child(uploadId).setValue(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            if (progress == 100.0) {
                                updateProfilePicid();
                            }

                        }
                    });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This function updates the new picture id to the database.
     */
    private void updateProfilePicid() {
        String gruppUrl = "https://projekt-app.herokuapp.com/change/profile_picture_id";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        try{
            postObject.put("profile_picture_id", uniqueID);
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


}
