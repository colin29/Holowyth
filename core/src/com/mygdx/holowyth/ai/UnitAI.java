package com.mygdx.holowyth.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class UnitAI {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	BehaviorTree<UnitOrderable> btree;

	private final UnitOrderable self;

	public UnitAI(UnitOrderable unit) {
		self = unit;

		if (unit.getSide() == Side.ENEMY) {
			logger.debug(self.getName() + " New btree:" + btree);
			setToTestBTree();
		}

	}

	public void setBTree(String bTreeRef) {
		btree = BehaviorTreeLibraryManager.getInstance().getLibrary().createBehaviorTree(bTreeRef, self);
	}

	public void setToTestBTree() {
		setBTree("test btree");
	}

	public void tick() {
		if (btree != null) {
			btree.step();
		}
	}

}
