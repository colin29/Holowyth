package com.mygdx.holowyth.gameScreen.town;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gameScreen.session.OwnedCurrency;
import com.mygdx.holowyth.gameScreen.session.OwnedItems;
import com.mygdx.holowyth.unit.item.Item;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

@NonNullByDefault
public class Shop {
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private OwnedCurrency customerFunds;
	private OwnedItems customerItems;
	
	private final List<@NonNull ItemStock> itemStocks = new ArrayList<>();
	
	public static class ItemStock {
		public Item item; // this item should be copied when bought
		public int costEa = 1;
		public int stock = 1;
		
		public ItemStock(Item item, int costEa) {
			item.markAsTemplate();
			this.item = item;
			this.costEa = costEa;
		}
		
		public ItemStock(Item item, int costEa, int stock) {
			item.markAsTemplate();
			this.item = item;
			this.costEa = costEa;
			this.stock = stock;
		}
	}
	
	/**
	 * @param ownedCurrency The currency collection that will be used for purchases and sales.
	 */
	public Shop(OwnedCurrency customerFunds, OwnedItems customerItems){
		this.customerFunds = customerFunds;
		this.customerItems = customerItems;
	}
	
	public boolean purchase(ItemStock itemStock) {
		if(!itemStocks.contains(itemStock))
			throw new HoloIllegalArgumentsException("Tried to purchase item entry not in store");
		if(customerFunds.canAfford(itemStock.costEa)) {
			logger.debug("Purchased '{}' from shop", itemStock.item.name);
			customerFunds.subtract(itemStock.costEa);
			customerItems.addItem(itemStock.item.cloneObject());
			return true;
		}
		return false;
	}
	
	public void addItemStock(Item item, int costEa) {
		addItemStock(item, costEa, 1);
	}
	
	public void addItemStock(Item item, int costEa, int stock) {
		logger.debug("Added {}x '{}' to store with price [{} sp]", stock, item.name, costEa);
		itemStocks.add(new ItemStock(item, costEa, stock));
	}
	public List<@NonNull ItemStock> getItemStocks() {
		return itemStocks;
	}
	
}
