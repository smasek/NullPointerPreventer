package de.smasek.nmnp.transformator;

import java.util.ArrayList;

public class MappingNeuMain {

	public static void main(String[] args) throws Exception {
		Bean bean = new Bean();
		Adress adress = new Adress();
		adress.setStreet("zuhase");
		bean.setAdress(adress);
		bean.setName("Heinz");
		SampleTO r = new SampleTO();
		bean.setFriend(bean);
		adress.setAdd(new ArrayList<String>());
		adress.getAdd().add("SASCHA");
		MappingNeu m = NullPointerExceptionPreventer.getInstance();
		r = new SampleTO();
		m.map(bean, r);

		System.out.println(r);
	}

	public static void main1(String[] args) throws Exception {
		Bean bean = new Bean();
		Adress adress = new Adress();
		adress.setStreet("zuhase");
		bean.setAdress(adress);
		bean.setName("Heinz");
		SampleTO r = new SampleTO();
		bean.setFriend(bean);
		adress.setAdd(new ArrayList<String>());
		adress.getAdd().add("SASCHA");
		MappingNeu m = NullPointerExceptionPreventer.getInstance();
		r = new SampleTO();
		m.map(bean, r);

		System.out.println(r);
	}
}
