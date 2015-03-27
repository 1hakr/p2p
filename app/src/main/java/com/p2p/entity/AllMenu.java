package com.p2p.entity;


import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class AllMenu {
	public int id = 0;
    public ArrayList<MenuItem> all_dishes = new ArrayList<MenuItem>();

	public static class MenuItem implements Serializable {
		public int id = 0;
		public String name = "";
        public String category = "";
        public String calories = "";
        public String fat = "";
        public String ingredients = ""; //comma seperated
        public ArrayList<String> image_urls = new ArrayList<String>();

        public MenuItem (String name){
            this.name = name;
        }
	}
}