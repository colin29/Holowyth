
package com.mygdx.holowyth.util.tools.debugstore;

import static com.mygdx.holowyth.util.DataUtil.percentage;
import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloUI;

/**
 * Is used as a common UI submodule. When created, it exposes a table which the caller can place where desired
 * 
 * May add minor customization like font color and type
 * 
 * @author Colin Ta
 *
 */
public class DebugStoreUI {

	Table debugTable = new Table();

	ValueLabelMapping valueLabelMapping;

	DebugStore debugStore;

	/**
	 * Creates the debugValues table, call getDebugValuesTable() to access
	 * @param debugStore
	 */
	public DebugStoreUI(DebugStore debugStore) {
		this.debugStore = debugStore;
		createDebugInfoDisplay();
	}
	
//	public static DebugStoreUI create(DebugStore debugStore) {
//		return new DebugStoreUI(debugStore);
//	}

	/**
	 * Call after whatever components you want are registered. Additional calls regenerate the table.
	 */
	public void populateDebugValueDisplay() {
		valueLabelMapping = new ValueLabelMapping();
		debugTable.clear();

		LabelStyle debugStyle = new LabelStyle(Holowyth.fonts.debugFont(), Color.BLACK);

		for (Map.Entry<String, DebugValues> entry : debugStore.getStore().entrySet()) {
			String componentName = entry.getKey();
			debugTable.add(new Label(componentName, debugStyle));
			debugTable.row();
			ArrayList<DebugValue> listOfValues = entry.getValue();

			for (DebugValue v : listOfValues) {
				if (v.isASpacingEntry()) {
					debugTable.add(new Label("", debugStyle));
					debugTable.row();
				} else {
					Label n = new Label(" -" + v.getName(), debugStyle);
					Label l = new Label("", debugStyle);
					debugTable.add(n, l);
					debugTable.row();
					valueLabelMapping.registerLabel(v, l);
				}
			}
		}
	}

	public void updateDebugValueDisplay() {
		if (valueLabelMapping != null) {
			valueLabelMapping.forEach(DebugStoreUI::updateLabel);
		}
	}

	private static void updateLabel(DebugValue v, Label l) {
		String str;

		switch (v.getValueType()) {
		case FLOAT:
			if (v.shouldDisplayAsPercentage()) {
				str = percentage(v.getFloatValue());
			} else {
				str = getRoundedString(v.getFloatValue());
			}
			break;
		case INT:
			str = String.valueOf(v.getIntValue());
			break;
		case STRING:
			str = v.getStringValue();
			break;
		default:
			System.out.println("Unsupported debug value type");
			str = null;
			break;
		}

		l.setText(str);
	}

	public DebugStore getDebugStore() {
		return debugStore;
	}

	public Table getDebugValuesTable() {
		return debugTable;
	}

	public void createDebugInfoDisplay() {
		debugTable = new Table();

		debugTable.top().left();
		debugTable.pad(4);

		debugTable.defaults().spaceRight(20).left();
		debugTable.setVisible(Holo.debugPanelShowAtStartup);
		debugTable.setBackground(HoloUI.getSolidBG(Color.WHITE, 0.4f));
	}

}
