package dev.jojo.agilus.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import dev.jojo.agilus.R;
import dev.jojo.agilus.objects.PilotMenuItem;

/**
 * Created by MAC on 17/12/2017.
 */

public class PilotMenuAdapter extends BaseAdapter {

    private List<PilotMenuItem> menuItemList;
    private Activity act;

    public PilotMenuAdapter(List<PilotMenuItem> menuItems,Activity activity){

        this.menuItemList = menuItems;
        this.act = activity;

    }

    @Override
    public int getCount() {
        return this.menuItemList.size();
    }

    @Override
    public PilotMenuItem getItem(int position) {

        return this.menuItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView != null)
            convertView = this.act.getLayoutInflater().inflate(R.layout.list_item_pilot_menu,null);

        PilotMenuItem pItem = this.menuItemList.get(position);




        return convertView;

    }
}
