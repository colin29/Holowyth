package com.mygdx.holowyth.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task.Status;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class UnitAI {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	BehaviorTree<UnitOrderable> btree;
	
	// Debug
	public String lastRanOrder = "";

	private final UnitOrderable self;

	public UnitAI(UnitOrderable unit) {
		self = unit;

		if (unit.getSide() == Side.ENEMY) {
			setBTree("enemy");
			logger.debug(self.getName() + " New btree:" + btree);
		}
	}

	public void clearMapLifetimeData() {
		// None. We'll keep b-tree for now and see what happens. Only enemies use AI, don't forsee moving enemies between maps atm.
	}
	
	/**
	 * Creates a copy of the given bTree and uses it.
	 */
	public void setBTree(String bTreeRef) {
		btree = BehaviorTreeLibraryManager.getInstance().getLibrary().createBehaviorTree(bTreeRef, self);
	}

	boolean done = false;

	public void tick() {
		if (btree != null && !done) {
			btree.step();
			if (btree.getStatus() == Status.SUCCEEDED)
				done = true;
		}

	}

	/**
	 * Only use for debug
	 * 
	 * @return
	 */
	public BehaviorTree<UnitOrderable> getBTree() {
		return btree;
	}

}
