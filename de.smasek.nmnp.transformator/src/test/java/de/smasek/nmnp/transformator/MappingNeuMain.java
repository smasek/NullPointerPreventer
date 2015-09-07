package de.smasek.nmnp.transformator;

public class MappingNeuMain {

	public static void main(String[] args) throws Exception {
		Bean bean = new Bean();
		Adress adress = new Adress();
		adress.setStreet("zuhase");
		bean.setAdress(adress);
		bean.setName("Heinz");
		SampleTO r =new SampleTO();
		bean.setFriend(bean);
		MappingNeu m = NullPointerExceptionPreventer.getInstance();
		 r =new SampleTO();
		m.map(bean, r);
		
		System.out.println(r);
	}
}
