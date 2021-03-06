package dev.jojo.agilus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import dev.jojo.agilus.objects.AccountObject;
import devlight.io.library.ntb.NavigationTabBar;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AdminActivity extends AppCompatActivity {

    private ProgressDialog prg;
    private Disposable netDisposable;
    private AlertDialog alertInfoDialog;
    private GoogleMap gMap;

    private Bundle currentSaved;

    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_admin);

        currentSaved = savedInstanceState;

        initUI(savedInstanceState);
        initNetworkListener();

        h = new Handler(this.getMainLooper());

    }
    /*
    private void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    } */

    public void animateMarker(final int position, final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker,final GoogleMap gMap,Bitmap markerBit) {


        final Marker marker = gMap.addMarker(new MarkerOptions()
                .position(startPosition)
                .title("Test")
                //.snippet(mCarParcelableListCurrentLation.get(position).mAddress)
                .icon(BitmapDescriptorFactory.fromBitmap(markerBit)));


        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private void initUI(final Bundle savedInstanceState){

        prg = new ProgressDialog(AdminActivity.this);

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

                        initMapList(viewMap,savedInstanceState);
                        //final TextView txtPage = (TextView) view.findViewById(R.id.id);
                        //txtPage.setText(String.format("Page #%d", position));

                        container.addView(viewMap);
                        return viewMap;


                    case 2:
                        final View viewInfo = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.pager_account_info, null, false);
                        initInfoDisplay(viewInfo);
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

    private void initMapList(View pager, Bundle savedInstanceState){

        final MapView mapView = (MapView)pager.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                AdminActivity.this.gMap = googleMap;

                int height = 100;
                int width = 100;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.agilus_graphic);
                Bitmap b=bitmapdraw.getBitmap();
                final Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                // Add a marker in Sydney and move the camera
                final LatLng sydney = new LatLng(-34, 151);

                //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,12.0f));

                // Add a marker in Sydney and move the camera
                final LatLng sydney2 = new LatLng(-34 + (Math.random()), 151 + (Math.random()));

                //animateMarkerToGB(new MarkerOptions()
                       // .position(sydney2)
                        //.title("Marker in Sydney: ")
                        //.icon(BitmapDescriptorFactory.fromBitmap(smallMarker)),);

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateMarker(0,sydney,sydney2,false,googleMap,smallMarker);
                    }
                },10000);


                mapView.onResume();
            }
        });

    }

    private void initInfoDisplay(View pager){

        TextView adminUsername = (TextView)pager.findViewById(R.id.tvUsername);
        TextView adminRole = (TextView)pager.findViewById(R.id.tvUserRole);

        //TODO Put the username and role here.
        adminUsername.setText(ParseUser.getCurrentUser().getUsername());
        adminRole.setText(ParseUser.getCurrentUser().getString("Role"));

        Button logout = (Button)pager.findViewById(R.id.btnLogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmLogoutDialog();
            }
        });

    }

    private void showConfirmLogoutDialog(){
        AlertDialog.Builder lgConfirm = new AlertDialog.Builder(AdminActivity.this);

        lgConfirm.setTitle("Log out");
        lgConfirm.setMessage("Are you sure you want to logout?");

        lgConfirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                prg.setMessage("Logging out...");
                prg.show();

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        
                        prg.dismiss();

                        if(e == null){
                            Toast.makeText(AdminActivity.this, 
                                    "Logged out successfully.", Toast.LENGTH_SHORT).show();
                            finish();

                            startActivity(new Intent().setClass(getApplicationContext(),LoginActivity.class));
                        }
                        else{
                            Toast.makeText(AdminActivity.this,
                                    "Failed to log out.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        lgConfirm.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        lgConfirm.create().show();
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


        lvPilotList.setEmptyView(tvStat);

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

    private void initNetworkListener(){

        netDisposable = ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Connectivity>() {
                    @Override public void accept(final Connectivity connectivity) {
                        // do something with connectivity
                        // you can call connectivity.getState();
                        // connectivity.getType(); or connectivity.toString();
                        if(connectivity.getState().equals(NetworkInfo.State.CONNECTED)){
                            if(alertInfoDialog != null){
                                if(alertInfoDialog.isShowing()){
                                    alertInfoDialog.dismiss();
                                }
                            }
                            Toast.makeText(AdminActivity.this, "Device connected.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder dc = new AlertDialog.Builder(AdminActivity.this);
                            dc.setTitle("Device offline");
                            dc.setMessage("The device is currently offline. Service is unavailable.");
                            dc.setCancelable(false);
                            alertInfoDialog = dc.create();
                            alertInfoDialog.show();
                        }
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        initUI(currentSaved);
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

    @Override
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }
        super.onDestroy();
    }
}
