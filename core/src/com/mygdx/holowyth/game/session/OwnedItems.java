package com.mygdx.holowyth.game.session;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.item.Item;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * No item stacks for now
 * @author Colin
 */
public class OwnedItems {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final @NonNull List<@NonNull Item> items = new ArrayList<>();
	
	public boolean remove(Item item) {
		if(!items.contains(item))
			logger.warn("Tried to remove item {} which isn't in inventory.");
		return items.remove(item);
	}
	public boolean addItem(@NonNull Item item) {
		if(items.contains(item))
			throw new HoloIllegalArgumentsException("Item was already in inventory: " + item.name);
		items.add(item);
		return true;
	}
}
