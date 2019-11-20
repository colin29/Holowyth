package com.mygdx.holowyth.combatDemo.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mygdx.holowyth.combatDemo.Controls;
import com.mygdx.holowyth.combatDemo.Controls.ControlsListener;
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
				if (controls.getSelectedUnits().size() == 1) {
					regenerateSkillBar();
				}
			}
		});

	}

	/**
	 * Todo: bar should only appear when exactly one unit is selected
	 */
	private void regenerateSkillBar() {
		logger.debug("regenerated skill bar");
		root.clear();

		var skillBar = new Table();
		root.add(skillBar);

		skillBar.row().size(50).space(10);

		for (int i = 1; i <= 5; i++) {
			var button = new TextButton("Skill " + i, skin);
			skillBar.add(button);
		}

		skillBar.pad(20);

	}

	public void remove() {
		root.remove();
	}

}
