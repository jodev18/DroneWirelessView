package dev.jojo.agilus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.objects.AccountObject;

public class AccountsActivity extends AppCompatActivity {

    @BindView(R.id.lvAccountsList) ListView lvPilotList;
    @BindView(R.id.tvStatusNone) TextView tvStat;
    ProgressDialog prg;

    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prg = new ProgressDialog(AccountsActivity.this);

        ButterKnife.bind(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),NewPilotAccountActivity.class));
            }
        });

        h = new Handler(this.getMainLooper());

        loadAssocPilots();
    }

    private void loadAssocPilots() {

        prg.setMessage("Loading accounts");
        prg.show();

        ParseQuery<ParseObject> pq = ParseQuery.getQuery("PilotAccounts");

        pq.whereEqualTo("PilotAdmin", ParseUser.getCurrentUser().getObjectId());

        //Toast.makeText(this, "OBJECT ID--" + ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();

        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                prg.dismiss();

                if (e == null) {

                    if (objects.size() > 0) {
                        List<AccountObject> accountObjects = new ArrayList<>();

                        int accs = objects.size();

                        for (int i = 0; i < accs; i++) {
                            AccountObject accObj
                                    = new AccountObject(objects.get(i).getString("PilotUserName"),
                                    objects.get(i).getString("PilotPassword"),
                                    objects.get(i).getString("PilotName"),
                                    objects.get(i).getString("PilotDrone"),
                                    objects.get(i).getObjectId());

                            accountObjects.add(accObj);
                        }

                        AccountsAdapter accountsAdapter = new AccountsAdapter(AccountsActivity.this, accountObjects);
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
    protected void onResume(){
        super.onResume();

        Toast.makeText(this, ParseUser.getCurrentUser().getUsername(), Toast.LENGTH_SHORT).show();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAssocPilots();
            }
        },1000);
    }


    @Override
    public void onBackPressed(){

        ParseUser.logOutInBackground();
        super.onBackPressed();

    }

}
