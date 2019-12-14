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

	private StatLabels simpleStatText;

	private StatLabels baseStatText;
	private StatLabels skillBonusText;
	private StatLabels equipBonusText;
	private StatLabels finalStatText;

	private Table detailedPanel;

	private boolean showDetailedPanel = true;

	public StatsPanelUI(Stage stage, Skin skin) {
		this.skin = skin;
		this.stage = stage;

		simpleStatText = new StatLabels(skin);

		baseStatText = new StatLabels(skin);
		skillBonusText = new StatLabels(skin);
		equipBonusText = new StatLabels(skin);
		finalStatText = new StatLabels(skin);

		create();
	}

	private void create() {

		createSimpleTable();
		createDetailedTable();
	}

	private void createSimpleTable() {

		statPanel = new Table();
		statPanel.padLeft(10).padRight(10);
		statPanel.defaults().minWidth(10).spaceLeft(5).left();
		statPanel.setBackground(HoloUI.getSolidBG(HoloGL.rgb(0, 0, 0, 0.5f)));

		createStatPanelRow("atk:", simpleStatText.atk);
		createStatPanelRow("def:", simpleStatText.def);
		createStatPanelRow("force:", simpleStatText.force);
		createStatPanelRow("stab:", simpleStatText.stab);

		statPanel.setPosition(5, 275);
		statPanel.left().bottom();
		statPanel.pack();
		statPanel.setVisible(false);
		stage.addActor(statPanel);
	}

	private void createStatPanelRow(String rowName, Label total) {
		statPanel.row();
		statPanel.add(new Label(rowName, skin));
		statPanel.add(total);
	}

	private void createDetailedTable() {

		detailedPanel = new Table();
		detailedPanel.padLeft(10).padRight(10);
		detailedPanel.defaults().minWidth(10).spaceLeft(5).left();
		detailedPanel.setBackground(HoloUI.getSolidBG(HoloGL.rgb(0, 0, 0, 0.5f)));

		createDetailedPanelRow("atk:", baseStatText.atk, skillBonusText.atk, equipBonusText.atk, finalStatText.atk);
		createDetailedPanelRow("def:", baseStatText.def, skillBonusText.def, equipBonusText.def, finalStatText.def);
		createDetailedPanelRow("force:", baseStatText.force, skillBonusText.force, equipBonusText.force, finalStatText.force);
		createDetailedPanelRow("stab:", baseStatText.stab, skillBonusText.stab, equipBonusText.stab, finalStatText.stab);

		detailedPanel.setPosition(5, 275);
		detailedPanel.left().bottom();
		detailedPanel.pack();
		detailedPanel.setVisible(false);
		stage.addActor(detailedPanel);
	}

	private void createDetailedPanelRow(String rowName, Label base, Label skill, Label equip, Label total) {
		detailedPanel.row();
		detailedPanel.add(new Label(rowName, skin));
		detailedPanel.add(base);
		detailedPanel.add(skill);
		detailedPanel.add(equip);
		detailedPanel.add(total);

	}

	@Override
	public void unitSelectionModified(List<UnitInfo> selectedUnits) {
		if (selectedUnits.size() == 1) {
			setVisible(true);
			update(selectedUnits.get(0).getStats());
		} else {
			setVisible(false);
		}

	}

	/**
	 * Unit must not be null
	 */
	private void update(UnitStatsInfo unit) {

		// logger.debug("update called:");
		// ((UnitStats) unit).printInfo();

		simpleStatText.atk.setText(unit.getAtk());
		simpleStatText.force.setText(unit.getForce());
		simpleStatText.def.setText(unit.getDef());
		simpleStatText.stab.setText(unit.getStab());

		// Update detailed table
		var base = unit.getBaseStats();

		baseStatText.atk.setText(base.atk);
		baseStatText.force.setText(base.force);
		baseStatText.def.setText(base.def);
		baseStatText.stab.setText(base.stab);

		final String PLUS_TEXT = "+ ";
		final String EQUALS_TEXT = "= ";

		var skill = unit.getSkillBonuses();

		skillBonusText.atk.setText(PLUS_TEXT + skill.atk);
		skillBonusText.force.setText(PLUS_TEXT + skill.force);
		skillBonusText.def.setText(PLUS_TEXT + skill.def);
		skillBonusText.stab.setText(PLUS_TEXT + skill.stab);

		var equip = unit.getEquipBonuses();

		equipBonusText.atk.setText(PLUS_TEXT + equip.atk);
		equipBonusText.force.setText(PLUS_TEXT + equip.force);
		equipBonusText.def.setText(PLUS_TEXT + equip.def);
		equipBonusText.stab.setText(PLUS_TEXT + equip.stab);

		finalStatText.atk.setText(EQUALS_TEXT + unit.getAtk());
		finalStatText.force.setText(EQUALS_TEXT + unit.getForce());
		finalStatText.def.setText(EQUALS_TEXT + unit.getDef());
		finalStatText.stab.setText(EQUALS_TEXT + unit.getStab());

		statPanel.pack();
		detailedPanel.pack();
	}

	private static class StatLabels {

		StatLabels(Skin skin) {
			atk = new Label("", skin);
			force = new Label("", skin);
			def = new Label("", skin);
			stab = new Label("", skin);
		}

		Label atk;
		Label force;
		Label def;
		Label stab;
	}

	public void remove() {
		statPanel.remove();
	}

	public void setVisible(boolean value) {
		if (value) {
			statPanel.setVisible(!showDetailedPanel);
			detailedPanel.setVisible(showDetailedPanel);
		} else {
			statPanel.setVisible(false);
			detailedPanel.setVisible(false);
		}
	}

	public boolean isVisible() {
		return statPanel.isVisible() || detailedPanel.isVisible();
	}

	public void toggleDetailedView() {
		showDetailedPanel = !showDetailedPanel;
		setVisible(isVisible());
	}

}
