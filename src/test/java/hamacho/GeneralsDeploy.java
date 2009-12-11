package hamacho;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;
import org.junit.Test;
import org.openqa.selenium.server.SeleniumServer;


public class GeneralsDeploy extends SangokushiBase {



	public static int SLEEP_TIME = 60 * 20; //ループを何秒するか

	public static int ERROR_MAX = 30;

	public static final boolean DEBUG = false;

	protected SeleniumServer seleniumServer;

	private List<String> excludeGeneralNames;

	//今は前提として、デッキのファイルのソート順が、HPの降順が第一位にきてる必要がある。
	//おすすめは、２番目がLVの降順で、３番目が討伐の降順
	//というのも設定でどうにかできるといいですが、まだまだですね…
	@SuppressWarnings("unchecked")
	@Test
	public void start() throws Exception {
		Map deploySettings = (Map) Yaml.load(getClass().getResourceAsStream("GeneralsDeploy.yaml"));
		excludeGeneralNames = (List<String>) deploySettings.get("excludeGeneralNames");

		int errorCount = 0;

		while(true) {
			try {
			selenium = openSelenium();
			selenium.start();

			top開いてログイン();
			ブラウザ三国志のリンククリックしてワールドが開くまで();

			if (DEBUG) SLEEP_TIME = 10;

			int count = 1;
			while(true) {
				デッキを表示させる();
				try {
				//	拠点を選択して都市を表示させる("清澄白河山城");
				//たまに開き直さないと多分だめ
				if (count % 10 == 0) {
					selenium.open("/");
					ブラウザ三国志のリンククリックしてワールドが開くまで();
				}
				count++;
				レベルが上がった武将のステータス強化を行う();

				if (!DEBUG)傷ついた武将をデッキからファイルに戻す();
				//現状、HPの降順、攻撃力の降順にデッキが見えてる前提でスクリプトを組む
				ファイルからデッキに入れる();



				//出兵画面に
				sleep(STEP_SLEEP);
				//TODO
				waitForElementPresent("//a[contains(text(), '出兵')]", 60);


				selenium.click("//a[contains(text(), '出兵')]");
				waitForElementPresent("id=raid_attack");
				//強襲にする
				//と思ったけど、自分の領地には強襲できないようだ。 selenium.click("id=raid_attack");

				int deployCount = 0;
				while(is派兵可能() && deployCount < 10) {
					武将を派兵可能にセットする();
					GeneralInfo generalInfo = 派兵可能な武将の情報を取得();
					int atackPower = generalInfo.atackPower;
					for (GeneralsOperationSetting operationSetting : operations) {
						if (operationSetting.isTarget(atackPower)) {
							 武将を自陣に派兵する(operationSetting, generalInfo.兵科);
							 deployCount++;
							break;
						}
					}

					デッキを表示させる();
					//出兵画面に
					selenium.click("//a[contains(text(), '出兵')]");
					waitForElementPresent("id=raid_attack");

					deployCount++;
				}
				sleep(SLEEP_TIME);
				} catch (TimeOutException te) {
				te.printStackTrace();
				}
			}

		} catch (Exception ee) {
			ee.printStackTrace();
			selenium.close();
			sleep(300);

			errorCount++;
			if (errorCount > ERROR_MAX) {
				break;
			}
		}
		}
	}


	private void レベルが上がった武将のステータス強化を行う() {
		while(selenium.isElementPresent("//img[@class='levelup']")) {
			selenium.click("//img[@class='levelup']/..");
			waitForElementPresent("//input[@value='+5']");
			selenium.click("//input[@value='+5']");

			sleep(1);
			selenium.chooseOkOnNextConfirmation();
			selenium.click("btn_update");

			waitForElementPresent("//a[@href='#deckTop']", 30);
		}
	}


	private void 武将を自陣に派兵する(GeneralsOperationSetting operationSetting, 兵科 兵科) {
		Point point = operationSetting.getNextTargetPoint(兵科);
		selenium.type("name=village_x_value", String.valueOf(point.x));
		selenium.type("name=village_y_value", String.valueOf(point.y));

		selenium.click("name=btn_preview");

		waitForElementPresent("name=btn_send");

		if (!DEBUG) {
			//出兵
			selenium.click("name=btn_send");
		}

	}


	private void 武将を派兵可能にセットする() {
		selenium.click("//input[@name='unit_assign_card_id']");
	}


	class GeneralInfo {
		String name;
		int atackPower;
		兵科 兵科;

	}

	private GeneralInfo 派兵可能な武将の情報を取得() {
		String generalName = selenium.getText("//input[@name='unit_assign_card_id']/../../td[2]").split("\n")[0];
		String statusText = selenium.getText("//input[@name='unit_assign_card_id']/../../td[3]");
		String 兵科txt = (statusText.substring(statusText.indexOf("兵科：") + 3)).split(" ")[0].replace("\n", "");
		statusText = statusText.substring(statusText.indexOf("攻撃") + 2).split(" ")[0];

		GeneralInfo generalInfo = new GeneralInfo();
		generalInfo.name = generalName;
		generalInfo.atackPower = Integer.valueOf(statusText);
		generalInfo.兵科 = 兵科.valueOf(兵科txt);

		return generalInfo;
	}


