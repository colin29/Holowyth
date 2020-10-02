package com.mygdx.holowyth.game.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.holowyth.game.session.OwnedItems;
import com.mygdx.holowyth.game.session.OwnedItems.InventoryListener;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.item.Item;

@NonNullByDefault
public class InventoryDisplay extends SingleUseUIWidget implements InventoryListener {

	Table display = new Table();

	/**
	 * If set, double-clicking on a equip will attempt to equip it to that unit.
	 */
	private @Nullable Unit unit;

	private final OwnedItems owned;

	public InventoryDisplay(Stage stage, Skin skin, OwnedItems owned) {
		super(stage, skin);
		this.owned = owned;
		createDisplay();
		owned.addListener(this);
		root.debugAll();
		root.right();
	}

	private void createDisplay() {
		root.add(display).pad(10);
		display.defaults().space(20);
		regenerateDisplay();
	}

	private void regenerateDisplay() {
		display.clear();
		for (Item item : owned.getItems()) {
			display.add(makeLabel(item)).width(100).height(30);
			display.row();
		}
	}

	private Label makeLabel(Item item) {
		Label entry = new Label("", skin);
		entry.setText(item.name);
		entry.addListener(new ClickListener() {
			@SuppressWarnings("null")
			@Override
			public void clicked(@Nullable InputEvent event, float x, float y) {
				if (getTapCount() == 2) {
					if (item instanceof Equip) {
						Equip equip = (Equip) item;
						if (unit != null) {
							unit.equip.equip(equip);
						}
					}
				}
			}

		});
		return entry;

	}

	@Override
	public void changed() {
		regenerateDisplay();
	}

	public void setLinkedUnit(Unit unit) {
		this.unit = unit;
	}
}
