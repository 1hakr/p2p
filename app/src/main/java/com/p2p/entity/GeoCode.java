package com.p2p.entity;

import java.util.ArrayList;

public class GeoCode {

	public ArrayList<Result> results = new ArrayList<Result>();
	public String status = "";
	
	public static class Result{
		public ArrayList<Component> address_components = new ArrayList<Component>();
		
		public static class Component{
			public String long_name = "";
			public ArrayList<String> types = new ArrayList<String>();
		}
	}
}
