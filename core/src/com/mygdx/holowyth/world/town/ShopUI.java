package com.mygdx.holowyth.world.town;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.holowyth.world.town.Shop.ItemStock;

/**
 * This object should be created when someone enters a store
 */
@NonNullByDefault
public class ShopUI implements Shop.ShopChangedListener {  // Give it a stage

	private Table root = new Table();
	private Table listings = new Table();
	private Label currencyOwned;
	
	private final ShopSession session;
	private final Shop shop;
	private Skin skin;
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public ShopUI(ShopSession session, Stage stage, Skin skin) {
		this.session = session;
		this.shop = session.getShop();
		this.skin = skin;
		currencyOwned = new Label("", skin);
		createUI(shop, stage);
		root.setDebug(true, true);
		shop.addListener(this);
		
		update();
	}
	private void createUI(Shop shop, Stage stage) {
		root.setFillParent(true);
		stage.addActor(root);
		root.add(listings).height(400).width(300);
		root.align(Align.topLeft);
		root.row();
		root.add(currencyOwned);
	}
	
	private void update() {
		regenerateItemListings();
		updateCurrencyOwnedText();

	}
	private void regenerateItemListings(){
		listings.clear();
		for(var a : shop.getItemStocks()) {
			addNewItemListing(a);
			listings.row();
		}
	}
	private void updateCurrencyOwnedText() {
		currencyOwned.setText("Funds: " + session.getCustomerFunds().getBalance() + "sp");
	}
	private void addNewItemListing(ItemStock entry) {
		Label[]  labels = new Label[3];
		labels[0] = new Label(entry.item.name, skin);
		labels[1] = new Label(String.format("(x%s)", entry.count), skin);
		labels[2] = new Label(String.format("[x%s]", entry.costEa), skin);
		
		for(var label : labels) {
			label.addListener(new ClickListener() {
				@Override
				public void clicked(@Nullable InputEvent event, float x, float y) {
					makePurchaseConfirmationDialog(entry);
				}
			});
			listings.add(label);
		}
		
	}
	
	private void makePurchaseConfirmationDialog(ItemStock entry) {
		logger.debug("Hi!");
	}

	/**
	 * Remove all shop's UI elements from stage, removes listeners
	 */
	public void remove() {
		root.remove();
	}
	@Override
	public void changed() {
		update();
	}
}
