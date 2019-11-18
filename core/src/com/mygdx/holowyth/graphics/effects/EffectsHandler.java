package com.mygdx.holowyth.graphics.effects;

import java.util.ArrayList;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.effects.DamageEffect.PresetType;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * Manages all effects for the game
 * 
 * @author Colin Ta
 *
 */
public class EffectsHandler {

	Holowyth game;
	OrthographicCamera worldCamera;

	SparksEffectHandler sparksManager;

	ArrayList<DamageEffect> damageEffects = new ArrayList<DamageEffect>();

	public EffectsHandler(Holowyth game, OrthographicCamera camera, DebugStore debugStore) {
		this.game = game;
		this.worldCamera = camera;

		// DebugValues debugValues = debugStore.registerComponent("Effects");
		// debugValues.add("damageEffect count", () -> damageEffects.size());

		sparksManager = new SparksEffectHandler(game, camera, debugStore);
	}

	public void renderDamageEffects() {
		game.batch.setProjectionMatrix(worldCamera.combined);
		game.batch.begin();

		for (DamageEffect d : damageEffects) {
			BitmapFont font;
			if (d instanceof MissEffect) {
				font = Holowyth.fonts.missEffectFont();
			} else {
				if (d.isUsingPreset()) {
					switch (d.presetType) {
					case ENEMY:
						font = Holowyth.fonts.damageEffectRegular();
						break;
					case PLAYER:
						font = Holowyth.fonts.damageEffectPlayer();
						break;
					default:
						throw new IllegalStateException("Unhandled DamageEffect preset");
					}
				} else {
					font = Holowyth.fonts.damageEffectRegular();
					font.setColor(d.color);
				}
			}

			// we want to draw the fonts centered
			GlyphLayout glyphLayout = new GlyphLayout();
			glyphLayout.setText(font, d.text);
			float width = glyphLayout.width;
			float height = glyphLayout.height;

			font.getColor().a = d.getCurrentOpacity();

			font.draw(game.batch, d.text, d.x - width / 2, d.y + height / 2);
		}
		game.batch.end();
	}

	/**
	 * Ticks effects which operate based on the game logic clock. Vfx particle effects may run independently using delta t
	 */
	public void tick() {
		ListIterator<DamageEffect> iter = damageEffects.listIterator();
		while (iter.hasNext()) {
			DamageEffect cur = iter.next();
			cur.tick();
			if (cur.isExpired()) {
				iter.remove();
			}
		}
	}

	/**
	 * 
	 * @param damage
	 * @param unit
	 *            The unit which is taking damage
	 */
	public void makeDamageEffect(float damage, UnitInfo unit) {

		PresetType damageEffectType = unit.isAPlayerCharacter() ? PresetType.PLAYER : PresetType.ENEMY;

		float x = unit.getX();
		float y = unit.getY() + unit.getRadius() / 2;
		damageEffects.add(new DamageEffect(DataUtil.getFullyRoundedString(damage), unit.getPos(), damageEffectType));
	}

	/**
	 * 
	 * @param unit
	 *            The unit that missed
	 */
	public void makeMissEffect(UnitInfo unit) {
		float x = unit.getX();
		float y = unit.getY() + unit.getRadius() / 2;
		damageEffects.add(new MissEffect(x, y));
	}

	public void renderBlockEffects(float delta) {
		sparksManager.render();
	}

	public void makeBlockEffect(UnitInfo self, UnitInfo enemy) {

		float ratio = 0.66f; // how close the effect is drawn to the enemy.

		float x = enemy.getX() * ratio + self.getX() * (1 - ratio);
		float y = enemy.getY() * ratio + self.getY() * (1 - ratio);
		sparksManager.addSparkEffect(x, y);
	}

}
