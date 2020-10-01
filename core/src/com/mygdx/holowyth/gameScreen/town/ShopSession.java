package com.mygdx.holowyth.gameScreen.town;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gameScreen.session.OwnedCurrency;
import com.mygdx.holowyth.gameScreen.session.OwnedItems;
import com.mygdx.holowyth.gameScreen.town.Shop.ItemStock;
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
			logger.debug("Purchased '{}' from shop", itemStock.item.name);
			customerFunds.subtract(itemStock.costEa);
			customerItems.addItem(itemStock.item.cloneObject());
			shop.removeItemStock(itemStock, 1);
			return true;
		}
		return false;
	}

}
