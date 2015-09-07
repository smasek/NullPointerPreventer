package de.smasek.nmnp.transformator;

public class MappingNeu  {

	@PreventNullPointerException
	public void map(Bean bean, SampleTO target) {
		target.setName(bean.getName());
		target.setStreet(bean.getFriend().getAdress().getStreet());
		
	}

	
}
