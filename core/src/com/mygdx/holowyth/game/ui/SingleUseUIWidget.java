package com.mygdx.holowyth.game.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

@NonNullByDefault
public abstract class SingleUseUIWidget {
	protected final Stage stage; 
	protected final Skin skin;
	protected Table root = new Table();
	protected AssetManager assets;
	
	public SingleUseUIWidget(Stage stage, Skin skin, AssetManager assets) {
		this.stage = stage;
		this.skin = skin;
		this.assets = assets;
		stage.addActor(root);
		root.setFillParent(true);
		root.center();
	}
	
	public void remove() {
		root.remove();
	}
}
