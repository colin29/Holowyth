package com.mygdx.holowyth;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;

public class PrefixingAssetManager extends AssetManager {

	public final String prefixPath;

	public PrefixingAssetManager(String prefix) {
		this.prefixPath = prefix;
	}

	// Only need to override this method, as all calls funnel into this one
	@Override
	public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		super.load(prefixPath + fileName, type, parameter);
	}

}
