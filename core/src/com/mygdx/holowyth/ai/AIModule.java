package com.mygdx.holowyth.ai;

import java.io.Reader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.StreamUtils;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.ai.btree.enemy.FleeUntilReachLocation;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

/**
 * App life-time component
 * 
 * @author Colin Ta
 *
 */
public class AIModule {

	BehaviorTreeLibraryManager treeLibraryManager = BehaviorTreeLibraryManager.getInstance();
	BehaviorTreeLibrary library = treeLibraryManager.getLibrary();

	public AIModule() {
		parseTestBTreeFromFile();
	}

	public void createTestBTree() {
		Task<UnitOrderable> selector = new FleeUntilReachLocation();
		BehaviorTree<UnitOrderable> tree = new BehaviorTree<UnitOrderable>(selector);
		library.registerArchetypeTree("enemy", tree);
	}

	public void parseTestBTreeFromFile() {
		Reader reader = null;
		try {
			reader = Gdx.files.internal(Holowyth.ASSETS_PATH + "ai/btree/enemy.tree").reader();
			BehaviorTreeParser<UnitOrderable> parser = new BehaviorTreeParser<UnitOrderable>(BehaviorTreeParser.DEBUG_HIGH);
			BehaviorTree<UnitOrderable> tree = parser.parse(reader, null);
			library.registerArchetypeTree("enemy", tree);

		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	public void update(float delta) {
		GdxAI.getTimepiece().update(delta);
	}
}
