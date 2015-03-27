package com.p2p.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;

/**
 * Created by HaKr on 28/01/15.
 */
public class Places {

    public ArrayList<Place> results = new ArrayList<Place>();
    public String status = ""; //"OK"

    public class Place implements ClusterItem {
        public Geometry geometry = new Geometry();
        public String name = "";
        public String icon = "";
        public String vicinity = "";
        public ArrayList<String> types = new ArrayList<String>();
        public ArrayList<Photo> photos = new ArrayList<Photo>();

        @Override
        public LatLng getPosition() {
            return new LatLng(Double.valueOf(geometry.location.lat), Double.valueOf(geometry.location.lng));
        }

        public class Photo{
            public int height = 0;
            public String photo_reference = "";
        }

        public class Geometry {
            public Location location = new Location();

            public class Location {
                String lat = "";
                String lng = "";
            }
        }
    }
}
