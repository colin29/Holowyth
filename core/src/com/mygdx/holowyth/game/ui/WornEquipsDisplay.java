package com.mygdx.holowyth.game.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.holowyth.unit.WornEquips;
import com.mygdx.holowyth.unit.WornEquips.EquippedItemsListener;
import com.mygdx.holowyth.unit.WornEquips.Slot;
import com.mygdx.holowyth.unit.item.Equip;



@NonNullByDefault
public class WornEquipsDisplay extends SingleUseUIWidget implements EquippedItemsListener {

	@SuppressWarnings("null")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	Table equipsDisplay = new Table();
	
	WornEquips worn;
	
	Image soniaFace;
	
	public WornEquipsDisplay(WornEquips worn, Stage stage, Skin skin, AssetManager assets) {
		super(stage, skin);
		this.worn = worn;
		worn.addListener(this);
		createDisplay();
		root.debugAll();
		
		Texture tex = assets.get("img/sprites/head/Sonia.png", Texture.class);
		soniaFace = new Image(tex);
		
	}
	
	private void createDisplay(){
		root.add(equipsDisplay);
		regenerateDisplay();
		
	}
	private void regenerateDisplay() {
		equipsDisplay.clear();
		Table slots1 = new Table();
		Table slots2 = new Table();
		
		slots1.defaults().pad(20);
		slots2.defaults().pad(20);
		
		slots1.add(makeLabel(Slot.HEAD));
		slots1.row();
		slots1.add(makeLabel(Slot.BODY));
		slots1.row();
		slots1.add(makeLabel(Slot.SHOES));
		slots1.row();
		
		slots2.add(makeLabel(Slot.MAIN_HAND));
		slots2.row();
		slots2.add(makeLabel(Slot.OFF_HAND));
		slots2.row();
		slots2.add(makeLabel(Slot.ACCESSORY));
		slots2.row();
		
		equipsDisplay.add(slots1).width(150);
		equipsDisplay.add(soniaFace).maxWidth(100).top().padTop(20);
		equipsDisplay.add(slots2).width(150);
	}
	private Label makeLabel(Slot slot) {
		Equip equip = worn.getEquip(slot);
		Label entry = new Label("", skin);
		if(equip==null) {
			entry.setText(String.format("{%s}", slot.getName()));
		}else{
			entry.setText(equip.name);
		}
		entry.addListener(new ClickListener() {
			{
//				setTapCountInterval()
			}
			@Override
			public void clicked(@Nullable InputEvent event, float x, float y) {
				logger.debug("tap count: {}", getTapCount());
				if(getTapCount() == 2) {
					worn.unequip(slot);
				}
			}
			
		});
		return entry;
	}

	@Override
	public void remove() {
		root.remove();
	}

	@Override
	public void changed() {
		regenerateDisplay();
	}
	
	
	
}
