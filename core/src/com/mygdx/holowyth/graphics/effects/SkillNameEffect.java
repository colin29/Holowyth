package com.mygdx.holowyth.graphics.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

public class SkillNameEffect extends Effect {

	private Stage stage;
	private Skin skin;
	private UnitInfo unit;

	private Camera worldCamera;

	public SkillNameEffect(String text, UnitInfo unit, Camera worldCamera, World world, Stage stage, Skin skin) {
		super(world);
		this.stage = stage;
		this.skin = skin;
		this.textStr = text;
		this.unit = unit;

		this.worldCamera = worldCamera;
	}

	float maxDuration = 100;
	float framesElapsed = 0;
	private String textStr;
	private Label skillText;

	@Override
	public void begin() {
		skillText = new Label(textStr, skin);

		Vector3 worldPos = new Vector3(unit.getX(), unit.getY(), 0);
		Vector3 vec = worldCamera.project(worldPos);
		stage.addActor(skillText);
		skillText.setPosition(vec.x - skillText.getWidth() / 2, vec.y + 25);
	}

	@Override
	public void tick() {
		if (framesElapsed >= maxDuration) {
			skillText.remove();
			markAsComplete();
		}

		framesElapsed += 1;
	}

}
