package de.smasek.nmnp.transformator;

public class MappingNeu  {

	@PreventNullPointerException
	public void map(Bean bean, SampleTO target) {
		target.setName(bean.getName());
		target.setStreet(bean.getFriend().getAdress().getStreet());
		if (true)
		target.setAdd((bean.getAdress().getAdd().get(0).toLowerCase().trim().concat(" Masek")));
	}
	
	

	
}
