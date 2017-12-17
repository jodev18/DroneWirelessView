package dev.jojo.agilus;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class PilotActivity extends AppCompatActivity {


    private ProgressDialog prg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFAB();
        initSessionLocationList();
        initProgressDialog();

    }

    private void initProgressDialog(){
        prg = new ProgressDialog(PilotActivity.this);
    }


    private void initSessionLocationList(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("");
        query.whereEqualTo("","");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(e==null){

                }
                else{

                }
            }
        });


    }

    /**
     * Adds new flying session.
     */
    private void initFAB(){

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onBackPressed(){

        AlertDialog.Builder ab = new AlertDialog.Builder(PilotActivity.this);

        ab.setTitle("Log out");

        ab.setMessage("Pressing the back button will log you out." +
                " Are you sure you want to logout?");

        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {

                    }
                });
                PilotActivity.super.onBackPressed();
            }
        });

        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        ab.create().show();
    }

}
