package com.iot.device.management.iot.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iot.device.management.iot.R;
import com.iot.device.management.iot.device.DeviceProperties;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by sneez on 23.04.16.
 */
public class DeviceAdapter extends ArrayAdapter<DeviceProperties> {
    private static class ViewHolder {
        public ImageView ivCover;
        public TextView tvTitle;
        public TextView tvDescription;
        public View vNotificator;
    }

    public DeviceAdapter(Context context, ArrayList<DeviceProperties> deviceProperties) {
        super(context, 0, deviceProperties);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DeviceProperties device = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_device, parent, false);
            viewHolder.ivCover = (ImageView) convertView.findViewById(R.id.ivDevice);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
//            viewHolder.vNotificator = (View) convertView.findViewById(R.id.vNotificator);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(device.getTitle());
        viewHolder.tvDescription.setText(device.getIpAddress());
//        viewHolder.vNotificator.setBackgroundColor(Color.RED);
        convertView.setEnabled(false);
        Picasso.with(getContext()).load(R.mipmap.device).into(viewHolder.ivCover);
        return convertView;
    }
}