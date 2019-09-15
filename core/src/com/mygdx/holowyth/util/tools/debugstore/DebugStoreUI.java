
package com.mygdx.holowyth.util.tools.debugstore;

import static com.mygdx.holowyth.util.DataUtil.getAsPercentage;
import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.Holo;

/**
 * Is used as a common UI submodule. When created, it exposes a table which the caller can place where desired
 * 
 * May add minor customization like font color and type
 * 
 * @author Colin Ta
 *
 */
public class DebugStoreUI {

	Table debugInfo = new Table();

	ValueLabelMapping valueLabelMapping;

	DebugStore debugStore;

	public DebugStoreUI(DebugStore debugStore) {
		this.debugStore = debugStore;

		createDebugInfoDisplay();
	}

	/**
	 * This may not work for loading a second map, atm. Nonetheless, this still needs to be called after debug values are added from all components --
	 * at the end of component creation
	 */
	public void populateDebugValueDisplay() {
		valueLabelMapping = new ValueLabelMapping();

		LabelStyle debugStyle = new LabelStyle(Holowyth.fonts.debugFont(), Color.BLACK);

		for (Map.Entry<String, DebugValues> entry : debugStore.getStore().entrySet()) {
			String componentName = entry.getKey();
			debugInfo.add(new Label(componentName, debugStyle));
			debugInfo.row();
			ArrayList<DebugValue> listOfValues = entry.getValue();

			for (DebugValue v : listOfValues) {
				if (v.isASpacingEntry()) {
					debugInfo.add(new Label("", debugStyle));
					debugInfo.row();
				} else {
					Label n = new Label(" -" + v.getName(), debugStyle);
					Label l = new Label("", debugStyle);
					debugInfo.add(n, l);
					debugInfo.row();
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
				str = getAsPercentage(v.getFloatValue());
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

	public Table getDebugInfo() {
		return debugInfo;
	}

	public void createDebugInfoDisplay() {
		debugInfo = new Table();

		debugInfo.top().left();
		debugInfo.pad(4);

		debugInfo.defaults().spaceRight(20).left();
		debugInfo.setVisible(Holo.debugPanelShowAtStartup);
	}

}
