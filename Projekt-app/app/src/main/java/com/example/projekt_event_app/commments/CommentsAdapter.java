package com.example.projekt_event_app.commments;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projekt_event_app.R;
import com.example.projekt_event_app.commments.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Adapter used to show comments in the way we choose to represent them in a given listview.
 */
public class CommentsAdapter extends ArrayAdapter<Comment> {


    private Context mContext;


    private ArrayList<Comment> comments;

    int mResource;

    private StorageReference storageReference;


    public CommentsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Comment> objects) {
        super(context, resource, objects);
        mContext = context;
        comments = objects;
        mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Comment comment = comments.get(position);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        final ImageView imageView = convertView.findViewById(R.id.profile_comment_view);
        TextView tv = convertView.findViewById(R.id.contentView);
        TextView tvName = convertView.findViewById(R.id.commentName);

        tvName.setText(comment.getUser());

        tv.setText(comment.getContent());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference ref = storageReference.child(comment.getUser_pic_id() + ".jpg");


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
