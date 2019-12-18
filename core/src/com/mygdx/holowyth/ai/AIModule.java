package com.mygdx.holowyth.ai;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.mygdx.holowyth.ai.tasks.Flee;
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
		createTestBTree();
	}

	public void createTestBTree() {
		Task<UnitOrderable> selector = new Flee();
		BehaviorTree<UnitOrderable> tree = new BehaviorTree<UnitOrderable>(selector);
		library.registerArchetypeTree("test btree", tree);
	}

}
