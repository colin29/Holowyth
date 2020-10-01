package com.mygdx.holowyth.gameScreen.session;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.unit.Unit;

/**
 * @author Colin
 *
 */

@NonNullByDefault
public class SessionData {
	public final List<@NonNull Unit> playerUnits = new ArrayList<@NonNull Unit>();
	public final OwnedCurrency ownedCurrency = new OwnedCurrency();
	public final OwnedItems ownedItems = new OwnedItems();
	
	
	/**
	 * For testing
	 */
	public static class DummySessionData extends SessionData{
		{
			ownedCurrency.add(5000);
		}
	}

}
