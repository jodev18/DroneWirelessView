package dev.jojo.agilus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_admin);

        initUI();

    }

    private void initUI(){
        final String[] colors = getResources().getStringArray(R.array.vertical_ntb);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                switch(position){
                    case 0:

                        final View view = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.pager_accounts, null, false);

                        //final TextView txtPage = (TextView) view.findViewById(R.id.id);
                        //txtPage.setText(String.format("Page #%d", position));

                        container.addView(view);
                        return view;

                    case 1:
                        final View viewMap = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.pager_map, null, false);

                        //final TextView txtPage = (TextView) view.findViewById(R.id.id);
                        //txtPage.setText(String.format("Page #%d", position));

                        container.addView(viewMap);
                        return viewMap;


                    case 2:
                        final View viewInfo = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.pager_account_info, null, false);

                        //final TextView txtPage = (TextView) view.findViewById(R.id.id);
                        //txtPage.setText(String.format("Page #%d", position));

                        container.addView(viewInfo);
                        return viewInfo;


                        default:
                            return null;
                }
            }
        });

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_vertical);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(android.R.drawable.ic_menu_myplaces),
                        Color.parseColor(colors[0]))
                        .title("ic_first")
                        .selectedIcon(getResources().getDrawable(android.R.drawable.ic_menu_myplaces))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(android.R.drawable.ic_dialog_map),
                        Color.parseColor(colors[1]))
                        .selectedIcon(getResources().getDrawable(android.R.drawable.ic_dialog_map))
                        .badgeTitle("HEY")
                        .title("ic_second")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(android.R.drawable.ic_menu_info_details),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                        .title("ic_third")
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setIsBadged(true);
        navigationTabBar.setViewPager(viewPager, 0);
    }


    @Override
    public void onBackPressed(){
        AlertDialog.Builder bb = new AlertDialog.Builder(AdminActivity.this);

        bb.setTitle("Exit?");
        bb.setMessage("Are you sure you wanna quit?");

        bb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        bb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        bb.create().show();

    }
}
