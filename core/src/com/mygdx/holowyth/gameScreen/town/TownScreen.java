package com.mygdx.holowyth.gameScreen.town;

import org.eclipse.jdt.annotation.NonNull;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gameScreen.StandardGameScreen;
import com.mygdx.holowyth.gameScreen.session.SessionData;
import com.mygdx.holowyth.util.template.HoloBaseScreen;

public class TownScreen extends HoloBaseScreen {

	
	private final Shop shop;
	
	public TownScreen(Holowyth game, @NonNull SessionData session) {
		this(game, session, null);
	}
	
	public TownScreen(Holowyth game, @NonNull SessionData session, StandardGameScreen gameScreen) {
		super(game);
		
		shop = new Shop(session.ownedCurrency, session.ownedItems);
		
		Drawable bg = new TextureRegionDrawable(assets.get("img/bg/shop.jpg", Texture.class));
		root.setBackground(bg);
	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		stage.draw();
	}

	
	

}
