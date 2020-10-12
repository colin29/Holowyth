package com.mygdx.holowyth.game;

import com.mygdx.holowyth.Holowyth;

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
		
	}

	@Override
	public void render(float delta) {
		super.render(delta);
	}


}
