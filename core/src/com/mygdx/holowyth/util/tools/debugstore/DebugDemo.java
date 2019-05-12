package com.mygdx.holowyth.util.tools.debugstore;

import static com.mygdx.holowyth.util.DataUtil.*;

import java.util.ArrayList;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.mygdx.holowyth.util.template.adapters.ApplicationListenerAdapter;

/**
 * Testing class: Simulates roughly the main usage case for the DebugStore
 * 
 * @author Colin Ta
 *
 */
public class DebugDemo extends ApplicationListenerAdapter {

	private static DebugStore store = new DebugStore();
	private Skin skin;

	private ValueLabelMapping mapping = new ValueLabelMapping();
	
	private Table debugTable;
	private Stage stage;
	private float f;
	
	@Override
	public void create() {
		
		// Load Skin:
		VisUI.load();
		skin = VisUI.getSkin();
		
		// Create UI:
		stage = new Stage(new ScreenViewport());
		debugTable = new Table();
		debugTable.setFillParent(true);
		stage.addActor(debugTable);
		
		// Simulate a component registering and adding values

		ArrayList<DebugValue> debugValues = store.registerComponent("myComponent");
		ArrayList<DebugValue> debugValues2 = store.registerComponent("AnotherComponent");

		int someIntValue = 0;
		debugValues.add(new DebugValue("Some int value", () -> someIntValue));
		
		int i = 5;
		f = 0.3f;
		debugValues2.add(new DebugValue("Foo", () -> i));
		debugValues2.add(new DebugValue("Bar", () -> f));

		// Simulate the Viewer generating a table
		createDebugTable();
		
		// call update whenever needed (usually every render)
		
		mapping.forEach(DebugDemo::updateLabel);
		
	}
	
	@Override
	public void render() {
		clearScreen(Color.BLACK);
		
		stage.draw();
		stage.act(Gdx.graphics.getDeltaTime());
		
		f+=0.01;
		mapping.forEach(DebugDemo::updateLabel);
	}

	private void createDebugTable() {
//		debugTable.debug();
		debugTable.defaults().spaceRight(20).left();
		debugTable.top().left();
		
		for (Map.Entry<String, DebugValues> entry : store.getStore().entrySet()) {
			String componentName = entry.getKey();
			debugTable.add(new Label(componentName, skin));
			debugTable.row();
			ArrayList<DebugValue> listOfValues = entry.getValue();

			for (DebugValue v : listOfValues) {
				Label n = new Label(v.getName(), skin);
				Label l = new Label("", skin);
				debugTable.add(n, l);
				debugTable.row();
				mapping.registerLabel(v, l);
			}
		}
	}

	private static void updateLabel(DebugValue v, Label l) {
		String str;

		switch (v.valueType) {
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
		default:
			System.out.println("Unsupported debug value type");
			str = null;
			break;
		}
		
		l.setText(str);
//		System.out.println("Updated value:  -" + v.name + " " + str);
	}

}
