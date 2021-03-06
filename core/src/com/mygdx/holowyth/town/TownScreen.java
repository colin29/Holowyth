package com.mygdx.holowyth.town;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.game.StandardGameScreen;
import com.mygdx.holowyth.game.base.GameScreen;
import com.mygdx.holowyth.game.session.SessionData;
import com.mygdx.holowyth.gamedata.towns.TestTown;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.town.model.Shop;
import com.mygdx.holowyth.town.model.Town;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.template.HoloBaseScreen;
import com.mygdx.holowyth.world.map.Entrance.MapDestination;

@NonNullByDefault
public class TownScreen extends HoloBaseScreen {
	
	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Town town;
	private final InputMultiplexer multiplexer = new InputMultiplexer();
	
	private @Nullable StandardGameScreen gameScreen;

	public MapDestination exitDest = new MapDestination("forest1", "entrance_1");

	public TownScreen(Holowyth game, SessionData session, @Nullable StandardGameScreen gameScreen) {
		super(game);
		this.gameScreen = gameScreen;
		
		town = new TestTown();
		final Shop shop = town.shop;

		root.bottom().right();
		HoloUI.textButton(root, "Leave Town", skin, ()->{leaveTown();}).width(100).height(60);
		
		var shopSession = shop.enter(session.ownedCurrency, session.ownedItems);
		@SuppressWarnings("unused")
		var shopUI = new ShopUI(shopSession, stage, skin);
		shopSession.purchase(shop.getItemStocks().get(0));
	}
	
	
	/**
	 * If gameScreen wasn't set, does nothing
	 */
	protected void leaveTown() {
		if(gameScreen != null) {
			@NonNull StandardGameScreen gameScreen = this.gameScreen;
			
			game.setScreen(gameScreen);
			gameScreen.goToMap(exitDest.map, exitDest.loc);
		}else {
			logger.info("Game screen wasn't set, so nowhere to leave to");
		}
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
		HoloGL.clearScreenAndSetGLBlending(Color.BLACK);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
	}

	
	

}
