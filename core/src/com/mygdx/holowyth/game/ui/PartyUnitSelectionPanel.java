package com.mygdx.holowyth.game.ui;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.WornEquips.Slot;

/**
 * Shows a list of player head sprites
 * @author Colin
 *
 */
@NonNullByDefault
public class PartyUnitSelectionPanel extends SingleUseUIWidget {

	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	Table panel = new Table();
	List<@NonNull Unit> units;
	
	public PartyUnitSelectionPanel(List<@NonNull Unit> units, Stage stage, Skin skin, AssetManager assets) {
		super(stage, skin, assets);
		this.units = units;
		
		createPanel();
		root.left().bottom();
		root.debugAll();
	}
	
	private void createPanel(){
		root.add(panel);
		regeneratePanel();
		
	}
	@SuppressWarnings("null")
	private void regeneratePanel() {
		panel.clear();
		for(Unit u : units) {
			String headSpriteName = u.graphics.getHeadSpriteName();
			logger.debug("Unit '{}' headsprite name: '{}'", u.getName(), headSpriteName);
			if(assets.isLoaded(headSpriteName)) { // isLoaded will return false if null
				panel.add(getImage(headSpriteName));
			}else {
				panel.add(getImage("img/sprites/head/Default.png"));
			}
		}
		
	}
	private Image getImage(String path) {
		return new Image(assets.get(path, Texture.class));
	}
}
