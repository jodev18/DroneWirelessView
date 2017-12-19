package dev.jojo.agilus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import dev.jojo.agilus.objects.AccountObject;
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
                                getBaseContext()).inflate(R.layout.activity_accounts, null, false);
                        initAccountDisplay(view);

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

    private void initMapList(View pager){

    }

    private void initPilotList(View pager){


    }

    private void initAccountDisplay(View pager){

        Toolbar toolbar = (Toolbar)pager.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.agilus_graphic);

        FloatingActionButton fab = (FloatingActionButton)pager.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),NewPilotAccountActivity.class));
            }
        });

        final ListView lvPilotList = (ListView)pager.findViewById(R.id.lvAccountsList);
        final TextView tvStat = (TextView)pager.findViewById(R.id.tvStatusNone);

        ParseQuery<ParseObject> pq = ParseQuery.getQuery("PilotAccounts");

        pq.whereEqualTo("PilotAdmin", ParseUser.getCurrentUser().getObjectId());

        //Toast.makeText(this, "OBJECT ID--" + ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();

        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                //prg.dismiss();

                if (e == null) {

                    if (objects.size() > 0) {
                        List<AccountObject> accountObjects = new ArrayList<>();

                        int accs = objects.size();

                        for (int i = 0; i < accs; i++) {
                            AccountObject accObj
                                    = new AccountObject(objects.get(i).getString("PilotUser"),
                                    objects.get(i).getString("PilotPass"),
                                    objects.get(i).getString("PilotName"),
                                    objects.get(i).getString("PilotDrone"),
                                    objects.get(i).getObjectId());

                            accountObjects.add(accObj);
                        }

                        AccountsAdapter accountsAdapter = new AccountsAdapter(AdminActivity.this, accountObjects);
                        lvPilotList.setAdapter(accountsAdapter);
                        tvStat.setVisibility(TextView.GONE);
                    } else {
                        tvStat.setVisibility(TextView.VISIBLE);
                        tvStat.setText("No accounts yet.");
                    }
                } else {
                    tvStat.setText("There was a problem encountered while loading accounts.");
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        initUI();
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
