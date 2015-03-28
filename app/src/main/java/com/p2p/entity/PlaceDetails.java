package com.p2p.entity;


import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class PlaceDetails implements Serializable {
    public String id = "";
    public String name = "";
    public String type = "";
    public String lat = "";
    public String lng = "";
    public String photo_url = "";
    public String score = "";
    public ArrayList<Profile.Checkin> checkins = new ArrayList<Profile.Checkin>();
    public ArrayList<Profile.Checkin> royals = new ArrayList<Profile.Checkin>();
}