package com.mygdx.holowyth.combatDemo.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.combatDemo.Controls.ControlsListener;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.util.HoloUI;

/**
 * Is map life-time.
 * 
 * It needs to listen to controls, so might as well make it map life-time
 * 
 * @author Colin Ta
 *
 */
public class StatsPanelUI extends ControlsListener {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Table statPanel;

	private Skin skin;
	private Stage stage;

	private StatLabels statText = new StatLabels();

	public StatsPanelUI(Stage stage, Skin skin) {
		this.skin = skin;
		this.stage = stage;
		create();
	}

	private void create() {
		statPanel = new Table();
		statPanel.padLeft(10).padRight(10);
		statPanel.defaults().minWidth(10).spaceLeft(5).left();
		statPanel.setBackground(HoloUI.getSolidBG(HoloGL.rgb(0, 0, 0, 0.5f)));

		statText.atk = new Label("", skin);
		statText.force = new Label("", skin);
		statText.def = new Label("", skin);
		statText.stab = new Label("", skin);

		createStatPanelRow("atk:", statText.atk);
		createStatPanelRow("force:", statText.force);
		createStatPanelRow("def:", statText.def);
		createStatPanelRow("stab:", statText.stab);

		statPanel.setPosition(5, 275);
		statPanel.left().bottom();
		statPanel.pack();
		statPanel.setVisible(false);
		stage.addActor(statPanel);

	}

	private void createStatPanelRow(String rowName, Label label) {
		statPanel.row();
		statPanel.add(new Label(rowName, skin));
		statPanel.add(label);
	}

	@Override
	public void unitSelectionModified(List<UnitInfo> selectedUnits) {
		logger.debug("update called");
		if (selectedUnits.size() == 1) {
			statPanel.setVisible(true);
			update(selectedUnits.get(0).getStats());
		} else {
			statPanel.setVisible(false);
		}

	}

	/**
	 * Unit must not be null
	 */
	private void update(UnitStatsInfo unit) {
		// ((UnitStats) unit).printInfo();
		statText.atk.setText(unit.getAtk());
		statText.force.setText(unit.getForce());
		statText.def.setText(unit.getDef());
		statText.stab.setText(unit.getStab());
	}

	private static class StatLabels {
		Label atk;
		Label force;
		Label def;
		Label stab;
	}

	public void remove() {
		statPanel.remove();
	}

}
