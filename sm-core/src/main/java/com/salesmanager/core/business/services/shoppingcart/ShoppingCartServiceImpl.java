package com.salesmanager.core.business.services.shoppingcart;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.shoppingcart.ShoppingCartAttributeRepository;
import com.salesmanager.core.business.repositories.shoppingcart.ShoppingCartItemRepository;
import com.salesmanager.core.business.repositories.shoppingcart.ShoppingCartRepository;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;
import com.salesmanager.core.model.common.UserContext;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.shipping.ShippingProduct;
import com.salesmanager.core.model.shoppingcart.ShoppingCart;
import com.salesmanager.core.model.shoppingcart.ShoppingCartAttributeItem;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("shoppingCartService")
public class ShoppingCartServiceImpl extends SalesManagerEntityServiceImpl<Long, ShoppingCart>
		implements ShoppingCartService {

	private ShoppingCartRepository shoppingCartRepository;

	@Inject
	private ProductService productService;

	@Inject
	private ShoppingCartItemRepository shoppingCartItemRepository;

	@Inject
	private ShoppingCartAttributeRepository shoppingCartAttributeItemRepository;

	@Inject
	private PricingService pricingService;

	@Inject
	private ProductAttributeService productAttributeService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

	@Inject
	public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository) {
		super(shoppingCartRepository);
		this.shoppingCartRepository = shoppingCartRepository;

	}

	/**
	 * Retrieve a {@link ShoppingCart} cart for a given customer
	 */
	@Override
	@Transactional
	public ShoppingCart getShoppingCart(final Customer customer, MerchantStore store) throws ServiceException {

		try {

			List<ShoppingCart> shoppingCarts = shoppingCartRepository.findByCustomer(customer.getId());

			// elect valid shopping cart
			List<ShoppingCart> validCart = shoppingCarts.stream().filter((cart) -> cart.getOrderId() == null)
					.collect(Collectors.toList());

			ShoppingCart shoppingCart = null;

			if (!CollectionUtils.isEmpty(validCart)) {
				shoppingCart = validCart.get(0);
				getPopulatedShoppingCart(shoppingCart, store);
				if (shoppingCart != null && shoppingCart.isObsolete()) {
					delete(shoppingCart);
					shoppingCart = null;
				}
			}

			return shoppingCart;

		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}

	/**
	 * Save or update a {@link ShoppingCart} for a given customer
	 */
	@Override
	public void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException {

		Validate.notNull(shoppingCart, "ShoppingCart must not be null");
		Validate.notNull(shoppingCart.getMerchantStore(), "ShoppingCart.merchantStore must not be null");

		try {
			UserContext userContext = UserContext.getCurrentInstance();
			if (userContext != null) {
				shoppingCart.setIpAddress(userContext.getIpAddress());
			}
		} catch (Exception s) {
			LOGGER.error("Cannot add ip address to shopping cart ", s);
		}

		if (shoppingCart.getId() == null || shoppingCart.getId() == 0) {
			super.create(shoppingCart);
		} else {
			super.update(shoppingCart);
		}

	}

	/**
	 * Get a {@link ShoppingCart} for a given id and MerchantStore. Will update the
	 * shopping cart prices and items based on the actual inventory. This method
	 * will remove the shopping cart if no items are attached.
	 */
	@Override
	@Transactional
	public ShoppingCart getById(final Long id, final MerchantStore store) throws ServiceException {

		try {
			ShoppingCart shoppingCart = shoppingCartRepository.findById(store.getId(), id);
			if (shoppingCart == null) {
				return null;
			}
			getPopulatedShoppingCart(shoppingCart, store);

			if (shoppingCart.isObsolete()) {
				delete(shoppingCart);
				return null;
			} else {
				return shoppingCart;
			}

		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}

	/**
	 * Get a {@link ShoppingCart} for a given id. Will update the shopping cart
	 * prices and items based on the actual inventory. This method will remove the
	 * shopping cart if no items are attached.
	 */
	/*
	 * @Override
	 * 
	 * @Transactional public ShoppingCart getById(final Long id, MerchantStore
	 * store) throws {
	 * 
	 * try { ShoppingCart shoppingCart = shoppingCartRepository.findOne(id); if
	 * (shoppingCart == null) { return null; }
	 * getPopulatedShoppingCart(shoppingCart);
	 * 
	 * if (shoppingCart.isObsolete()) { delete(shoppingCart); return null; } else {
	 * return shoppingCart; } } catch (Exception e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } return null;
	 * 
	 * }
	 */

	/**
	 * Get a {@link ShoppingCart} for a given code. Will update the shopping cart
	 * prices and items based on the actual inventory. This method will remove the
	 * shopping cart if no items are attached.
	 */
	@Override
	@Transactional
	public ShoppingCart getByCode(final String code, final MerchantStore store) throws ServiceException {

		try {
			ShoppingCart shoppingCart = shoppingCartRepository.findByCode(store.getId(), code);
			if (shoppingCart == null) {
				return null;
			}
			getPopulatedShoppingCart(shoppingCart, store);

			if (shoppingCart.isObsolete()) {
				delete(shoppingCart);
				return null;
			} else {
				return shoppingCart;
			}

		} catch (javax.persistence.NoResultException nre) {
			return null;
		} catch (Throwable e) {
			throw new ServiceException(e);
		}

	}

	@Override
	@Transactional
	public void deleteCart(final ShoppingCart shoppingCart) throws ServiceException {
		ShoppingCart cart = this.getById(shoppingCart.getId());
		if (cart != null) {
			super.delete(cart);
		}
	}

	/*
	 * @Override
	 * 
	 * @Transactional public ShoppingCart getByCustomer(final Customer customer)
	 * throws ServiceException {
	 * 
	 * try { List<ShoppingCart> shoppingCart =
	 * shoppingCartRepository.findByCustomer(customer.getId()); if (shoppingCart ==
	 * null) { return null; } return getPopulatedShoppingCart(shoppingCart);
	 * 
	 * } catch (Exception e) { throw new ServiceException(e); } }
	 */

	@Transactional(noRollbackFor = { org.springframework.dao.EmptyResultDataAccessException.class })
	private ShoppingCart getPopulatedShoppingCart(final ShoppingCart shoppingCart, MerchantStore store) throws Exception {

		try {

			boolean cartIsObsolete = false;
			if (shoppingCart != null) {

				Set<ShoppingCartItem> items = shoppingCart.getLineItems();
				if (items == null || items.size() == 0) {
					shoppingCart.setObsolete(true);
					return shoppingCart;

				}

				// Set<ShoppingCartItem> shoppingCartItems = new
				// HashSet<ShoppingCartItem>();
				for (ShoppingCartItem item : items) {
					LOGGER.debug("Populate item " + item.getId());
					getPopulatedItem(item, store);
					LOGGER.debug("Obsolete item ? " + item.isObsolete());
					if (item.isObsolete()) {
						cartIsObsolete = true;
					}
				}

				Set<ShoppingCartItem> refreshedItems = new HashSet<>(items);

				shoppingCart.setLineItems(refreshedItems);
				update(shoppingCart);

				if (cartIsObsolete) {
					shoppingCart.setObsolete(true);
				}
				return shoppingCart;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ServiceException(e);
		}

		return shoppingCart;

	}

	@Override
	public ShoppingCartItem populateShoppingCartItem(Product product, MerchantStore store) throws ServiceException {
		Validate.notNull(product, "Product should not be null");
		Validate.notNull(product.getMerchantStore(), "Product.merchantStore should not be null");
		Validate.notNull(store, "MerchantStore should not be null");

		ShoppingCartItem item = new ShoppingCartItem(product);
		item.setSku(product.getSku());//already in the constructor

		// set item price
		FinalPrice price = pricingService.calculateProductPrice(product);
		item.setItemPrice(price.getFinalPrice());
		return item;

	}

	@Transactional
private void getPopulatedItem(final ShoppingCartItem item, MerchantStore store) throws Exception {

	Product product = productService.getBySku(item.getSku(), store, store.getDefaultLanguage());

	if (product == null) {
		item.setObsolete(true);
		return;
	}

	item.setProduct(product);
	item.setSku(product.getSku());

	if (product.isProductVirtual()) {
		item.setProductVirtual(true);
	}

	Set<ShoppingCartAttributeItem> cartAttributes = item.getAttributes();
	Set<ProductAttribute> productAttributes = product.getAttributes();
	List<ProductAttribute> attributesList = new ArrayList<ProductAttribute>();// attributes maintained
	List<ShoppingCartAttributeItem> removeAttributesList = new ArrayList<ShoppingCartAttributeItem>();// attributes
																										// to remove
	/* QECI-fix (2024-01-09 19:06:55.798727):
	Avoid nested loops:
	Using a HashMap to store ProductAttribute objects by their ID for efficient lookup and eliminating the inner loop.
	*/
	if (productAttributes != null && !productAttributes.isEmpty()) {
		Map<Long, ProductAttribute> attributesMap = new HashMap<>();
		for (ProductAttribute productAttribute : productAttributes) {
			attributesMap.put(productAttribute.getId(), productAttribute);
		}

		if (cartAttributes != null) {
			for (ShoppingCartAttributeItem attribute : cartAttributes) {
				ProductAttribute productAttribute = attributesMap.get(attribute.getProductAttributeId());
				if (productAttribute != null) {
					attribute.setProductAttribute(productAttribute);
					attributesList.add(productAttribute);
				} else {
					removeAttributesList.add(attribute);
				}
			}
		}
	}

	// cleanup orphean item
	if (CollectionUtils.isNotEmpty(removeAttributesList)) {
		for (ShoppingCartAttributeItem attr : removeAttributesList) {
			shoppingCartAttributeItemRepository.delete(attr);
		}
	}

	// cleanup detached attributes
	if (CollectionUtils.isEmpty(attributesList)) {
		item.setAttributes(null);
	}

	// set item price
	FinalPrice price = pricingService.calculateProductPrice(product, attributesList);
	item.setItemPrice(price.getFinalPrice());
	item.setFinalPrice(price);

	BigDecimal subTotal = item.getItemPrice().multiply(new BigDecimal(item.getQuantity()));
	item.setSubTotal(subTotal);

}

@Override
public List<ShippingProduct> createShippingProduct(final ShoppingCart cart) throws ServiceException {
	/**
	 * Determines if products are virtual
	 */
	Set<ShoppingCartItem> items = cart.getLineItems();
	/* QECI-fix (2024-01-09 19:06:55.798727):
	Avoid instantiations inside loops:
	Moving the instantiation of `ArrayList<ShippingProduct>` outside of the loop.
	*/
	List<ShippingProduct> shippingProducts = new ArrayList<ShippingProduct>();
	for (ShoppingCartItem item : items) {
		Product product = item.getProduct();
		if (!product.isProductVirtual() && product.isProductShipeable()) {
			if (shippingProducts.isEmpty()) {
				shippingProducts = new ArrayList<ShippingProduct>();
			}
			ShippingProduct shippingProduct = new ShippingProduct(product);
			shippingProduct.setQuantity(item.getQuantity());
			shippingProduct.setFinalPrice(item.getFinalPrice());
			shippingProducts.add(shippingProduct);
		}
	}

	return shippingProducts;

}

@Override
public void removeShoppingCart(final ShoppingCart cart) throws ServiceException {
	shoppingCartRepository.delete(cart);
}


	@Override
public ShoppingCart mergeShoppingCarts(final ShoppingCart userShoppingModel, final ShoppingCart sessionCart,
        final MerchantStore store) throws Exception {
    if (sessionCart.getCustomerId() != null
            && sessionCart.getCustomerId().equals(userShoppingModel.getCustomerId())) {
        LOGGER.info("Session Shopping cart belongs to same logged in user");
        if (CollectionUtils.isNotEmpty(userShoppingModel.getLineItems())
                && CollectionUtils.isNotEmpty(sessionCart.getLineItems())) {
            return userShoppingModel;
        }
    }

    LOGGER.info("Starting merging shopping carts");
    if (CollectionUtils.isNotEmpty(sessionCart.getLineItems())) {
        /* QECI-fix (2024-01-09 19:06:55.798727):
        Avoid nested loops
        Replaced the nested loops with a hashmap to reduce complexity from O(n^2) to O(n) when checking for duplicate items.
        */
        Map<Long, ShoppingCartItem> userShoppingCartItemsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userShoppingModel.getLineItems())) {
            for (ShoppingCartItem cartItem : userShoppingModel.getLineItems()) {
                userShoppingCartItemsMap.put(cartItem.getProduct().getId(), cartItem);
            }
        }

        Set<ShoppingCartItem> shoppingCartItemsSet = getShoppingCartItems(sessionCart, store, userShoppingModel);
        for (ShoppingCartItem sessionShoppingCartItem : shoppingCartItemsSet) {
            ShoppingCartItem existingItem = userShoppingCartItemsMap.get(sessionShoppingCartItem.getProduct().getId());
            if (existingItem != null) {
                LOGGER.info("Duplicate item found..updating existing product quantity");
                existingItem.setQuantity(existingItem.getQuantity() + sessionShoppingCartItem.getQuantity());
            } else {
                LOGGER.info("New item found..adding item to Shopping cart");
                userShoppingModel.getLineItems().add(sessionShoppingCartItem);
            }
        }
    }
    LOGGER.info("Shopping Cart merged successfully.....");
    saveOrUpdate(userShoppingModel);
    removeShoppingCart(sessionCart);

    return userShoppingModel;
}

private Set<ShoppingCartItem> getShoppingCartItems(final ShoppingCart sessionCart, final MerchantStore store,
        final ShoppingCart cartModel) throws Exception {

    /* QECI-fix (2024-01-09 19:06:55.798727):
    Avoid instantiations inside loops
    Moved the instantiation of HashSet and ArrayList outside of the loops to avoid repeated object creation.
    */
    Set<ShoppingCartItem> shoppingCartItemsSet = new HashSet<>();
    List<ShoppingCartAttributeItem> cartAttributes = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(sessionCart.getLineItems())) {
        for (ShoppingCartItem shoppingCartItem : sessionCart.getLineItems()) {
            Product product = productService.getBySku(shoppingCartItem.getSku(), store, store.getDefaultLanguage());
                    //.getById(shoppingCartItem.getProductId());
            if (product == null) {
                throw new Exception("Item with sku " + shoppingCartItem.getSku() + " does not exist");
            }

            if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new Exception("Item with sku " + shoppingCartItem.getSku()
                        + " does not belong to merchant " + store.getId());
            }

            ShoppingCartItem item = populateShoppingCartItem(product, store);
            item.setQuantity(shoppingCartItem.getQuantity());
            item.setShoppingCart(cartModel);

            cartAttributes.clear();
            if (shoppingCartItem != null && !CollectionUtils.isEmpty(shoppingCartItem.getAttributes())) {
                cartAttributes.addAll(shoppingCartItem.getAttributes());
                if (CollectionUtils.isNotEmpty(cartAttributes)) {
                    for (ShoppingCartAttributeItem shoppingCartAttributeItem : cartAttributes) {
                        ProductAttribute productAttribute = productAttributeService
                                .getById(shoppingCartAttributeItem.getId());
                        if (productAttribute != null && productAttribute.getProduct().getId().longValue() == product
                                .getId().longValue()) {

                            ShoppingCartAttributeItem attributeItem = new ShoppingCartAttributeItem(item,
                                    productAttribute);
                            if (shoppingCartAttributeItem.getId() > 0) {
                                attributeItem.setId(shoppingCartAttributeItem.getId());
                            }
                            item.addAttributes(attributeItem);

                        }
                    }
                }
            }

            shoppingCartItemsSet.add(item);
        }

    }
    return shoppingCartItemsSet;
}

@Override
@Transactional
public void deleteShoppingCartItem(Long id) {

    ShoppingCartItem item = shoppingCartItemRepository.findOne(id);
    if (item != null) {

        if (item.getAttributes() != null) {
            item.getAttributes().forEach(a -> shoppingCartAttributeItemRepository.deleteById(a.getId()));
            item.getAttributes().clear();
        }

        // refresh
        item = shoppingCartItemRepository.findOne(id);

        // delete
        shoppingCartItemRepository.deleteById(id);

    }

}

}
