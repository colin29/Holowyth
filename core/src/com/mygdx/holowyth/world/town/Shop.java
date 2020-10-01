package com.mygdx.holowyth.world.town;

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
	

	private final List<@NonNull ItemStock> itemStocks = new ArrayList<>();
	
	public static class ItemStock {
		public Item item; // this item should be copied when bought
		public int costEa;
		public int count = 1;
		
		public ItemStock(Item item, int costEa) {
			item.markAsTemplate();
			this.item = item;
			this.costEa = costEa;
		}
		
		public ItemStock(Item item, int costEa, int stock) {
			item.markAsTemplate();
			this.item = item;
			this.costEa = costEa;
			this.count = stock;
		}
	}
	
	public void addItemStock(Item item, int costEa) {
		addItemStock(item, costEa, 1);
	}
	
	public void addItemStock(Item item, int costEa, int stock) {
		logger.debug("Added {}x '{}' to store with price [{} sp]", stock, item.name, costEa);
		itemStocks.add(new ItemStock(item, costEa, stock));
	}
	void removeItemStock(ItemStock itemStock, int amount) {
		if(!itemStocks.contains(itemStock))
			throw new HoloIllegalArgumentsException("Tried to remove item stock not in store");
		logger.debug("Removed {}x '{}' from store", amount, itemStock.item.name);
		if(itemStock.count < amount) {
			logger.warn("Removed {}x, but stock only had {}", amount, itemStock.count);
			itemStock.count = 0;
		}else {
			itemStock.count -= amount;
		}
	}
	public ShopSession enter(OwnedCurrency customerFunds, OwnedItems customerItems) {
		return new ShopSession(this, customerFunds, customerItems);
	}
	
	public List<@NonNull ItemStock> getItemStocks() {
		return itemStocks;
	}
	
}
