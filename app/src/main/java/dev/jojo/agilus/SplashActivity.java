package dev.jojo.agilus;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {

    private Handler h;

    private AlertDialog alertInfoDialog;

    private Disposable netDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        h = new Handler(this.getMainLooper());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);

        Boolean hasAllowed = sp.getBoolean("has_permission",false);

        if(!hasAllowed){

            Dexter.withActivity(this)
                    .withPermissions(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ).withListener(new MultiplePermissionsListener() {

                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);

                    SharedPreferences.Editor e = sp.edit();

                    e.putBoolean("has_permission",true);
                    e.commit();

                    initNetworkListener();
                }
                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                }
            }).check();

        }
        else{
            initNetworkListener();
        }
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
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent goLogin = new Intent(SplashActivity.this,LoginActivity.class);
                                    startActivity(goLogin);
                                    finish();
                                }
                            },1800);
                           // Snackbar.make(bLogin,"Device connected.",Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder dc = new AlertDialog.Builder(SplashActivity.this);
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
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }

        super.onDestroy();
    }
}
