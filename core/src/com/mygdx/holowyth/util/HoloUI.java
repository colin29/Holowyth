package com.mygdx.holowyth.util;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class HoloUI {

	public interface VoidInterface {
		public void run();
	}
	public interface FloatConsumer {
		public void accept(Float f);
	}

	public static void addJapaneseCharacters(FreeTypeFontParameter parameter) {
		parameter.characters += "�����������������������������������������������������������������������ÂĂłƂǂȂɂʂ˂̂͂΂ςЂт҂ӂԂՂւׂ؂قڂۂ܂݂ނ߂�������������������񃔃@�A�B�C�D�E�F�G�H�I�J�K�L�M�N�O�P�Q�R�S�T�U�V�W�X�Y�Z�[�\�]�^�_�`�a�b�c�d�e�f�g�h�i�j�k�l�m�n�o�p�q�r�s�t�u�v�w�x�y�z�{�|�}�~�����������������������������������������������E�J�K�[�R�S�U�J�K�[����ꍑ��l�N��\��{�����o�O�����������s�Ќ������c��O�����A�ܔ��ԑΏ㕔���ғ}�n���s�Ɠ������l�荡��V���������I���J��ė͊w�⍂�㖾���~�֌��q�����S�ڕ\��o�ʊO�Ō����������̉��c�����Z���艺��Ӗ@�s���쐫�I�v�p�����x�����C�������������싦��s�a���ȋ@�������R�v�Ƙb�����̑������i�����ݕۉ����L�@�����k�ߎw���S�E�x��Y���S�h�_����Ϗ��{�����拤���𖼌����\����ۍ����ʈύ��R�������d�ߐ�l���F��C�Q�����g�m�ē��M��W�݌��c�ʕ����C���g�����������i��_�v�������W��������ϑŒj��e�n���������v�����m����^�I���ʐ�������L�e�K�����d�ΏZ���k�\���Ĉʒu��^���i�L�^���ߋǏ����Ō�������Z����ُ�H���ꋅ�c��E�ؓy�^�}�~���������\�؊����g��t�{�ؗR���]�H���h��ԗD�v�����f�䉽��Α�������_�ԋK�p��W�ԓ��N����Q�z���Z�������ƌx�{����ȗA�K�y�N������X�q�c�z�����a�_�B�������O�Ҏ����ۋ�揕�J��q�R�����f���e�z�Ċ��ǐR���t�`�`���`�i�����S�D�ޏ��ܑi�ӑ��p�튔���Z�ᖈ�㕜�d���p�����t�ؓn���ڍ��O��ʕ]�ۖ����]�Ɏ�����e���ܕ����ʖ]���񌂍��j�ώ@���i���Z�^���[�����鐻�[�����\�l���`�����ďB�ǋ���A�j�ꐄ�J�Ì�j�V�K�����s�ǒl�̔��˕��ڐ���m�����H���ȍU����Í׌��}�T�ϊۑ��y�p�^���ȋ��������������݋q�������ێ��t�E�ޓo�n��Y��Ռ����Όˑ����ԏt��E�����ԍ��P�����j�ґ{�|�����~���ӕ��Ï]�E�C�ߑ���̐D�X���g�ِ̊U������Ǖٍ��F�F��A�}���̉���ٗ������H�������n�����x�ەx�l����ޔʖ��ۖf�u�M���ё����đf�S���͌��q�R��͗Y�K�w�S��v����ٗގ��]�ֈ�t���ԕW���v�Z���Ȗ\�֐��w���p�A�M�h��ɍ]���K�������o�g���D�{�ω��r���|���c�Փ��w������֐L���ߒ⋻�����ʌ��V�g�n��p�ؑ_�щ��H�w�œ����������𕁎U�i�N���L�m�ÑP�ߍ������͑����������f�����I���E���⋉�K������y�|�������銳�����~���w�ŘV�ߊp�����[��ȓP�����햧��k�[���ђz�ݎu���ڏ��r�w��Έ׌��x�}���������G���ދG�������i���G�������ɒe���������������X�鋑�Y�⍏��^�˒v���J���Ɣ��`�z������Ց��F���r�����{���ߊX���k��G��z�_�f����~�ɐG�@�ːЉ��k�Җ����Όݕ����X�����h�D�g���[�b��I���T���؊ɐߎ����ˌX�͗j�V�������w���N�R�[�J���Օ�v����p�[���܏Њl�S�����O���M��ՏŒD�ٍЉY��֐͗a�Ċȏ��̓��[�����͑����U���ݎ��@���T�T���q���ƒ����r�K�����a�Q�e�ߕb���g���f�l�����B�a�N���|�ӑo�F�h����Q���Õǈ�×��q�������єr�T���󓐎ōj�z�T�ꈵ�ڍO�ŏ׉����_���בt�������z���b����Ћ��h�h�Ɗ��E�הY�ؐ��͉B�~����N�������ߌȍr���d�ÈЍ��F���ؔ������ǎb���q�����F�̒����������Ց܈������Y�P��M���P�����o���󕿋����������������_��ܐ���׍֊ѐ�Ԍ�����{�r�����|�������ɋU�r�e���������ږїΑ���j�瑋�_�΋]��������ԗ����̔ɓa�Z������G�������ĉt�n�~�i�ߌ�����_�Z���{�ŉj�b���c�c�r�H�ג��~���s�t�l�����ǉ�����������|��Ðb���ʖ����|�_�@�c�P�~������ҏu�̊F�C����֏ː�����G���j�טd�o���x���e�i�������B�c��ϗ��m�R�g�c�ҖF���x�����Y�x�y���������P���g�`�V�E�]�B�ϒ��|���؎��������m���x�T�S��ِ{�Ε������Ύ����i�슱�������T�Ɗ�������F�q��B�͗n�y����ߖd�g�����N��z�����E���܊u��C�v�ɔx������ӎ��A���s����q���e�}�O�[��u���떟���S��ܔD�h�n�������œ��������Ӌ����Ȋ�Èܔ|�������a�W�����яy���q�鏧���Z��_�X�@����_���y�쒠���@�F�S�s�|�ď����쏙�����o���Z������E�O�U�Wḕ��C�~�����������Y�����ȍ͍ˉ��}�ʓY�����Γ������Q�������d�M���V�������䉵���◓��U���r���}�������牸���I���_���\�ފʔ��ϐǊ����l�I�����Ñ��ˎS�����t�C�������ϕP���k�c�H��a�ݒp�������ӗw�Ō��@�Q���ۊ����D�p�ՔǕ��I�����ɘL��C������������h���鑭���׏��Ҕ��n��������߉��P��J�p�����~�����@�����������Ĉ̐��Ɋn�������R�݊Y���K�j���Ֆ~�W��s��Q�T�u�ݒs����W���ː��s���A���f����o�����������E���h���퍩�e����K���P�֋R�J��z�E�֔L�Ӕ@���S�Q��������Õc�b�����D���ِ��֐��D�m�������j���}�e�A�����Q�a�����������X�����`�˖����͕����ܔ���t�����툻�L���x�n����X�Ӑ��J�B���������������^�ۖO���������M�L��Q�a����b���ۗ������P����|������e�������玜�M�m��k���x�Б��������ؒ{���Q���֐㗅�V����Е�\���@������`��Ս�t���O���r�E���{���㊅�����ٍy���؎Ք����͍����ޖA�����S�a���b�}�Y�ފ����z�l�����v�є����k���`���ˍ���ܔ}�{�T�������y�R�}�×��œ剁���I�����U����I�i���Řh����J�H����z�N�����đJ�َ��ړ��Ĕ����|�������z�f���������|������䑳�r�����d���i���h�Ő������Ȗ��Б��ʔT�F�ԒM�����F���ޗ����q�[���O�r�ՌӗH�s�����r�u���ڕ������ђΗ�В����s�č��������H�D��������ܔm���ό��݌u��Ԋ��Ǘ����f���B�ƉG��������P�ȗɉS�E�k���C�O���v�쉑�F�m�k���Y����ޓ��N�ӗq�J�X鰕я������}�ϔb�����N����d�w�Ŗ��V����`���`����_����닸�^���@�������剗���Q�����L���V�ረ��ݘ\����T����a���ԋĉ������ߊd���ʒ�����C�ԏ����ݘD�ɉ������ڈǉފ���P�]�������d�_���D��O�j�ы������]���Ԑd�X�͌ϊ����c��������ϑ��U���ي��𐉈��c���d�Y�������f���y�ғ��ƒӊߊ����J�B���I���Ǐ����������ˋH����R�טU���َH����⽎L�ֈҔv�����C�ӏ��s�Ꮆ���������G�������ϓՒ`��☎���ގ\���𚒑��a�ʛ��������吝����Б���ⵒh���M�a���Њ~�\�ҖĈ�����ԉN┘n���R�k�G���������@������G�ߋw���Μ��Ґ����Y���ˑՑu���t�č��I�ؔ��በ���E���G���c���È�ڔB���n�K�P�����֔דی�O䪘[�m����G�ʌm��V��y�R���]��͙l���w���ҏ�W�؍A���Q�l�r�֕��V�Q���K�������A����U���d���䑵�Y�p�J�ƊI�����t�@�����j�~������S���{���L�g�k�p�����䟭��ה�\�������P�b�嗭�W�g��ɓ������Z�k�M�Ƒ���n��l�ђ��z��S����ݑȍ���k�l���̓Q�ݍ��A�m�M�C��搞X�ؖ��B�o�K�p����";
	}

	public static TextureRegionDrawable drawableFromTexture(Texture texture) {
		return new TextureRegionDrawable(new TextureRegion(texture));
	}

	public static void printCanonicalPath(String s) {
		File f = new File("");
		try {
			System.out.println(f.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convenience function to make new colors from a RBG tuple 
	 * @return
	 */
	public static Color color(int r, int g, int b) {
		return new Color((float) r / 256, (float) g / 256, (float) b / 256, 1);
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

	private static Cell<TextButton> textButton(Table table, String text, Skin skin, EventListener listener) {
		TextButton button = new TextButton(text, skin);
		button.addListener(listener);
		return table.add(button);
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
	 * @param action This should be a lambda which takes in a float and sets the parameter
	 */
	public static void parameterSlider(float minVal, float maxVal, String parameterName, Table parent, Skin skin, FloatConsumer action){
		Label vLabel = new Label("-", skin);
		Label vName = new Label(parameterName, skin);
		Slider vSlider = new Slider(minVal, maxVal, (maxVal-minVal)/30, false, skin);
		vSlider.addListener(new ChangeListener(){
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
}
