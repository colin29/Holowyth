package com.mygdx.holowyth.combatDemo.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.unit.UnitInfo;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.debug.DebugStore;
import com.mygdx.holowyth.util.debug.DebugValues;

/**
 * Manages all effects for the game
 * @author Colin Ta
 *
 */
public class EffectsHandler {
	
	Holowyth game;
	OrthographicCamera worldCamera;
	
	SparksEffectHandler sparksManager;
	
	ArrayList<DamageEffect> damageEffects = new ArrayList<DamageEffect>();
	
	public EffectsHandler(Holowyth game, OrthographicCamera camera, DebugStore debugStore){
		this.game = game;
		this.worldCamera = camera;
		
//		DebugValues debugValues = debugStore.registerComponent("Effects");
//		debugValues.add("damageEffect count", () -> damageEffects.size());
		
		sparksManager = new SparksEffectHandler(game, camera, debugStore);
	}
	
	public void renderDamageEffects() {
		game.batch.setProjectionMatrix(worldCamera.combined);
		game.batch.begin();
		
		BitmapFont damageFont = game.damageEffectFont;
		BitmapFont missFont = game.missEffectFont;
		
		BitmapFont font;
		
		for(DamageEffect d: damageEffects) {
			
			if(d instanceof MissEffect) {
				font = missFont;
			}else {
				font = damageFont;
			}
			
			//we want to draw the fonts centered
			GlyphLayout glyphLayout = new GlyphLayout();
			glyphLayout.setText(font, d.text);
			float width = glyphLayout.width;
			float height = glyphLayout.height;
			
				Color color = missFont.getColor();
				color.set(d.color).a = d.getCurrentOpacity();
				font.draw(game.batch, d.text, d.x - width/2, d.y + height/2);
		}
		game.batch.end();
	}
	
	/**
	 * Ticks effects which operate based on the game logic clock. Vfx particle effects may run independently using delta t
	 */
	public void tick(){
		ListIterator<DamageEffect> iter = damageEffects.listIterator();
		while(iter.hasNext()) {
			DamageEffect cur = iter.next();
			cur.tick();
			if(cur.isExpired()) {
				iter.remove();
			}
		}
	}
	
	/**
	 * 
	 * @param damage
	 * @param unit The unit to display the damage over
	 */
	private static float damageEffectVerticalOffset = 5;
	public void makeDamageEffect(float damage, UnitInfo unit) {
		float x = unit.getX();
		float y = unit.getY() + unit.getRadius()/2;
		damageEffects.add(new DamageEffect(DataUtil.getFullyRoundedString(damage), x, y));
	}
	
	/**
	 * 
	 * @param unit The unit that missed
	 */
	public void makeMissEffect(UnitInfo unit) {
		float x = unit.getX();
		float y = unit.getY() + unit.getRadius()/2;
		damageEffects.add(new MissEffect(x, y));
	}

	public void renderBlockEffects(float delta) {
		sparksManager.render();
	}
	
	public void makeBlockEffect(UnitInfo self, UnitInfo enemy) {
		
		float ratio = 0.66f; // how close the effect is drawn to the enemy.
		
		float x = enemy.getX() * ratio + self.getX() * (1-ratio);
		float y = enemy.getY() * ratio + self.getY() * (1-ratio);
		sparksManager.addSparkEffect(x, y);
	}

}
