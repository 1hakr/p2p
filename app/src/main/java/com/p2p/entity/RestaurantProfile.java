package com.p2p.entity;


import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class RestaurantProfile implements Serializable {
	public int id = 0;
    public ArrayList<Restaurant> places = new ArrayList<Restaurant>();

    public static class Restaurant implements Serializable {

        public Restaurant(){

        }

        public String name = "";
        public String image_url = "";
    }
}