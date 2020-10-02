package com.mygdx.holowyth.game.ui;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.holowyth.unit.Unit;

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
	
	@Nullable Unit selectedUnit;
	
	
	//Reference
	InventoryDisplay inv;
	//Owns
	@Nullable WornEquipsDisplay worn; 
	
	
	public PartyUnitSelectionPanel(List<@NonNull Unit> units, InventoryDisplay inv, Stage stage, Skin skin, AssetManager assets) {
		super(stage, skin, assets);
		this.units = units;
		this.inv = inv;
		
		
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
			
			Image image = u.graphics.getHeadSprite(); 
			panel.add(image);
			image.addListener(new ClickListener() {
				@Override
				public void clicked(@Nullable InputEvent event, float x, float y) {
					logger.debug("clicked head icon");
					openForUnit(u);
				}
			});
		}
		
	}

	public void openForUnit(Unit unit) {
		logger.debug("Opening for unit '{}'", unit.getName());
		if(selectedUnit == unit) 
			return;
		if(worn != null)
			worn.remove();
		selectedUnit = unit;
		inv.setLinkedUnit(unit);
		worn = new WornEquipsDisplay(unit, stage, skin, assets);
	}
}
