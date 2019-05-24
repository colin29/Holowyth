
package com.mygdx.holowyth.combatDemo.ui;

import static com.mygdx.holowyth.util.DataUtil.getAsPercentage;
import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValue;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;
import com.mygdx.holowyth.util.tools.debugstore.ValueLabelMapping;

public class DebugStoreUI {

	Stage stage;

	Table debugInfo = new Table();

	ValueLabelMapping valueLabelMapping;

	DebugStore debugStore;

	public DebugStoreUI(Stage stage, DebugStore debugStore) {
		this.stage = stage;
		this.debugStore = debugStore;

		createDebugInfoDisplay();
	}

	public void populateDebugValueDisplay() {
		valueLabelMapping = new ValueLabelMapping();

		LabelStyle debugStyle = new LabelStyle(Holowyth.fonts.debugFont(), Holo.debugFontColor);

		for (Map.Entry<String, DebugValues> entry : debugStore.getStore().entrySet()) {
			String componentName = entry.getKey();
			debugInfo.add(new Label(componentName, debugStyle));
			debugInfo.row();
			ArrayList<DebugValue> listOfValues = entry.getValue();

			for (DebugValue v : listOfValues) {
				Label n = new Label(" -" + v.getName(), debugStyle);
				Label l = new Label("", debugStyle);
				debugInfo.add(n, l);
				debugInfo.row();
				valueLabelMapping.registerLabel(v, l);
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
		debugInfo.setFillParent(true);

		debugInfo.top().left();
		debugInfo.pad(4);
		// debugInfo.debug();

		debugInfo.defaults().spaceRight(20).left();

		stage.addActor(debugInfo);

		debugInfo.setVisible(Holo.debugPanelShowAtStartup);
	}

}
