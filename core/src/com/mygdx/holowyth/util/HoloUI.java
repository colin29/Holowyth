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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class HoloUI {

	public interface VoidInterface {
		public void run();
	}

	public static void addJapaneseCharacters(FreeTypeFontParameter parameter) {
		parameter.characters += "‚Ÿ‚ ‚¡‚¢‚£‚¤‚¥‚¦‚§‚¨‚©‚ª‚«‚¬‚­‚®‚¯‚°‚±‚²‚³‚´‚µ‚¶‚·‚¸‚¹‚º‚»‚¼‚½‚¾‚¿‚À‚Á‚Â‚Ã‚Ä‚Å‚Æ‚Ç‚È‚É‚Ê‚Ë‚Ì‚Í‚Î‚Ï‚Ğ‚Ñ‚Ò‚Ó‚Ô‚Õ‚Ö‚×‚Ø‚Ù‚Ú‚Û‚Ü‚İ‚Ş‚ß‚à‚á‚â‚ã‚ä‚å‚æ‚ç‚è‚é‚ê‚ë‚ì‚í‚î‚ï‚ğ‚ñƒ”ƒ@ƒAƒBƒCƒDƒEƒFƒGƒHƒIƒJƒKƒLƒMƒNƒOƒPƒQƒRƒSƒTƒUƒVƒWƒXƒYƒZƒ[ƒ\ƒ]ƒ^ƒ_ƒ`ƒaƒbƒcƒdƒeƒfƒgƒhƒiƒjƒkƒlƒmƒnƒoƒpƒqƒrƒsƒtƒuƒvƒwƒxƒyƒzƒ{ƒ|ƒ}ƒ~ƒ€ƒƒ‚ƒƒƒ„ƒ…ƒ†ƒ‡ƒˆƒ‰ƒŠƒ‹ƒŒƒƒƒƒƒ‘ƒ’ƒ“ƒ”ƒ•ƒ–EJK[RSUJK[°“úˆê‘‰ïl”N‘å\“ñ–{’†’·oO“¯­–©sĞŒ©Œ•ª‹cŒã‘O–¯¶˜AŒÜ”­ŠÔ‘Îã•”“ŒÒ“}’n‡s‹Æ“à‘Š•ûl’è¡‰ñVê‹àˆõ‹ã“ü‘I—§ŠJè•Ä—ÍŠw–â‚‘ã–¾À‰~ŠÖŒˆq“®‹‘S–Ú•\íŒo’ÊŠOÅŒ¾Œ»—’²‘Ì‰»“c“–”ª˜Z–ñå‘è‰ºñˆÓ–@•s—ˆì«“I—v—p§¡“x–±‹­‹C¬µ¬ŠúŒö–ì‹¦æ“s˜a“ˆÈ‹@•½‘‰ÁRv‰Æ˜b¢ó‹æ—Ì‘½Œ§‘±i³ˆÀİ•Û‰ü”‹L‰@—‰–kŒßwŒ SŠEx‘æYŒ‹•S”h“_‹³•ñÏ‘•{ŠˆŒ´æ‹¤“¾‰ğ–¼Œğ‘—\ìŒüÛ¸Ÿ–ÊˆÏŒR•¶”½Œ³d‹ßçl”»”F‰æŠCQ”„—˜‘g’mˆÄ“¹MôWİŒ’c•Ê•¨‘¤”Cˆøg‹ŠŸ…”¼•iğ˜_Œv€Š¯‘ŒWŠ´“Áî“Š¦•Ï‘Å’jŠî„Šen“‡’¼—¼’©Šv‰¿®Šm‘º’ñ‰^I‹“‰Ê¼¨Œ¸‘äL—e•K‰‰‰“dÎZ‘ˆ’k”\–³ÄˆÊ’uŠé^—¬Ši—L‹^Œû‰ß‹Ç­•úÅŒŸ“¡’¬íZ—¿‘òÙóHŒšŒê‹…‰c‹óEØ“y—^‹}~‘—‰‡‹Ÿ‰Â–ğ\–ØŠ„•·g”ï•t{Ø—Rà“]H”ä“ï–h•âÔ—D•vŒ¤û’fˆä‰½“ìÎ‘«ˆáÁ‹«_”Ô‹KpŒì“W‘Ô“±‘N”õ‘îŠQ”z•›Z‹ğŠ²“ÆŒx‹{‹†ˆçÈ—A–KŠy‹N–œ’…æ“Xqc‘zü—¦•a”_B•º¿”O‘Ò‘°Û‹âˆæ•˜J—á‰q‘R‘’£‰fŒÀeŠzŠÄŠÂŒ±’ÇR¤—t‹`“`“­Œ`Œi—‰¢’SD‘Ş€Ü‘i•Ó‘¢‰p”íŠ”“ª‹Z’á–ˆˆã•œd‹p–¡•‰ŠtŠØ“n¸ˆÚ·OŒÂ–åÊ•]‰Û––çá”]‹Éí”ü‰ª‰e–½ŠÜ•Ÿ‘ —Ê–]¼”ñŒ‚²ŠjŠÏ@®’i‰¡—ZŒ^”’[š“š–é»•[‹µ‰¹\—là`¯’ŒÄÂ’B—Ç‹¿ã‹Ajê„’JŒÃŒój“VŠK’ö–”sŠÇ’l‰Ì”ƒ“Ë•ºÚ¿ŠímŒõ“¢˜Hˆ«‰ÈUè“ÂöÃ×Œø}TÏŠÛ‘¼‹y˜p˜^ˆÈ‹ŒºŒ›‘¾‹´•à—£Šİ‹q•—†Œƒ”Ûüt“EŞ“oŒn”á˜Y•êˆÕŒ’•‰ÎŒË‘¬‘¶‰Ôt”òE‰›Œ”Ô†’P–¿ÀÂ”j•Ò‘{’|œŠ®~’´Ó•À—Ã]‰EC•ß‘àŠëÌDX‹£ŠgŒÌŠÙU‹‹‰®‰î“Ç•ÙªF—F‹êAŒ}‘–”Ì‰€‹ï¶ˆÙ—ğ««HˆöŒ£Œµ”nˆ¤•‹xˆÛ•x•l•ƒˆâ”Ş”Ê–¢—Û–fu–M•‘—Ñ‘•”‰Ä‘f–SŒ€‰ÍŒ­qR—â–Í—Y“K•w“SŠñ‰vŠç‹Ù—Ş™—]‹Öˆó‹t‰¤•Ô•WŠ·‹v’Z–ûÈ–\—Öèé”wº”pA”Mh–òˆÉ]´KŒ¯—Š—»Šo‹g·‘D”{‹Ï‰­“rˆ³Œ|‹–c—Õ“¥‰w”²‰óÂ•ÖL—¯ß’â‹»”š—¤‹ÊŒ¹‹V”g‘náŒp‹Ø‘_‘Ñ‰„‰H“wŒÅ“¬¸‘¥‘’—”ğ•UiN‘ª–L—mÃ‘P‘ß¥ŒúŠì—îˆÍ‘²”——ª³•‚˜f•ö‡‹I’®’E—·â‹‰KŠâ—û‰ŸŒy“|—¹’¡”éŠ³’÷“™‹~·‘w”Å˜V—ßŠp—‘¹–[•å‹È“P— •¥í–§’ë“k‘[•§Ñ’z‰İu¬Ú¸’rw‰ä‹Îˆ×ŒŒ’x—}–‹‹õ‰·Gµ“Ş‹G¢¯‰i‘ğG’˜’¥ŒÉ’eŠ§‘œŒ÷‹’Œ‡X”é‹‘ŒYâ’ê^’Ë’v•øŒJ•”Æ”ö•`•z‹°›—é”Õ‘§‰F€‘r”º‰“—{Œœ–ßŠX‹kŠèŠGŠó‰zŒ_Œf–ôŠü—~’ÉG“@ˆËĞ‰˜kŠÒ–‡‘®ÎŒİ•¡—¶—X‘©’‡‰hD˜g——[Œb”Â—ñ˜I‰«’T“¦ØŠÉßùœËŒX“Í—j—V–À–²ŠªwŠöŒN”R[‰J•ÂÕ•ï’“v­ã‹p’[’ÀÜĞŠlŒS•¹‘“Oˆù‹MéÕÅ’DŒÙĞ‰Y•é‘ÖÍ—aÄŠÈ÷Ì“÷”[÷’§Í‘Ÿ—¥—U•´‘İŠ@‘£TT‘¡’qˆ¬Æ’ˆğr‘K”–“°aŒQe”ß•b‘€Œg‰œf‹l‘õ°B’aNŠ‡Š|Ó‘oFh“‹ìQ“§’Ã•Çˆî‰¼ˆÃ—ô•q’¹ƒ¥”Ñ”r—TŒ˜–ó“Åj‹z“T‰êˆµŒÚOŠÅ×‰úƒ—_Š½•×‘tŠ©‘›—‚—z”´b‰õ“ê•Ğ‹½Œh—h–ÆŠù‘E—×”Y‰ØòŒä”Í‰B“~“¿”ç“N‹™™—¢ßŒÈr’™d‘ÃˆĞ‹ŒF•‘Ø”÷—²–„Çb’‰‘q’‹’ƒ•FŠÌ’ŒŠ«‰ˆ–­¥Õ‘Üˆ¢õ½–YPá•MŒP§—”o“¶•ó•¿‹Á–ƒ••‹¹–º»—›‰–_ŒëÜ£ïŠ×ÖŠÑåˆÔŒ«˜’í{˜rŒ“¹|‘¦ô–öÉ‹UŠr”e’›°”¨ŠµÚ–Ñ—Î‘¸’ï‹ºj—ç‘‹_–Î‹]Šø‹—‰ëü–Ô—³Ì”É“a”Z—ƒ‹ˆïŠƒ“G–£Œ™‹›Ä‰t•n•~—iˆßŒ¨Œ——ë_ŒZ”±“{–Å‰j‘b•…‘c—c‹r•H‰×’ª”~”‘s”t–l÷ŠŠŒÇ‰©à†‰Š”…‹åõ|ŠæŠÃb½Ê–€ó—ã‘|‰_Œ@c‹P’~²„”æ‰ÒuÌŠF–C“î•¬’¾ŒÖËµ’’éG´–Â‘j‘×˜d–o“€–x• ‹ei“û‰Œ‰—B–c–î‘Ï—öm˜RgŒc–Ò–F’¦xŒ•˜’Y—x–y²Šû’šûP–°—g–`”V—E‘]ŠB—Ï’Â‰¯•|Œ¢Ø¨ö’¿—œmŠxŠTS•æ–Ù{•Î•µ—‘‹öŒÎ‹·‹i‘ìŠ±’¸’ü‹T—ÆŠ“’” •ë˜F–qêBŠÍ—n”yŒŠŠï–’ß–d’g¹”˜NäzŠ°•¢–E‹ƒ—ÜŠuò•C–v‰É”x‘·’å–õŠÓ”‰A–Á‰s—óqŸºe}’OŒ[–ç‹u“ë–ŸŒº”SŒå•Ü”D“hnŒ¬ˆ®‰¶“Å“«‰“¤‹”Ó‹¶‹©“ÈŠò•ÃˆÜ”|Š’ø‹üŒa’W’Š”â’ì‹Ñy‹”qˆé§–…Zè’_•X‘@‹îŠ£‹•–_Š¦y—ì’ ‰÷—@‹FS‹s–|’ÄÀ˜”ì™“œ“‹o”¯–Z‚–¬‘êE‹O•U–Wá¸•²CŒ~Š¿…‘‘‘ø—‹•Y‰ùŠ¨–ÈÍË‰ûŠ}‘Ê“YŠ¾Š¥Î“º‹¾‘˜QˆŸ——¼’dŒM–‚V‡¼Œ–ä‰µ•±æâ—“ˆíŠU‘ñŠá•r–’}®•Œ’¤ç‰¸Œ°I–µŠ_¢‹\’ŞŠÊ”‹ÏÇŠ‹‰lŒI‹ğö‰Ã‘˜‰ËÂ‹S”§’tŒC› Œ¶Ï•P¾k”c‘H’æ‘a‹Â“İ’p„¾ªÓ—w‰ÅŒª@’Q–“‹ÛŠ™‘ƒ“D•p‹Õ”Ç•£’IŒ‰“É˜Lâ’C‹÷‹ô‰à•š“””h–Œé‘­”™×»’Ò”–n’Á“´—š—ò“ß‰£P•ò—J–p’à©~‰¬“›•@“ˆ‰ö—±Œ”µÄˆÌŒÉŠn‰À“‰–RˆİŠY•‹ŒKŒj‘ŒÕ–~W•ä‘s’ç‹Q–T‰u—İ’s”ÀôW–ü‹Ë¡ŠsŠ÷”A‹¥“f‰ƒ‘é•o—¸•†“©àŠ¶‹E’–h¥–í©‘e’ù‰èK¯P“Ö‹R”J‘óz”E”Ö”L‘Ó”@—¾—S–Q“ƒ•¦‰”ì‹Ã•cbˆ£’µŠD ‰Ù‚ÖŸ–D‘mŠô’­“‚˜jŒà–}Œe“Aˆ°—´•Qa‹±Š ‡ö”Œ–Xù’Š`—Ë–¶°ŒÍ•¾‹ú”Ü”•‰ìt‹‡¶—íˆ»LŠ˜‰xn”›—ï‹X–ÓˆJ‹BŠ‰Œ·›¸–«’‚†^Û–O”Ÿç—Á“ëMŠLé‰Qa•”è’b“ŒÛ—‡Š›•„—P‰òù‹|•¼–Œî˜e’°‘‘…“çœMm”ó—k”°x’Ğ‘‡‹Š—º•­’Ø’{®QŒâŒá’Öã—…–V‹¬•î—Ğ•ôŒ\ø˜@’¢‰³‹ä`“ò•Õät•ğŒOŠ¢—Â—rŒEŠ¼‰{’ãŠ…Š¸”©‘Ùy•®“ØÕ”à—°ÍÁ‹²Ş–A–”ŠS–a¦–b•}‹YŒŞŠõ‘÷–z“l—–Š—vÑ”«‹€Šk‹`Š”Ë¹•ã“Ü”}Œ{‘Tú“·””•y“R‘}Ã—’’Å“å‰‚›IŒ¦”†–U•ˆˆè—Ii”¿‹Å˜hŒ†“í“JŠH‘´—æ“z’NùŒãÄ‘JÙ˜Ú“»“Ä”£Š‰‰|‰´—«”¦zf“‹œŠ¬–|‹á’ü­—ä‘³r‹ª›Á—d”ú”i—ü‘hˆÅ’½–¦•È–ù“Ğ‘¨Ê”TF“Ô’MŠ’–ŠFˆ÷ŠŞ—„“q‹[•»O–rŠÕŒÓ—Hs‘‚£‰ràu› ”Ú•’’–•ˆÑ’Î—ê‰Ğ’±—Œs”Ä ƒ—ÀÀ“H‹D“ä‘ô–¥“½’Ü”m€“ÏŒ®‹İŒu“èÔŠ•‰Ç—®—Ÿ—f•üBŒÆ‰G—•‹¡‘¯ï‰‚‰P”È—É‰SE‹kŸù˜C•O‰‘vì‰‘’F“mŒk‰¥åY—õ‰å‹Ş“µ—N‹Ó—q–JXé°•Ñ¡Ÿ•õ}”Ï”b’õ–ŠN•ì‘Â˜¥‘dw—Å–´Våà‰`œœ˜`–¹•ç•_’¶”ã”ë‹¸–^úŠ@–÷“øƒ”å‰—æà‘Qˆ§‘ú‰LˆÁ‘V‰áˆ¨–ï‘”äİ˜\–Ğéì˜T’„ôaëŠã›Ô‹Ä‰…‹¨‹ø›ßŠd–¤“Ê’¨îˆ¾‰‰C’Ôû‘š–İ˜DÉ‰²”¢’º‹ÚˆÇ‰ŞŠ»ò–PŠ]”ÁˆüàŒd‹_•Dë˜O•jŒÑ‹§”ûŠ¡]›š‹Ôd’X‘ÍŒÏŠŒ‰¨àc’ğ’µŠå’Ï‘‘U’ÙŠ€ğ‰ˆ¼c–¨”dY‰š‰’Ÿf‰±‘yŒÒ“õˆÆ’ÓŠßŠ’ò´ãJ˜B–©–I”¹‘Ç‰Ïà…’’€Ë‹HŠà‰ã‰R×˜U•‡ÙH—©â½LˆÖˆÒ”v“†Œ–—CÓ¾—s‘á¶‹ùˆ­ˆ¥“²”G‘„ªåõ–Ï“Õ’`ãùâ˜³•áŞ\ˆŠğš’‘“‰aâÊ›³ŠŸŠÚçìç¾åô•ù”Ğ‘üˆâµ’h”ŠMœa“£ŠĞŠ~‰\Ò–Äˆ²—Œ‘çÔ‰Nâ”˜n”¶RkŒGŠ¦¬ˆº’@¹ñäï•Gß‹w‰¾ˆÎœ“áÒ¤´Y‹õ–Ë‘Õ‘uêt‘Ä¿èI”Ø”·á‰ ˆ¯âE™÷–G‹À˜c–»Ãˆë—Ú”B‘†‹nåKäPˆòáÁ–Ö”×“ÛŒò‰Oäª˜[•mªŒï’GŒÊŒmáàVŸî—yRˆ½æ]‹é™Í™l”w‹ æÒñŠWˆØAŸŠ—Q‰lr™Ö••V”QŠš‰K–‘•’’AˆìèˆåUŸ´œd“‘’ä‘µãYœpJŠÆŠIˆğåÍät@ˆ©—½–j~ÒâÀá©áSŸ©Ÿ{ú›L•g”k‘pšŒŠäŸ­–á–×”ê“\’³‘âäøáPšb—å—­—W–g•í•É“•’ú÷‰ZãkšM’Æ‘íõšn˜ñ”l“Ñ’ûŒzãó˜S•³’î’İ‘ÈøŒîŒšk˜l–•Ì“Qİ›œA˜m—M‘CÈæX—Ø–š“B’o’KpŒ¥‰Ú";
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

	public static void centerOnStage(Actor actor) {
		actor.setPosition(Gdx.graphics.getWidth() / 2 - actor.getWidth() / 2,
				Gdx.graphics.getHeight() / 2 - actor.getHeight() / 2);
	}
}
