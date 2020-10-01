package com.mygdx.holowyth.world.town;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gameScreen.StandardGameScreen;
import com.mygdx.holowyth.gameScreen.session.SessionData;
import com.mygdx.holowyth.gamedata.towns.TestTown;
import com.mygdx.holowyth.util.template.HoloBaseScreen;

@NonNullByDefault
public class TownScreen extends HoloBaseScreen {
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Town town;
	
	public TownScreen(Holowyth game, SessionData session) {
		this(game, session, null);
	}
	
	public TownScreen(Holowyth game, SessionData session, @Nullable StandardGameScreen gameScreen) {
		super(game);
		
		town = new TestTown();
		final Shop shop = town.shop;
		
		var shopSession = shop.enter(session.ownedCurrency, session.ownedItems);
		var shopUI = new ShopUI(shopSession, stage, skin);
		shopSession.purchase(shop.getItemStocks().get(0));
		
	}
	
	public void loadTown(Town town){
		logger.info("Loaded town '{}'", town.getName());
		this.town = town;
		Drawable bg = new TextureRegionDrawable(assets.get("img/bg/shop.jpg", Texture.class));
		root.setBackground(bg);
	}
	public void shutdownTown(){
	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		stage.draw();
	}

	
	

}
