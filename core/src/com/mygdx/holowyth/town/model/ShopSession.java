package com.mygdx.holowyth.town.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.game.session.OwnedCurrency;
import com.mygdx.holowyth.game.session.OwnedItems;
import com.mygdx.holowyth.town.model.Shop.ItemStock;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

@NonNullByDefault
public class ShopSession {
	
	private OwnedCurrency customerFunds;
	private OwnedItems customerItems;
	
	private final Shop shop;
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * @param ownedCurrency The currency collection that will be used for purchases and sales.
	 */
	public ShopSession(Shop shop, OwnedCurrency customerFunds, OwnedItems customerItems){
		this.shop = shop;
		this.customerFunds = customerFunds;
		this.customerItems = customerItems;
	}
	
	public boolean purchase(ItemStock itemStock) {
		if(!shop.getItemStocks().contains(itemStock))
			throw new HoloIllegalArgumentsException("Tried to purchase item entry not in store");
		if(customerFunds.canAfford(itemStock.costEa)) {
			if(itemStock.count < 1) {
				logger.warn("Insufficient stock: purchase not made");
				return false;
			}
			logger.debug("Purchased '{}' from shop", itemStock.item.name);
			customerFunds.subtract(itemStock.costEa);
			customerItems.addItem(itemStock.item.cloneObject());
			shop.removeItemStock(itemStock, 1);
			return true;
		}
		return false;
	}

	public OwnedCurrency getCustomerFunds() {
		return customerFunds;
	}

	public Shop getShop() {
		return shop;
	}

}
