package dev.jojo.agilus.adapters;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dev.jojo.agilus.R;
import dev.jojo.agilus.objects.LegendObject;

/**
 * Created by myxroft on 01/02/2018.
 */

public class LegendsAdapter extends BaseAdapter {
    public List<LegendObject> legendObjectList;
    public Activity act;

    public LegendsAdapter(List<LegendObject> legendObjects, Activity activity){
        this.act = activity;
        this.legendObjectList = legendObjects;
    }
    @Override
    public int getCount() {
        return legendObjectList.size();
    }

    @Override
    public LegendObject getItem(int position) {
        return legendObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){

            convertView = this.act.getLayoutInflater().inflate(R.layout.list_item_legend,null);

        }

        LegendObject lObj = this.legendObjectList.get(position);

        ImageView imLegend = convertView.findViewById(R.id.imgLegendColor);
        TextView tvLName = convertView.findViewById(R.id.tvLegendName);

        tvLName.setText(lObj.CATEGORY);


        imLegend.setImageDrawable(this.act.getDrawable(lObj.COLOR));

        return convertView;
    }
}
