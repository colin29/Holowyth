package com.mygdx.holowyth.combatDemo.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.holowyth.combatDemo.Controls;
import com.mygdx.holowyth.combatDemo.Controls.ControlsListener;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.HoloUI;
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
		skillBar.pad(20);

		ActiveSkill[] skills = unit.getSkills().getSkillSlots();

		for (int i = 1; i <= 8; i++) {

			// Make button
			var button = new TextButton("(" + i + ")", skin);
			ActiveSkill skill = skills[i];
			skillBar.add(button).size(50);

			if (skill == null) {
				String skillText = "(" + i + ")\n";
				button.setText(skillText);
				button.getLabel().setWrap(true);
				continue;
			} else {
				String skillText = "(" + i + ")\n" + skill.name;
				button.getLabel().setEllipsis("");
				button.setText(skillText);
				button.getCell(button.getLabel()).minSize(0); // minSize(0) is a bug fix to make truncation work
			}

			// Make Hover Panel
			final Table hoverPanel = new Table();
			hoverPanel.defaults().width(200);

			var headerRow = new Label(skill.name + "    " + skill.spCost + " sp", skin);
			headerRow.setWrap(true);
			hoverPanel.add(headerRow);

			hoverPanel.row();
			var description = new Label(skill.getDescription(), skin);
			description.setWrap(true);
			hoverPanel.add(description);

			hoverPanel.pad(4);
			hoverPanel.pack();

			hoverPanel.setBackground(HoloUI.getSolidBG(HoloGL.rgb(0, 0, 0, 0.5f)));

			// stage.setDebugAll(true);
			stage.addActor(hoverPanel);
			hoverPanel.setVisible(false);

			button.addListener(new ClickListener() {
				@Override
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					super.enter(event, x, y, pointer, fromActor);

					Vector2 buttonPos = button.localToStageCoordinates(new Vector2(0, 0));
					hoverPanel.setPosition(buttonPos.x, buttonPos.y + 50);
					hoverPanel.setVisible(true);
				}

				@Override
				public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
					super.exit(event, x, y, pointer, toActor);
					hoverPanel.setVisible(false);
				}

			});

		}

	}

	static class SkillButton extends TextButton {

		public SkillButton(String text, Skin skin, ActiveSkill skill) {
			super(text, skin);

		}

	}

	public void remove() {
		root.remove();
	}

}
