package com.mygdx.holowyth.world.town;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.holowyth.util.HoloUI;
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
	private final Stage stage;
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public ShopUI(ShopSession session, Stage stage, Skin skin) {
		this.stage = stage;
		this.session = session;
		this.shop = session.getShop();
		this.skin = skin;
		currencyOwned = new Label("", skin);
		createUI(shop, stage);
//		root.setDebug(true, true);
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
		labels[2] = new Label(String.format("[%ssp]", entry.costEa), skin);
		for(int i=0;i<labels.length;i++) {
			labels[i].addListener(new InputListener() {
				@Override
				public boolean touchDown(@Nullable InputEvent event, float x, float y, int pointer, int button) {
					createPurchaseConfirmationDialog(entry);
					return true;
				}
			});
			if(i!=0) {
				labels[i].setAlignment(Align.center);
			}
			listings.add(labels[i]).fill().space(10);
		}
		
	}
	
	private void createPurchaseConfirmationDialog(ItemStock entry) {
		logger.debug("Hi!");
		HoloUI.confirmationDialog(stage, skin, String.format("Purchase '%s'  for %ssp?", entry.item.name, entry.costEa), "", "Purchase", ()->{session.purchase(entry);});
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
