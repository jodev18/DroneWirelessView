package dev.jojo.agilus.objects;

import com.parse.ParseGeoPoint;

/**
 * Created by MAC on 17/12/2017.
 */

public class PinnedLocationObject {

    public static final String CLASS_NAME = "PinLocObject";

    public String PILOT_ID;
    public ParseGeoPoint LOC_GEOPOINT;
    public String TIMESTAMP;
    public String PIN_TYPE;
    public String PILOT_NAME;
    public String DRONE_NAME;

}
