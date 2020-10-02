package com.mygdx.holowyth.game.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.WornEquips.EquippedItemsListener;
import com.mygdx.holowyth.unit.item.Item;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * No item stacks for now
 * @author Colin
 */
@NonNullByDefault
public class OwnedItems {
	
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private final List<InventoryListener> listeners = new ArrayList<>();
	
	private final List<@NonNull Item> items = new ArrayList<>();
	
	public boolean remove(Item item) {
		if(!items.contains(item)) {
			logger.warn("Tried to remove item {} which isn't in inventory.");
			return false;
		}else {
			items.remove(item);
			changed();
			return true;
		}
	}
	public boolean addItem(Item item) {
		if(items.contains(item)) {
			logger.warn("Item was already in inventory: '{}'", item.name);
			return false;
		}
		items.add(item);
		changed();
		return true;
	}
	@SuppressWarnings("null")
	public List<@NonNull Item> getItems(){
		return Collections.unmodifiableList(items);
	}
	
	public void addListener(InventoryListener o) {
		listeners.add(o);
	}

	public boolean removeListener(InventoryListener o) {
		return listeners.remove(o);
	}

	public void changed() {
		for (var o : listeners)
			o.changed();
	}

	public static interface InventoryListener {
		public abstract void changed();
	}
}
