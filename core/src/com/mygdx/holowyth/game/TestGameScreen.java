package com.mygdx.holowyth.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.HomingProjectileMotion;
import com.mygdx.holowyth.graphics.effects.animated.AnimEffectOnFixedPos;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloUI;

public class TestGameScreen extends StandardGameScreen {

	boolean loadVN = false;
	
	public TestGameScreen(Holowyth game) {
		super(game);
		
		if(loadVN) {
			vn.startConversation("lecia1.conv", "default");
			vn.show();
			
			vn.setConvoExitListener(()->{
				vn.hide();
			});	
		}
		
//		HoloUI.parameterSlider(0, 5f, "turnSpeed", root, skin, (value) -> HomingProjectileMotion.turnSpeedDebug = value);
//		HoloUI.parameterSlider(0, 5f, "proj speed", root, skin, (value) -> HomingProjectileMotion.speedDebug = value);
//		HoloUI.parameterSlider(0, 5f, "Unit movespeed", root, skin, (value) -> Holo.defaultUnitMoveSpeed = value);
		
//		
//		Music music = Gdx.audio.newMusic(Gdx.files.internal(Holo.BGM_DISK_PATH + "Peritune_Wonder2.mp3"));
//		music.play();
		
		for(int h=0;h<8;h++) {
			for(int i=0;i<8;i++) {
				var effect = new AnimEffectOnFixedPos(i*70 + 10, 100 + h*70, (h*8+i)+".png", mapInstance, animations);
				effect.loop = true;
				gfx.addGraphicEffect(effect);
			}	
		}
		
		
		
	}

	@Override
	public void render(float delta) {
		super.render(delta);
	}


}
