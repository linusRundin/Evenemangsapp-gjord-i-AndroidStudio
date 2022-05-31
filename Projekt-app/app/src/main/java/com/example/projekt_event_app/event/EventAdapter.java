package com.example.projekt_event_app.event;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projekt_event_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Used to create a costume listview containing event information.
 */
public class EventAdapter extends ArrayAdapter<Event> {

    private Context mContext;

    private ArrayList<Event> events;

    int mResource;

    private StorageReference storageReference;


    public EventAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Event> objects) {
        super(context, resource, objects);
        mContext = context;
        events = objects;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        Event event = events.get(position);
        String description = event.getHost();
        final String name = event.getName();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = convertView.findViewById(R.id.EventName);
        TextView tvDescrpition = convertView.findViewById(R.id.EventHost);
        final ImageView imageView = convertView.findViewById(R.id.EventImageView);

        tvName.setText(name);
        tvDescrpition.setText(description);
        System.out.println(event.getImageId());
        // Create a reference with an initial file path and name
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


        return convertView;
    }
}

