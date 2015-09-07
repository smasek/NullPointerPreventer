package de.smasek.nmnp.transformator;

import java.util.Date;

public class Bean {

	private String name;
	
	private Adress adress;
	
	private Bean friend;
	
	private Date date;
	
	public Date getDate() {
		return date;
	}
	
	
	public void setFriend(Bean friend) {
		this.friend = friend;
	}
	public Bean getFriend() {
		return friend;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Adress getAdress() {
		return adress;
	}

	public void setAdress(Adress adress) {
		this.adress = adress;
	}
	
	
}
