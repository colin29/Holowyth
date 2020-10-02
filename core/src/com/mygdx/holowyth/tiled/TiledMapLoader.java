package com.mygdx.holowyth.tiled;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.game.base.TiledMapLoadingScreen;
import com.mygdx.holowyth.util.Holo;

/**
 * 
 * @author Colin Ta
 *
 */
public class TiledMapLoader  {

	Logger logger = LoggerFactory.getLogger(TiledMapLoadingScreen.class);

	
	private final Stage stage;
	private final FileChooser fileChooser;
	
	public TiledMapLoader(Stage stage, FileChooser fileChooser) {
		this.stage = stage;
		this.fileChooser = fileChooser;
	}

	public TiledMap getTiledMapFromTMXFile(String pathname) {
		return new MyAtlasTmxMapLoader().load(pathname);
	}
	@SuppressWarnings("unused")
	private void getTiledMapFromTMXFileUsingFileChooser(Consumer<TiledMap> callback, Runnable onCanceled) {
		logger.info("Opening file chooser");
		stage.addActor(fileChooser);
	
		fileChooser.setMode(Mode.OPEN);
		fileChooser.setSelectionMode(SelectionMode.FILES);
		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				logger.debug("Selected file: {}", file.get(0).file().getAbsolutePath());
				callback.accept((getTiledMapFromTMXFile(file.get(0).file().getPath()))); 
			}
			
			@Override
			public void canceled () {
				onCanceled.run();;
			}
		});
	
		fileChooser.setDirectory(Holo.mapsDirectory);
	}
	
}
