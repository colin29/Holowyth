package com.mygdx.holowyth.graphics.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.graphics.effects.animated.AnimatedEffects;
import com.mygdx.holowyth.graphics.effects.texteffect.DamageEffect;
import com.mygdx.holowyth.graphics.effects.texteffect.SkillNameEffect;
import com.mygdx.holowyth.graphics.effects.texteffect.SkillNameEffects;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * Manages all gfx effects displayed in-game <br>
 * Has map life-time
 * 
 * @author Colin Ta
 *
 */
@NonNullByDefault
public class EffectsHandler {

	private final SpriteBatch batch;
	private final OrthographicCamera worldCamera;

	private final AssetManager assets;
	private final ShapeDrawerPlus shapeDrawer;
	
	private Skin skin;

	private final Stage stage;

	private final SparksEffectHandler sparksManager;
	private final List<DamageEffect> damageEffects = new ArrayList<DamageEffect>();
	private final List<AnimatedEffects> animatedEffects = new ArrayList<>();
	private final SkillNameEffects skillNameEffects = new SkillNameEffects();

	public EffectsHandler(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, OrthographicCamera camera, AssetManager assets, Stage stage, Skin skin, DebugStore debugStore) {
		this.batch = batch;
		this.shapeDrawer = shapeDrawer;
		this.worldCamera = camera;

		this.stage = stage;
		this.skin = skin;
		
		this.assets = assets;

		sparksManager = new SparksEffectHandler(batch, camera, debugStore);
	}
	
	public void addGraphicEffect(AnimatedEffects e) {
		animatedEffects.add(e);
		e.begin();
	}

	public void renderDamageEffects() {
		batch.setProjectionMatrix(worldCamera.combined);
		batch.begin();

		for (DamageEffect d : damageEffects) {
			BitmapFont font = d.font;

			
			GlyphLayout glyphLayout = new GlyphLayout();
			glyphLayout.setText(font, d.text);
			float width = glyphLayout.width;
			float height = glyphLayout.height;

			font.getColor().a = d.getCurrentOpacity();

			font.draw(batch, d.text, d.x - width / 2, d.y + height / 2); // draw fonts centered
		}
		batch.end();
	}
	public void renderAnimatedEffects() {
		for(var effect : animatedEffects) {
			effect.render(batch, shapeDrawer, assets);
		}
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

		skillNameEffects.tick();
		tickAnimatedEffects();
		
	}
	private void tickAnimatedEffects() {
		for(var effect : animatedEffects) {
			effect.tick();
		}
		var iter = animatedEffects.iterator();
		while(iter.hasNext()) {
			if(iter.next().isComplete()) {
				iter.remove();
			}
		}
	}
	

	public void makeSkillNameEffect(String text, UnitInfo unit) {
		var effect = new SkillNameEffect(text, unit, worldCamera, (MapInstance) unit.getMapInstance(), stage, skin);
		effect.begin();
		skillNameEffects.addSkillTextOn(unit, effect);
	}

	public static class DamageEffectParams {
		public boolean useFastEffect;
	}
	
	private static final DamageEffectParams DEFAULT_DAMAGE_EFFECT_PARAMS = new DamageEffectParams();
	public void makeDamageEffect(float damage, UnitInfo unit) {
		makeDamageEffect(damage, unit, DEFAULT_DAMAGE_EFFECT_PARAMS);
	}
	public void makeDamageEffect(float damage, UnitInfo unit, DamageEffectParams params) {
		BitmapFont font = unit.isAPlayerCharacter() ? Holowyth.fonts.alliedDamageEffectFont : Holowyth.fonts.regularDamageEffectFont;
		var effect = new DamageEffect(DataUtil.roundFully(damage), unit.getPos(), font);
		if (params.useFastEffect) {
			effect.setInitialSpeed(3);
			effect.setDuration(80);
			effect.fullOpacityDuration = 50;
		}
		damageEffects.add(effect);
	}
	/**
	 * 
	 * @param unit
	 *            The unit that missed
	 */
	public void makeMissEffect(UnitInfo unit) {
		float x = unit.getX();
		float y = unit.getY() + unit.getRadius() / 2;
		
		var effect= new DamageEffect("Miss", x, y, Color.WHITE, Holowyth.fonts.missEffectFont);
		damageEffects.add(effect);
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
