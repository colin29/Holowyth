package com.mygdx.holowyth.graphics.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.gameScreen.MapInstance;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

public class SkillNameEffect extends Effect {

	private Stage stage;
	private Skin skin;
	private UnitInfo unit;

	private Camera worldCamera;

	public SkillNameEffect(String text, UnitInfo unit, Camera worldCamera, MapInstance world, Stage stage, Skin skin) {
		super(world);
		this.stage = stage;
		this.skin = skin;
		this.textStr = text;
		this.unit = unit;

		this.worldCamera = worldCamera;
	}

	float maxDuration = 50;
	float framesElapsed = 0;
	private String textStr;
	private Label skillText;

	@Override
	public void begin() {
		skillText = new Label(textStr, skin);

		stage.addActor(skillText);
		updateTextLocation();
	}

	@Override
	public void tick() {
		if (framesElapsed >= maxDuration) {
			skillText.remove();
			markAsComplete();
		}
		updateTextLocation();
		framesElapsed += 1;
	}

	private void updateTextLocation() {
		Vector3 worldPos = new Vector3(unit.getX(), unit.getY(), 0);
		Vector3 vec = worldCamera.project(worldPos);
		skillText.setPosition(vec.x - skillText.getWidth() / 2, vec.y + 25);
	}

	public void forceEnd() {
		skillText.remove();
		markAsComplete();
	}

}
