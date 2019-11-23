package com.mygdx.holowyth.combatDemo.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mygdx.holowyth.combatDemo.Controls;
import com.mygdx.holowyth.combatDemo.Controls.ControlsListener;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * Must have map lifetime, since it depends on Controls
 *
 */

public class SkillBarUI {

	private Stage stage;
	private Table root;

	private Skin skin;

	private Controls controls;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Creates a new skillbar UI and adds it to the stage
	 */
	public SkillBarUI(Stage stage, DebugStore debugStore, Skin skin, Controls controls) {
		this.stage = stage;
		this.skin = skin;

		this.controls = controls;

		create();
	}

	private void create() {
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.bottom();

		controls.addListener(new ControlsListener() {
			@Override
			public void unitSelectionModified() {
				root.clear();

				var selectedUnits = controls.getSelectedUnitReadOnly();
				if (selectedUnits.size() == 1) {
					regenerateSkillBar(selectedUnits.get(0));
				}
			}
		});

	}

	/**
	 * Todo: bar should only appear when exactly one unit is selected
	 */
	private void regenerateSkillBar(UnitInfo unit) {
		root.clear();

		var skillBar = new Table();
		root.add(skillBar);

		skillBar.row().size(50).space(10);

		Skill[] skills = unit.getSkills().getSkillSlots();

		for (int i = 1; i <= 8; i++) {
			var button = new TextButton("(" + i + ")", skin);
			skillBar.add(button);

			Skill skill = skills[i];
			if (skill == null)
				continue;
			String skillText = "(" + i + ")\n" + skill.name.substring(0, 5);
			button.setText(skillText);
			button.getLabel().setWrap(true);

		}

		skillBar.pad(20);

	}

	public void remove() {
		root.remove();
	}

}
