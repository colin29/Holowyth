package com.mygdx.holowyth.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Utility methods for creating GUI's in Scene2D
 * 
 * @author Colin Ta
 *
 */
public class HoloUI {

	@FunctionalInterface
	public interface VoidInterface {
		public void run();
	}

	@FunctionalInterface
	public interface FloatConsumer {
		public void accept(Float f);
	}

	@FunctionalInterface
	public interface ChangeEventInterface {
		public void accept(ChangeEvent e, Actor actor);
	}

	public static class PaddingValues {
		public float left, right, bot, top;

		public PaddingValues(float top, float left, float bot, float right) {
			this.top = top;
			this.left = left;
			this.bot = bot;
			this.right = right;
		}
	}

	// Widget Functions
	public static Cell<TextButton> textButton(Table table, String text, Skin skin, PaddingValues padding,
			VoidInterface action) {
		Cell<TextButton> c = textButton(table, text, skin, action);
		pad(c.getActor(), padding);
		return c;
	}

	public static Cell<TextButton> textButton(Table table, String text, TextButtonStyle style, PaddingValues padding,
			VoidInterface action) {
		Cell<TextButton> c = textButton(table, text, style, action);
		pad(c.getActor(), padding);
		return c;
	}

	public static Cell<TextButton> textButton(Table table, String text, Skin skin, PaddingValues padding,
			ChangeEventInterface listener) {
		Cell<TextButton> c = textButton(table, text, skin, listener);
		pad(c.getActor(), padding);
		return c;
	}

	public static Cell<TextButton> textButton(Table table, String text, PaddingValues padding, TextButtonStyle style,
			ChangeEventInterface listener) {
		Cell<TextButton> c = textButton(table, text, style, listener);
		pad(c.getActor(), padding);
		return c;
	}

	// Widget Functions
	public static Cell<TextButton> textButton(Table table, String text, Skin skin, VoidInterface action) {
		return textButton(table, text, skin, new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action.run();
			}
		});
	}

	public static Cell<TextButton> textButton(Table table, String text, TextButtonStyle style, VoidInterface action) {
		return textButton(table, text, style, new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action.run();
			}
		});

	}

	public static Cell<TextButton> textButton(Table table, String text, Skin skin, ChangeEventInterface listener) {
		return textButton(table, text, skin, new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				listener.accept(event, actor);
			}
		});
	}

	public static Cell<TextButton> textButton(Table table, String text, TextButtonStyle style,
			ChangeEventInterface listener) {
		return textButton(table, text, style, new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				listener.accept(event, actor);
			}
		});
	}

	private static Cell<TextButton> textButton(Table table, String text, Skin skin, EventListener listener) {
		return textButton(table, text, skin.get(TextButtonStyle.class), listener);
	}

	private static Cell<TextButton> textButton(Table table, String text, TextButtonStyle style,
			EventListener listener) {
		TextButton button = new TextButton(text, style);
		button.addListener(listener);
		return table.add(button);
	}

	public static void pad(Table table, PaddingValues values) {
		table.pad(values.top, values.left, values.bot, values.right);
	}

	/**
	 * Creates an exit button
	 * 
	 * @param table
	 *            Location to add the button
	 * @param skin
	 * @param parent
	 *            UI element which the button should close
	 * @return The enclosing cell for the new button
	 */
	public static Cell<TextButton> exitButton(Table table, Skin skin, final Actor parent) {
		return textButton(table, "x", skin, new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				parent.remove();
			}
		});
	}

	public static void confirmationDialog(boolean condition, Stage stage, Skin skin, String titleText,
			String contentText, String doAlteredText, String doOriginalText, VoidInterface alteredAction,
			VoidInterface originalAction) {

		if (condition) {
			Dialog dialog = new Dialog(titleText, skin);
			stage.addActor(dialog);

			Table contents = dialog.getContentTable();
			contents.add(new Label(contentText, skin));

			Table buttons = dialog.getButtonTable();
			textButton(buttons, doAlteredText, skin, () -> {
				alteredAction.run();
				dialog.remove();
			});
			textButton(buttons, doOriginalText, skin, () -> {
				originalAction.run();
				dialog.remove();
			});
			textButton(buttons, "Cancel", skin, () -> {
				dialog.remove();
			});
			dialog.pack();
			centerOnStage(dialog);
		} else {
			originalAction.run();
		}

	}

	/**
	 * Creates a parameter slider for quickly adjusting the values of parameters
	 * 
	 * @param action
	 *            This should be a lambda which takes in a float and sets the parameter
	 */
	public static void parameterSlider(float minVal, float maxVal, String parameterName, Table parent, Skin skin,
			FloatConsumer action) {
		Label vLabel = new Label("-", skin);
		Label vName = new Label(parameterName, skin);
		Slider vSlider = new Slider(minVal, maxVal, (maxVal - minVal) / 30, false, skin);
		vSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action.accept(vSlider.getValue());
				vLabel.setText(String.valueOf(vSlider.getValue()));
			}
		});
		parent.add();
		parent.add(vSlider);
		parent.row();
		parent.add(vName, vLabel);
		parent.row();
	}

	public static void centerOnStage(Actor actor) {
		actor.setPosition(Gdx.graphics.getWidth() / 2 - actor.getWidth() / 2,
				Gdx.graphics.getHeight() / 2 - actor.getHeight() / 2);
	}

	/**
	 * Workaround for there being no easy way to draw a background on a label
	 */
	public static void setLabelBackgroundColor(Label label, Color color) {
		LabelStyle style = new LabelStyle(label.getStyle());
		Pixmap labelColor = new Pixmap(1, 1, Pixmap.Format.RGB888);
		labelColor.setColor(color);
		labelColor.fill();
		style.background = new Image(new Texture(labelColor)).getDrawable();
		label.setStyle(style);
	}

	/**
	 * The returned drawable can be used to set a background for a widget, such as a table
	 */
	public static Drawable getSolidBG(Color color) {
		Pixmap labelColor = new Pixmap(1, 1, Pixmap.Format.RGB888);
		labelColor.setColor(color);
		labelColor.fill();
		return new Image(new Texture(labelColor)).getDrawable();
	}
}
