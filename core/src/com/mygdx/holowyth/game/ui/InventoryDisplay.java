package com.mygdx.holowyth.game.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.game.session.OwnedItems;
import com.mygdx.holowyth.game.session.OwnedItems.InventoryListener;
import com.mygdx.holowyth.unit.item.Item;

@NonNullByDefault
public class InventoryDisplay extends SingleUseUIWidget implements InventoryListener{

	
	Table display = new Table();
	
	private final OwnedItems owned;
	public InventoryDisplay(Stage stage, Skin skin, OwnedItems owned) {
		super(stage, skin);
		this.owned = owned;
		createDisplay();
		owned.addListener(this);
		root.debugAll();
		root.right();
	}

	private void createDisplay(){
		root.add(display).pad(10);
		display.defaults().space(20);
		regenerateDisplay();
	}
	private void regenerateDisplay() {
		display.clear();
		for(Item item : owned.getItems()) {
			display.add(makeLabel(item)).width(100).height(30);
			display.row();
		}
	}
	private Label makeLabel(Item item) {
		Label entry = new Label("", skin);
		entry.setText(item.name);
		return entry;
	}

	@Override
	public void changed() {
		regenerateDisplay();
	}
}
