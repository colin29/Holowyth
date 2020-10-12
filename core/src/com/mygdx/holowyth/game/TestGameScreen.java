package com.mygdx.holowyth.game;

import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.unit.Unit;

public class TestGameScreen extends StandardGameScreen {

	public TestGameScreen(Holowyth game) {
		super(game);
		
		vn.startConversation("lecia1.conv", "default");
		vn.show();
		
		vn.setConvoExitListener(()->{
			vn.hide();
		});
	}

	@Override
	public void render(float delta) {
		super.render(delta);
	}


}
