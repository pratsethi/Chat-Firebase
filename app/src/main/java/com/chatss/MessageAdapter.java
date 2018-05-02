package com.chatss;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by PSethi on 28-Apr-18.
 */

public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    public MessageAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.msgImage);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.msgText);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.ownerName);

        ChatMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getMsg());
        }
        authorTextView.setText(message.getmUsername()+" :: ");

        return convertView;
    }
}
