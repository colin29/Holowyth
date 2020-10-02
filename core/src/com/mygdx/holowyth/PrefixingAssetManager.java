package com.mygdx.holowyth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;

public class PrefixingAssetManager extends AssetManager {

	public final String prefixPath;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public PrefixingAssetManager(String prefix) {
		this.prefixPath = prefix;
	}

	// Only need this method, as all overloaded versions delegate to here
	@Override
	public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		logger.debug("Loaded asset: '{}'", prefixPath + fileName);
		super.load(prefixPath + fileName, type, parameter);
	}

	@Override
	public synchronized <T> T get(String fileName) {
		logRequest(fileName);
		if (fileName.startsWith(prefixPath)) {
			return super.get(fileName);
		} else {
			return super.get(prefixPath + fileName);
		}
	}

	@Override
	public synchronized <T> T get(String fileName, Class<T> type) {
		logRequest(fileName);
		if (fileName.startsWith(prefixPath)) {
			return super.get(fileName, type);
		} else {
			return super.get(prefixPath + fileName, type);
		}
	}
	private void logRequest(String fileName) {
		logger.debug("Requested asset: '{}'", prefixPath + fileName);
	}
}
