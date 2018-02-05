package dev.jojo.agilus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.adapters.PinnedLocationAdapter;
import dev.jojo.agilus.objects.PinnedLocationObject;

public class PinnedLocations extends AppCompatActivity {

    @BindView(R.id.lvPinnedLocList) ListView lvPinLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_locations);

        setTitle("Pinned locations");

        ButterKnife.bind(this);

        setListView();

    }

    private void setListView(){

        ParseQuery<ParseObject> getPins = ParseQuery.getQuery(PinnedLocationObject.CLASS_NAME);

        getPins.whereEqualTo("pilot_obj_id", ParseUser.getCurrentUser().getObjectId());

        getPins.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(e==null){

                    if(objects.size() > 0){

                        List<PinnedLocationObject> locationObjects = new ArrayList<>();

                        int obsize = objects.size();

                        for(int i=0;i<obsize;i++){

                            PinnedLocationObject pObj = new PinnedLocationObject();

                            ParseObject cObj = objects.get(i);

                            pObj.TIMESTAMP = cObj.getString("pin_timestamp");
                            pObj.PIN_TYPE = cObj.getInt("pin_type");
                            pObj.LOC_GEOPOINT = cObj.getParseGeoPoint("pin_loc");

                            locationObjects.add(pObj);
                        }

                        PinnedLocationAdapter pAdapter = new PinnedLocationAdapter(locationObjects,PinnedLocations.this);
                        lvPinLoc.setAdapter(pAdapter);

                    }
                    else{
                        Log.d("PINS","No pins");
                    }
                }
                else{
                    Log.e("PARSE_ERROR",e.getMessage());
                }
            }
        });

        //lvPinLoc.setAdapter();
    }
}
