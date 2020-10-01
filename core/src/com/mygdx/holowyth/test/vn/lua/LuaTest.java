package com.mygdx.holowyth.test.vn.lua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wizered67.game.scripting.lua.LuaScriptManager;

public class LuaTest {
	
	
	LuaScriptManager lua = new LuaScriptManager();
	
	 private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public LuaTest(){
		lua.setValue("questChoice", "help");
		
		logger.debug("Value of questChoice: '{}'", lua.getStringValue("questChoice"));
		
		lua.load("questChoice = questChoice .. '[added text]'", false).execute();
		
		logger.debug("Value of questChoice: '{}'", lua.getStringValue("questChoice"));
		
		
	}
	
	
	
	public static void main(String[] args) {
		new LuaTest();
	}

}
