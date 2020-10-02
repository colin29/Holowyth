package com.mygdx.holowyth.game.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

@NonNullByDefault
public abstract class SingleUseUIWidget {
	protected final Stage stage; 
	protected final Skin skin;
	protected Table root = new Table();
	
	public SingleUseUIWidget(Stage stage, Skin skin) {
		this.stage = stage;
		this.skin = skin;
		stage.addActor(root);
		root.setFillParent(true);
		root.center();
	}
	
	public void remove() {
		root.remove();
	}
}
