package com.p2p.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/03/15.
 */
public class Profile implements Serializable{
    public String id = "";
    public String device_id = "";
    public String name = "";
    public String photo_url = "";
    public ArrayList<Checkin> checkins = new ArrayList<Checkin>();

    public static class Checkin implements ClusterItem, Serializable {
        public String id = "";
        public String name = "";
        public String profile_id = "";
        public String place_id = "";
        public String checkedin_at = "";
        public String device_id = "";
        public String lat = "";
        public String lng = "";
        public String type = "";
        public String photo_url = "";

        @Override
        public LatLng getPosition() {
            return new LatLng(Double.valueOf(lat), Double.valueOf(lng));
        }

    }
}
