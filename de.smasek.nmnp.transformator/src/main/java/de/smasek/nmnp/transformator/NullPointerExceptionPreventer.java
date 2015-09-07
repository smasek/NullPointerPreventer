package de.smasek.nmnp.transformator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class NullPointerExceptionPreventer {

	private static final class Loader extends CacheLoader<String, Class<?>> {
		@Override
		public Class<?> load(String key) throws Exception {
			return new Transformator(key).transform();
		}
	}

	private static final LoadingCache<String, Class<?>> mappings = 
			CacheBuilder.newBuilder().build(new Loader());
	
	@SuppressWarnings("all")
	public static <T> T getInstance(T... param) throws Exception {
		return (T) mappings.get(param.getClass().getComponentType().getName()).newInstance();
	}
}