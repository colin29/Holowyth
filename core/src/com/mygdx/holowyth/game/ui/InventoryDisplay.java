package com.mygdx.holowyth.game.ui;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.holowyth.game.session.OwnedItems;
import com.mygdx.holowyth.game.session.OwnedItems.InventoryListener;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.WornEquips.EquipResult;
import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.item.Item;
import com.mygdx.holowyth.util.HoloUI;

@NonNullByDefault
public class InventoryDisplay extends SingleUseUIWidget implements InventoryListener {

	Table display = new Table();

	/**
	 * If set, double-clicking on a equip will attempt to equip it to that unit.
	 */
	private @Nullable Unit unit;

	private final OwnedItems inv;

	public InventoryDisplay(Stage stage, Skin skin, OwnedItems owned, AssetManager assets) {
		super(stage, skin, assets);
		inv = owned;
		createDisplay();
		owned.addListener(this);
		root.right();
//		root.debugAll();
		 hide();
	}

	private void createDisplay() {
		root.add(display).pad(10);
		display.defaults().space(20);
		display.setBackground(HoloUI.getSolidBG(HoloUI.menuColor));
		regenerateDisplay();
	}

	private void regenerateDisplay() {
		display.clear();
		for (Item item : inv.getItems()) {
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
							EquipResult result = unit.equip.equip(equip);
							if(result.success) {
								inv.remove(equip);
								for(Equip e : result.itemsRemoved) {
									inv.addItem(e);
								}
							}
						}
					}
				}
			}

		});
		return entry;

	}
	public void show() {
		root.setVisible(true);
	}
	public void hide() {
		root.setVisible(false);
	}

	@Override
	public void changed() {
		regenerateDisplay();
	}

	public void setLinkedUnit(Unit unit) {
		this.unit = unit;
	}
}
