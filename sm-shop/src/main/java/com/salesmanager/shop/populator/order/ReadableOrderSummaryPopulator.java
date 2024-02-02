package com.salesmanager.shop.populator.order;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.utils.AbstractDataPopulator;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.OrderTotal;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.order.ReadableOrderTotalSummary;
import com.salesmanager.shop.model.order.total.ReadableOrderTotal;
import com.salesmanager.shop.utils.LabelUtils;

public class ReadableOrderSummaryPopulator extends AbstractDataPopulator<OrderTotalSummary, ReadableOrderTotalSummary> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReadableOrderSummaryPopulator.class);
	
	private PricingService pricingService;
	
	private LabelUtils messages;
	


	@Override
	public ReadableOrderTotalSummary populate(OrderTotalSummary source, ReadableOrderTotalSummary target,
			MerchantStore store, Language language) throws ConversionException {
		
		Validate.notNull(pricingService,"PricingService must be set");
		Validate.notNull(messages,"LabelUtils must be set");
		
		if(target==null) {
			target = new ReadableOrderTotalSummary();
		}
		
		try {
		
			if(source.getSubTotal() != null) {
				target.setSubTotal(pricingService.getDisplayAmount(source.getSubTotal(), store));
			}
			if(source.getTaxTotal()!=null) {
				target.setTaxTotal(pricingService.getDisplayAmount(source.getTaxTotal(), store));
			}
			if(source.getTotal() != null) {
				target.setTotal(pricingService.getDisplayAmount(source.getTotal(), store));
			}
			
			if(!CollectionUtils.isEmpty(source.getTotals())) {
				ReadableOrderTotalPopulator orderTotalPopulator = new ReadableOrderTotalPopulator();
				orderTotalPopulator.setMessages(messages);
				orderTotalPopulator.setPricingService(pricingService);
				for(OrderTotal orderTotal : source.getTotals()) {

					/**********************************
					 * CAST-Finding START #1 (2024-02-02 12:31:03.685053):
					 * TITLE: Avoid instantiations inside loops
					 * DESCRIPTION: Object instantiation uses memory allocation, that is a greedy operation. Doing an instantiation at each iteration could really hamper the performances and increase resource usage.  If the instantiated object is local to the loop, there is absolutely no need to instantiate it at each iteration : create it once outside the loop, and just change its value at each iteration. If the object is immutable, create if possible a mutable class. If the aim is to create a consolidated data structure, then, unless the need is to release the data case by case, it could be better to make a single global allocation outside the loop, and fill it with data inside the loop.
					 * STATUS: WITHDRAWN
					 * CAST-Finding END #1
					 **********************************/

					// Instantiation inside loop is valid here
					ReadableOrderTotal t = new ReadableOrderTotal();
					orderTotalPopulator.populate(orderTotal, t, store, language);
					target.getTotals().add(t);
				}
			}
			
		
		} catch(Exception e) {
			LOGGER.error("Error during amount formatting " + e.getMessage());
			throw new ConversionException(e);
		}
		
		return target;
		
	}

	@Override
	protected ReadableOrderTotalSummary createTarget() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public PricingService getPricingService() {
		return pricingService;
	}

	public void setPricingService(PricingService pricingService) {
		this.pricingService = pricingService;
	}
	
	public LabelUtils getMessages() {
		return messages;
	}

	public void setMessages(LabelUtils messages) {
		this.messages = messages;
	}

}
