package com.p2p.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/01/15.
 */
public class Places {

    public Place now = new Place();
    public Place can_wait = new Place();
    public Place royal_pee = new Place();

    public class Place implements ClusterItem, Serializable {
        public String id = "";
        public String name = "";
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
