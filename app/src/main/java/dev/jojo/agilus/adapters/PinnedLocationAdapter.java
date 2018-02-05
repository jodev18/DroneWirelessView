package dev.jojo.agilus.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dev.jojo.agilus.R;
import dev.jojo.agilus.objects.PinnedLocationObject;

/**
 * Created by myxroft on 03/02/2018.
 */

public class PinnedLocationAdapter extends BaseAdapter {

    private List<PinnedLocationObject> pList;
    private Activity act;

    private final int PIN_TYPE_HEALTHY = 42;
    private final int PIN_TYPE_MINOR_INJURY = 41;
    private final int PIN_TYPE_MAJOR_INJURY = 40;
    private final int PIN_TYPE_CASUALTY = 39;
    private final int PIN_TYPE_NEED_SUPP = 38;
    private final int PIN_TYPE_TRAPPED = 37;
    private final int PIN_TYPE_RESPONDED = 36;

    public PinnedLocationAdapter(List<PinnedLocationObject> list, Activity activity){

        this.pList = list;
        this.act = activity;
    }


    @Override
    public int getCount() {
        return pList.size();
    }

    @Override
    public PinnedLocationObject getItem(int position) {
        return this.pList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView==null){
            convertView = this.act.getLayoutInflater().inflate(R.layout.list_item_pinned,null);
        }

        TextView tvTimeStamp = convertView.findViewById(R.id.tvTimeStamp);
        TextView tvCoords = convertView.findViewById(R.id.tvPinnedLocCoord);

        ImageView imgPinType = convertView.findViewById(R.id.imgListPinType);

        PinnedLocationObject pObj = pList.get(position);

        tvTimeStamp.setText(pObj.TIMESTAMP);
        tvCoords.setText(Double.valueOf(pObj.LOC_GEOPOINT.getLatitude()).toString()
                            + "," + Double.valueOf(pObj.LOC_GEOPOINT.getLongitude()).toString());

        Integer pinType = pObj.PIN_TYPE;

        switch (pinType){
            case PIN_TYPE_HEALTHY:

                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_person_pin_circle_green));
                break;
            case PIN_TYPE_MINOR_INJURY:
                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_person_pin_circle_yellow));
                break;

            case PIN_TYPE_CASUALTY:
                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_person_pin_circle_black));
                break;
            case PIN_TYPE_MAJOR_INJURY:
                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_person_pin_circle_red));
                break;
            case PIN_TYPE_NEED_SUPP:
                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_person_pin_circle_brown));
                break;

            case PIN_TYPE_TRAPPED:
                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_person_pin_circle_green));
                break;

            case PIN_TYPE_RESPONDED:
                imgPinType.setImageDrawable(this.act.getDrawable(R.drawable.ic_responded));
                break;
                default:
        }




        return convertView;
    }
}
