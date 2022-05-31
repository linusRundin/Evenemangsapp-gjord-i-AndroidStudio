package com.example.projekt_event_app.event;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projekt_event_app.R;
import com.example.projekt_event_app.firebase.Upload;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;


/**
 * Fragment used when creating a new event.
 */
public class EventCreatorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mv;

    private TextView dateview;
    private TextView timeview;
    private TextView placeview;
    private Calendar mCurrentDate;
    private int day, month, year, hour, minute;
    private Button photobutton;
    private ImageView eventimage;
    private Uri imageuri;
    private Button createeventbutton;
    private TextInputEditText titleview;
    private TextInputEditText description;

    private StorageReference mStorageRef;
    private DatabaseReference mDatebaseRef;

    private StorageTask mUploadTask;
    private String uniqueID;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int PICK_IMAGE_REQUEST = 1;

    private EventCreatorFragment.OnFragmentInteractionListener mListener;

    private static final String[] PERMISSION_EX_STORE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String[] PERMISSION_FINE_LOC = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };


    public EventCreatorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventCreatorFragment newInstance(String param1, String param2) {
        EventCreatorFragment fragment = new EventCreatorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        mv = inflater.inflate(R.layout.fragment_event_creator, container, false);
        mListener.selectIconMess();
        dateview = mv.findViewById(R.id.DateView);
        timeview = mv.findViewById(R.id.TimeView);
        placeview = mv.findViewById(R.id.PlaceView);
        photobutton = mv.findViewById(R.id.PhotoButton);
        eventimage = mv.findViewById(R.id.imageView);
        createeventbutton = mv.findViewById(R.id.CreateEventButton);
        titleview = mv.findViewById(R.id.Titel);
        description = mv.findViewById(R.id.Diss);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        mCurrentDate = Calendar.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatebaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month = mCurrentDate.get(Calendar.MONTH);
        year = mCurrentDate.get(Calendar.YEAR);

        hour = mCurrentDate.get(Calendar.HOUR);
        minute = mCurrentDate.get(Calendar.MINUTE);

        month = month +1;

        dateview.setText(day+"/"+month+"/"+year);
        timeview.setText(hour+"/"+minute);

        dateview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateview.setText(dayOfMonth+"/"+month+"/"+year);
                    }
                },year, month, day);
                datePickerDialog.show();
            }
        });

        timeview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        timeview.setText(hourOfDay+":"+minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(getContext()));
                timePickerDialog.show();
            }
        });

        placeview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {

                            Geocoder geocoder;
                            List<Address> addresses = null;
                            geocoder = new Geocoder(getContext(), Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(task.getResult().getLatitude(), task.getResult().getLongitude(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            placeview.setText(addresses.get(0).getAddressLine(0));

                        }
                    });


                }else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSION_FINE_LOC, 1000);
                }
            }
        });

        photobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    fileChooser();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSION_EX_STORE, 1000);
                }

            }
        });

        eventimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    fileChooser();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSION_EX_STORE, 1000);
                }
            }
        });

        createeventbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress())  {
                    Toast.makeText(getContext(), "Upload inProgress", Toast.LENGTH_SHORT).show();
                } else {
                    postEvent();
                    uploadFile();
                }

            }
        });

        return mv;
    }

    /**
     * Send information to server to create event.
     */
    private void postEvent(){
        String Url = "https://projekt-app.herokuapp.com/make/event";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject postObject = new JSONObject();
        uniqueID = UUID.randomUUID().toString();
        try{
            postObject.put("image_id", uniqueID);
            postObject.put("name", titleview.getText().toString());
            postObject.put("start_date", dateview.getText().toString());
            postObject.put("time", timeview.getText().toString());
            postObject.put("description", description.getText().toString());
            postObject.put("location", placeview.getText().toString());

        } catch (JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest postEventRequest = new JsonObjectRequest(Request.Method.POST, Url, postObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"det gick fint", Toast.LENGTH_LONG).show();
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
        queue.add(postEventRequest);
    }

    /**
     * Make an intent to be able to choose image from files.
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

        if ( requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageuri = data.getData();

            Picasso.with(getContext()).load(imageuri).resize(600, 200).centerCrop().into(eventimage);

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void sendToBlacklist();
        void selectIconMess();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EventCreatorFragment.OnFragmentInteractionListener) {
            mListener = (EventCreatorFragment.OnFragmentInteractionListener) context;
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


    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    /**
     * Used to upload event image to firebase.
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

                        }
                    });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
