package com.mygdx.holowyth.test.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.template.HoloBaseScreen;
import com.mygdx.holowyth.util.tools.TaskTimer;

public class BTreeDemo extends HoloBaseScreen {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Color clearColor = HoloGL.rgb(204, 204, 255); // pale purple

	final TaskTimer timer = new TaskTimer();

	BehaviorTreeLibraryManager treeLibraryManager = BehaviorTreeLibraryManager.getInstance();
	BehaviorTreeLibrary library = treeLibraryManager.getLibrary();

	public BTreeDemo(Holowyth game) {
		super(game);

		timer.start(1000); // 1 sec

		createBTree();

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();

		if (timer.taskReady())
			tick();
	}


	public void tick() {
		logger.debug("tick");
		tree.step();
	}

	BehaviorTree<Dog> tree;
	Dog dog = new Dog();

	private void createBTree() {
		Selector<Dog> selector = new Selector<Dog>();
		selector.addChild(new LogTask());

		tree = new BehaviorTree<Dog>(selector);
		tree.setObject(dog);
	}
}
