package com.mygdx.holowyth.gameScreen.town;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.gameScreen.session.OwnedCurrency;
import com.mygdx.holowyth.gameScreen.session.OwnedItems;
import com.mygdx.holowyth.unit.item.Item;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class Shop {
	
	private OwnedCurrency customerFunds;
	private OwnedItems customerItems;
	
	private final List<@NonNull ItemStock> itemStocks = new ArrayList<>();
	
	public static class ItemStock {
		public @NonNull Item item; // this item should be copied when bought
		public int costEa = 1;
		public int stock = 1;
		
		public ItemStock(@NonNull Item item, int costEa) {
			item.markAsTemplate();
			this.item = item;
			this.costEa = costEa;
		}
		
		public ItemStock(@NonNull Item item, int costEa, int stock) {
			item.markAsTemplate();
			this.item = item;
			this.costEa = costEa;
			this.stock = stock;
		}
	}
	
	/**
	 * @param ownedCurrency The currency collection that will be used for purchases and sales.
	 */
	public Shop(@NonNull OwnedCurrency customerFunds, @NonNull OwnedItems customerItems){
		this.customerFunds = customerFunds;
		this.customerItems = customerItems;
	}
	
	public boolean purchase(@NonNull ItemStock itemStock) {
		if(!itemStocks.contains(itemStock))
			throw new HoloIllegalArgumentsException("Tried to purchase item entry not in store");
		if(customerFunds.canAfford(itemStock.costEa)) {
			customerItems.addItem(itemStock.item.cloneObject());
			return true;
		}
		return false;
	}
	
	public void addItemStock(@NonNull Item item, int costEa) {
		itemStocks.add(new ItemStock(item, costEa));
	}
	
	public void addItemStock(@NonNull Item item, int costEa, int stock) {
		itemStocks.add(new ItemStock(item, costEa, stock));
	}
	
}
