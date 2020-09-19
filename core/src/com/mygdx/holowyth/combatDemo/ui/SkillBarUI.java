package com.mygdx.holowyth.combatDemo.ui;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.mygdx.holowyth.graphics.Cameras;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitSkills;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.exceptions.HoloException;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * Must have map lifetime, since it depends on Controls
 *
 */

public class SkillBarUI implements ControlsListener {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private Stage stage;
	private Table root;

	private Skin skin;

	private Controls controls;

	// Active state
	/**
	 * If skill bar is active, the unit being represented, otherwise null
	 */
	private Unit unit;
	/**
	 * If skill bar is active, a list of UnitSkills.NUM_SKILLS buttons, otherwise an empty list
	 */
	final List<SkillButton> skillButtons = new ArrayList<SkillButton>();

	final Table skillBar = new Table();

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

		root.add(skillBar);

		controls.addListener(this);
	}

	private void regenerateSkillBar() {
		clearSkillBar();

		skillBar.row().size(50).space(10);
		skillBar.pad(20);
		ActiveSkill[] skills = unit.getSkills().getSkillSlots();

		for (int i = 1; i <= UnitSkills.NUM_SKILL_SLOTS; i++) {
			final int slotNumber = i;
			// Make button
			ActiveSkill skill = skills[i];
			var button = new SkillButton("(" + i + ")", skin, skill);
			skillButtons.add(button);

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
			button.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					controls.orderSelectedUnitToUseSkillInSlot(slotNumber);
				}
			});

			// Make Hover Panel
			final Table hoverPanel = makeHoverPanel(skill);

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

	private Table makeHoverPanel(ActiveSkill skill) {
		var hoverPanel = new Table();
		hoverPanel.defaults().width(240);

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
		return hoverPanel;
	}

	public void update() {
		if (isSkillBarActive()) {
			for (SkillButton button : skillButtons) {
				if (button.skill != null) {
					button.setDisabled(button.skill.isOnCooldown());
				}
			}
		}
	}

	static class SkillButton extends TextButton {

		final ActiveSkill skill;

		public SkillButton(String text, Skin skin, ActiveSkill skill) {
			super(text, skin);
			this.skill = skill;

		}

	}

	/**
	 * Can be called even if skill bar is active
	 */
	private void activateSkillBar(Unit unit) {
		this.unit = unit;
		regenerateSkillBar();
	}

	private void deactivateSkillBar() {
		unit = null;
		clearSkillBar();
	}

	private void clearSkillBar() {
		skillBar.clear();
		skillButtons.clear();
	}

	public SkillButton getSlot(int slotNum) {
		if (!isSkillBarActive())
			throw new HoloException("Skill bar is not active");
		if (slotNum < 1 || slotNum > UnitSkills.NUM_SKILL_SLOTS) {
			throw new HoloIllegalArgumentsException("slot number must be between 1 and " + UnitSkills.NUM_SKILL_SLOTS);
		}
		return skillButtons.get(slotNum - 1);
	}

	public void remove() {
		root.remove();
		controls.removeListener(this);
	}

	public boolean isSkillBarActive() {
		return unit != null;
	}

	private Vector2 temp = new Vector2();

	private Color skillCoolDownOverlayColor = new Color(1, 1, 1, 0.25f);

	public void render(Cameras cameras, SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
		batch.begin();
		batch.setProjectionMatrix(cameras.fixedCamera.combined);

		shapeDrawer.setColor(skillCoolDownOverlayColor);

		for (SkillButton button : skillButtons) {
			var skill = button.skill;
			if (skill != null && skill.isOnCooldown()) {

				float cooldownFractionRemaining = Math.min(1, skill.curCooldown / skill.cooldown);

				temp = button.localToStageCoordinates(temp.setZero());
				shapeDrawer.filledRectangle(temp.x, temp.y, button.getWidth(), button.getHeight() * cooldownFractionRemaining);
			}

		}

		batch.end();
		batch.setProjectionMatrix(cameras.worldCamera.combined);
	}
	
	@Override
	public void unitSelectionModified(List<UnitInfo> selectedUnits) {
		if (selectedUnits.size() == 1) {
			activateSkillBar((Unit) selectedUnits.get(0));
		} else {
			deactivateSkillBar();
		}
	}

}
