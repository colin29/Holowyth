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
		parameter.characters += " ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñ@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~EJK[RSUJK[°úêïlNå\ñ{·oO¯­©sÐ©ªcãO¯¶AÜ­ÔÎãÒ}nsÆàûlè¡ñVêàõãüI§JèÄÍwâã¾À~Öq®SÚ\íoÊOÅ¾»²Ì»cªZñåèºñÓ@sì«Ivp§¡x±­C¬µ¬úöì¦æsaÈ@½ÁRvÆb¢óæÌ½§±i³ÀÝÛüL@kßw SExæYSh_³ñÏ{´æ¤¾ð¼ð\ìüÛ¸ÊÏR¶½³dßçl»FæCQgmÄ¹MôWÝcÊ¨¤Cøg¼ið_v¯W´Áî¦ÏÅjîen¼¼©v¿®mºñ^IÊ¼¨¸äLeKdÎZk\³ÄÊué^¬iL^ûßÇ­úÅ¡¬íZ¿òÙóHêcóEØy^}~Âð\Ø·gït{ØRà]HäïhâÔDv¤ûfä½ìÎ«áÁ«_ÔKpìWÔ±NõîQzZð²Æx{çÈAKyNæXqczü¦a_Bº¿OÒ°ÛâæJáqR£fÀezÄÂ±ÇR¤t``­`i¢SDÞÜiÓ¢píªZáãdp¡tØn¸Ú·OÂåÊ]Ûçá]Éíüªe½Ü Ê]¼ñ²jÏ@®i¡Z^[é»[µ¹\là`¯ÄÂBÇ¿ãAjêJÃójVKösÇlÌËºÚ¿ímõ¢H«ÈUèÂöÃ×ø}TÏÛ¼yp^Èº¾´à£ÝqÛütEÞonáYêÕÎË¬¶ÔtòEÔP¿ÀÂjÒ{|®~´ÓÀÃ]ECßàëÌDX£gÌÙU®îÇÙªFFêA}Ìï¶Ùð««Hö£µn¤xÛxlâÞÊ¢ÛfuMÑÄfSÍ­qRâÍYKwSñvçÙÞ]Öót¤ÔW·vZûÈ\ÖèéwºpAMhòÉ]´K¯»og·D{Ï­r³|cÕ¥w²óÂÖL¯ßâ»¤Ê¹VgnápØ_ÑHwÅ¬¸¥ðUiNªLmÃPß¥úìîÍ²ª³föI®E·âKâûy|¹¡é³÷~·wÅVßp¹[åÈP ¥í§ëk[§ÑzÝu¬Ú¸rwäÎ×x}õ·GµÞG¢¯iðG¥Ée§÷XéYâê^ËvøJÆö`z°éÕ§Frº{ßXkèGóz_fôü~ÉG@ËÐkÒ®ÎÝ¡¶X©hDg[bÂñI«T¦ØÉßùËXÍjVÀ²ªwöNR[JÂÕïv­ãp[ÀÜÐlS¹OùMéÕÅDÙÐYéÖÍaÄÈ÷Ì÷[÷§Í¥U´Ý@£TT¡q¬ÆðrK°aQeßbgflõ°BaN|ÓoFhìQ§ÃÇî¼Ãôq¹¥ÑrTóÅjzTêµÚOÅ×ú_½×t©z´bõêÐ½hhÆùE×YØòäÍB~¿çN¢ßÈrdÃÐFØ÷²ÇbqFÌ«­¥ÕÜ¢õ½YPáMP§o¶ó¿Á¹º»_ëÜ£ï×ÖÑåÔ«í{r¹|¦ôöÉUre°¨µÚÑÎ¸ïºjç_Î]øëüÔ³ÌÉaZïG£Ätn~iß¨ë_Z±{ÅjbccrH×ª~stl÷Ç©àåõ|æÃb½Êóã|_@cP~²æÒuÌFCî¬¾ÖËµéG´Âj×dox eiûBcîÏömRgcÒF¦xYxy²ûûP°g`VE]BÏÂ¯|¢Ø¨ö¿mxTSæÙ{ÎµöÎ·iì±¸üTÆ ëFqêBÍnyïßdg¹Näz°¢EÜuòCvÉx·åõÓAÁsóqºe}O[çuëºSåÜDhn¬®¶Å«¤Ó¶©ÈòÃÜ|øüaWâìÑyqé§Zè_X@î£_¦yì ÷@FSs|ÄÀìo¯Z¬êEOUWá¸²C~¿øYù¨ÈÍËû}ÊY¾¥Îº¾Q¼dMV¼äµ±æâíUñár}®¤ç¸°Iµ_¢\ÞÊÏÇlIðöÃËÂS§tC ¶ÏP¾kcHæaÂÝp¾ªÓwÅª@QÛDpÕÇ£IÉLâC÷ôàhé­×»ÒnÁ´òß£PòJpà©~¬@ö±µÄÌÉnÀRÝYKjÕ~WäsçQTuÝsÀôWüË¡s÷A¥féo¸©à¶Eh¥í©eùèK¯PÖRJózEÖLÓ@¾SQ¦ìÃcb£µD ÙÖDmô­jà}eA°´Qa± öXù`Ë¶°Í¾úÜìt¶í»LxnïXÓJB·¸«^ÛOçÁëMLéQaèbÛPòù|¼îe°çMmók°xÐº­Ø{®QâáÖãV¬îÐô\ø@¢³ä`òÕätðO¢ÂrE¼{ã¸©Ùy®ØÕà°ÍÁ²ÞASa¦b}YÞõ÷zlvÑ«k`Ë¹ãÜ}{Tú·yR}ÃÅåI¦UèIi¿ÅhíJH´æzNùãÄJÙÚ»Ä£|´«¦zf¬|áü­ä³rªÁdúiühÅ½¦ÈùÐ¨ÊTFÔMF÷Þq[»OrÕÓHs£ràu ÚÑÎêÐ±sÄ ÀÀHDäô¥½ÜmÏ®ÝuèÔÇ®füBÆG¡¯ïPÈÉSEkùCOvìFmk¥åYõåÞµNÓqJXé°Ñ¡õ}ÏbõNìÂ¥dwÅ´Våà``¹ç_¶ãë¸^ú@÷øåæàQ§úLÁVá¨ïäÝ\ÐéìTôaëãÔÄ¨øßd¤Ê¨î¾CÔûÝDÉ²¢ºÚÇÞ»òP]Áüàd_DëOjÑ§û¡]ÔdXÍÏ¨àcðµåÏUÙð¼c¨dYf±yÒõÆÓßò´ãJB©I¹ÇÏàËHàãR×UÙH©â½LÖÒvCÓ¾sá¶ù­¥²GªåõÏÕ`ãùâ³áÞ\ðaâÊ³Úçìç¾åôùÐüâµhMa£Ð~\ÒÄ²çÔNân¶RkG¦¬º@¹ñäïGßw¾ÎáÒ¤´YõËÕuêtÄ¿èIØ·á ¯âE÷GÀc»ÃëÚBnåKäPòáÁÖ×ÛòOäª[mªïGÊmáàVîyR½æ]éÍlw æÒñWØAQlrÖVQKAìèåU´däµãYpJÆIðåÍät@©½j~ÒâÀá©áS©{úLgkpä­á×ê\³âäøáPbå­WgíÉú÷ZãkMÆíõnñlÑûzãóS³îÝÈøîklÌQÝAmMCÈæXØBoKp¥Ú";
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
