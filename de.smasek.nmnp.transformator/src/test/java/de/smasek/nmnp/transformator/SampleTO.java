package de.smasek.nmnp.transformator;

import java.util.Date;

public class SampleTO {

	private String street;
	private String name;
	private Date date;
	private String w;
	private String add;
	
	
	public void setDate(Date date, String w) {
		this.date = date;
		this.w = w;
	}
	
	
	public Date getDate() {
		return date;
	}
	
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	


	@Override
	public String toString() {
		return "SampleTO [street=" + street + ", name=" + name + ", date=" + date + ", w=" + w + ", add=" + add + "]";
	}


	public String getAdd() {
		return add;
	}


	public void setAdd(String add) {
		this.add = add;
	}

	
	
	
}
