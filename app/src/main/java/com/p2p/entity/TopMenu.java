package com.p2p.entity;


import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class TopMenu {
	public int id = 0;
    public ArrayList<TopItem> top_dishes = new ArrayList<TopItem>();

	public static class TopItem implements Serializable {
		public int id = 0;
		public String name = "";
        public String image_url = "";

        public TopItem (String name){
            this.name = name;
        }
	}
}