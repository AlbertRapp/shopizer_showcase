package com.salesmanager.core.business.utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.merchant.MerchantStore;

@Component("cache")
public class CacheUtils {
	
	
    @Inject
    @Qualifier("serviceCache")
    private Cache cache;
	
	
	public final static String REFERENCE_CACHE = "REF";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtils.class);

	private final static String KEY_DELIMITER = "_";
	


	public void putInCache(Object object, String keyName) throws Exception {

		cache.put(keyName, object);
		
	}
	

	public Object getFromCache(String keyName) throws Exception {

		ValueWrapper vw = cache.get(keyName);
		if(vw!=null) {
			return vw.get();
		}
		
		return null;
		
	}
	
	public List<String> getCacheKeys(MerchantStore store) throws Exception {
		
		  net.sf.ehcache.Cache cacheImpl = (net.sf.ehcache.Cache) cache.getNativeCache();
		  List<String> returnKeys = new ArrayList<String>();

		// QECI Fix: Use StringBuilder
		StringBuilder logMsg = new StringBuilder();
		
		  for (Object key: cacheImpl.getKeys()) {
		    
			  
				try {
					String sKey = (String)key;
					
					// a key should be <storeId>_<rest of the key>
					int delimiterPosition = sKey.indexOf(KEY_DELIMITER);
					
					if(delimiterPosition>0 && Character.isDigit(sKey.charAt(0))) {
					
						String keyRemaining = sKey.substring(delimiterPosition+1);
						returnKeys.add(keyRemaining);
					
					}

				} catch (Exception e) {
					
					/**********************************
					 * CAST-Finding START #1 (2024-02-02 12:30:50.836571):
					 * TITLE: Avoid string concatenation in loops
					 * DESCRIPTION: Avoid string concatenation inside loops.  Since strings are immutable, concatenation is a greedy operation. This creates unnecessary temporary objects and results in quadratic rather than linear running time. In a loop, instead using concatenation, add each substring to a list and join the list after the loop terminates (or, write each substring to a byte buffer).
					 * STATUS: RESOLVED
					 * CAST-Finding END #1
					 **********************************/

					// QECI Fix: Use StringBuilder
					logMsg.append("key ").append(key).append(" cannot be converted to a String or parsed");
					LOGGER.equals(logMsg.toString());
					// LOGGER.equals("key " + key + " cannot be converted to a String or parsed");
				}  
		  }

		return returnKeys;
	}
	
	public void shutDownCache() throws Exception {
		
	}
	
	public void removeFromCache(String keyName) throws Exception {
		cache.evict(keyName);
	}
	
	public void removeAllFromCache(MerchantStore store) throws Exception {
		  net.sf.ehcache.Cache cacheImpl = (net.sf.ehcache.Cache) cache.getNativeCache();
		
		// QECI Fix: Use StringBuilder
		StringBuilder logMsg = new StringBuilder();
		
		  for (Object key: cacheImpl.getKeys()) {
				try {
					String sKey = (String)key;
					
					// a key should be <storeId>_<rest of the key>
					int delimiterPosition = sKey.indexOf(KEY_DELIMITER);
					
					if(delimiterPosition>0 && Character.isDigit(sKey.charAt(0))) {
					

						cache.evict(key);
					
					}

				} catch (Exception e) {

					/**********************************
					 * CAST-Finding START #2 (2024-02-02 12:30:50.836571):
					 * TITLE: Avoid string concatenation in loops
					 * DESCRIPTION: Avoid string concatenation inside loops.  Since strings are immutable, concatenation is a greedy operation. This creates unnecessary temporary objects and results in quadratic rather than linear running time. In a loop, instead using concatenation, add each substring to a list and join the list after the loop terminates (or, write each substring to a byte buffer).
					 * STATUS: RESOLVED
					 * CAST-Finding END #2
					 **********************************/

					// QECI Fix: Use StringBuilder
					logMsg.append("key ").append(key).append(" cannot be converted to a String or parsed");
					LOGGER.equals(logMsg.toString());
					// LOGGER.equals("key " + key + " cannot be converted to a String or parsed");
				}  
		  }
	}
	


}