	private boolean is派兵可能() {
		return selenium.isElementPresent("//input[@name='unit_assign_card_id']");

	}





	private void ファイルからデッキに入れる() {
		sleep(STEP_SLEEP);
		while(selenium.isElementPresent("//img[contains(@src, 'btn_setdeck.gif')]")) {
			//TODO
			//String hiraName = selenium.getText("//img[contains(@src, 'btn_return.gif')]/../../..//span[@class='name2']");
//			sleep(STEP_SLEEP);
			selenium.chooseOkOnNextConfirmation();
			selenium.click("//img[contains(@src, 'btn_setdeck.gif')]");
			System.out.println(selenium.getConfirmation());
			sleep(STEP_SLEEP);
		}
	}


	private void 傷ついた武将をデッキからファイルに戻す() {
		sleep(STEP_SLEEP);
		//ファイルに戻すボタンがあるってことは傷ついてる武将がいるってことなので戻す（このスクリプトが走ってる間は、デッキには傷ついた武将しかいないはず）
		while(selenium.isElementPresent("//img[contains(@src, 'btn_return.gif')]")) {
			selenium.chooseOkOnNextConfirmation();
			sleep(STEP_SLEEP + 20);
			selenium.click("//img[contains(@src, 'btn_return.gif')]");
			System.out.println(selenium.getConfirmation());
			sleep(STEP_SLEEP + 20);
		}
		//		int generalsCount = デッキにある武将の枚数取得();
//		List<Integer> 傷ついた武将の位置List = new ArrayList<Integer>();
//		for(int i = 0; i < generalsCount; i++) {
//			if (指定したデッキの位置の武将の現在のHPを取得(i) < 100) {
//				傷ついた武将の位置List.add(i);
//			}
//		}
//		//逆側からファイルに戻さないと、位置がずれる
//		Collections.reverse(傷ついた武将の位置List);
//		for (Integer position : 傷ついた武将の位置List) {
//			指定した位置のデッキの武将をファイルに戻す(position);
//		}
	}


	enum Op {
		Upper, Between;
	}

	class GeneralsOperationSetting {
		Op op;
		int[] params;
		Map<兵科, Point[]> targetPointsMap;
		public GeneralsOperationSetting(Op op, int[] params, Map<兵科, Point[]> targetPointsMap) {
			this.op = op;
			this.params = params;
			this.targetPointsMap = targetPointsMap;
		}

		boolean isTarget(int atackPower) {
			if (op == Op.Upper) {
				return atackPower > params[0];
			} else if (op == Op.Between) {
				return params[0] >= atackPower
				       &&
				       params[1] <= atackPower;
			}
			return false;
		}

		public Point getNextTargetPoint(兵科 兵科) {
			Point[] targetPoints = targetPointsMap.get(兵科);
			Point returnValue = targetPoints[0];

			Point[] newTargetPoints = new Point[targetPoints.length];
			int count = 0;
			while(count < targetPoints.length - 1) {
				newTargetPoints[count] = targetPoints[count+1];
				count++;
			}
			newTargetPoints[count] = returnValue;
			targetPoints = newTargetPoints;
			return returnValue;
		}
	}


	private List<GeneralsOperationSetting> operations;

	//TODO 外で設定できるようにしたいね
	{


		operations = new ArrayList<GeneralsOperationSetting>();
		Map<兵科, Point[]> targetPointsMap = null;

		targetPointsMap = new HashMap<兵科, Point[]>();
		targetPointsMap.put(兵科.弓兵, new Point[] {point(208, -43) }); //岩
		targetPointsMap.put(兵科.槍兵, new Point[] {point(207, -42) }); //鉄
		targetPointsMap.put(兵科.騎兵, new Point[] {point(204, -42) }); //森

		targetPointsMap.put(兵科.歩兵, new Point[] {point(204, -42) });

		operations.add(new GeneralsOperationSetting(
				Op.Upper,
				new int[] { 500 },
				targetPointsMap));

		targetPointsMap = new HashMap<兵科, Point[]>();
		targetPointsMap.put(兵科.弓兵, new Point[] {point(206, -40) }); //岩のはずなんだけど、岩1がない
		targetPointsMap.put(兵科.槍兵, new Point[] {point(208, -40) }); //鉄
		targetPointsMap.put(兵科.騎兵, new Point[] {point(209, -43) }); //森

		targetPointsMap.put(兵科.歩兵, new Point[] {point(209, -43) }); //

		operations.add(new GeneralsOperationSetting(
				Op.Between,
				new int[] { 500, 50 },
				targetPointsMap));
	}

	private Point point(int x, int y) {
		return new Point(x, y);
	}




}
