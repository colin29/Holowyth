package com.mygdx.holowyth.gameScreen.session;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.unit.Unit;

/**
 * @author Colin
 *
 */
public class SessionData {
	public final List<@NonNull Unit> playerUnits = new ArrayList<@NonNull Unit>();
	public final @NonNull OwnedCurrency ownedCurrency = new OwnedCurrency();
	public final @NonNull OwnedItems ownedItems = new OwnedItems();
}
