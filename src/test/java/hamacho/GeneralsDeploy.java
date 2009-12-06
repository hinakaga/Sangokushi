package hamacho;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.GroupLayout.Alignment;

import junit.framework.AssertionFailedError;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.openqa.selenium.server.SeleniumServer;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;


public class GeneralsDeploy extends SangokushiBase {

	public static int SLEEP_TIME = 60 * 5; //ループを何秒末か
	public static int START_WAIT_TIME = 60 * 60; //一番最初のスリープ
	
	public static final boolean DEBUG = false;


	protected SeleniumServer seleniumServer;

	@Test
	public void start() throws Exception {
		
		while(true) {	
			try {
			selenium = new DefaultSelenium("localhost", 4444, "*safari", "http://mixi.jp/");

			selenium.start();

			top開いてログイン();
			ブラウザ三国志のリンククリックしてワールドが開くまで();
			デッキを表示させる();

			if (DEBUG) SLEEP_TIME = 10;

			int count = 1;
			while(true) {
				//たまに開き直さないと多分だめ
				if (count % 10 == 0) {
					selenium.open("/");
					ブラウザ三国志のリンククリックしてワールドが開くまで();
				}
				レベルが上がった武将のステータス強化を行う();

				傷ついた武将をデッキからファイルに戻す();
				//現状、HPの降順、攻撃力の降順にデッキが見えてる前提でスクリプトを組む
				ファイルからデッキに入れる();

				//出兵画面に
				sleep(10);
				//TODO
				try {
					waitForElementPresent("//a[contains(text(), '出兵')]");
				} catch (Exception e) {
					//なんかここだけエラーになるから、時間があったらデバッグする
					continue;
				}
				selenium.click("//a[contains(text(), '出兵')]");
				waitForElementPresent("id=raid_attack");
				//強襲にする
				//と思ったけど、自分の領地には強襲できないようだ。 selenium.click("id=raid_attack");

				int deployCount = 0;
				while(is派兵可能() && deployCount < 10) {
					武将を派兵可能にセットする();
					int atackPower = 派兵可能な武将の攻撃力を取得();
					for (GeneralsOperationSetting operationSetting : operations) {
						if (operationSetting.isTarget(atackPower)) {
							if (!DEBUG) 武将を自陣に派兵する(operationSetting);
							break;
						}
					}

					デッキを表示させる();
					//出兵画面に
					selenium.click("//a[contains(text(), '出兵')]");
					waitForElementPresent("id=raid_attack");
					
					deployCount++;
				}
				count++;
				sleep(SLEEP_TIME);
			}
		} catch (Exception ee) {
			ee.printStackTrace();
			selenium.close();
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


	private void 武将を自陣に派兵する(GeneralsOperationSetting operationSetting) {
		Point point = operationSetting.getNextTargetPoint();
		selenium.type("name=village_x_value", String.valueOf(point.x));
		selenium.type("name=village_y_value", String.valueOf(point.y));

		selenium.click("name=btn_preview");

		waitForElementPresent("name=btn_send");
		//出兵
		selenium.click("name=btn_send");
	}


	private void 武将を派兵可能にセットする() {
		selenium.click("//input[@name='unit_assign_card_id']");
	}


	private int 派兵可能な武将の攻撃力を取得() {
		
		String statusText = selenium.getText("//input[@name='unit_assign_card_id']/../../td[3]");
		statusText = statusText.substring(statusText.indexOf("攻撃") + 2).split(" ")[0];
		return Integer.valueOf(statusText);
	}


	private boolean is派兵可能() {
		return selenium.isElementPresent("//input[@name='unit_assign_card_id']");
		
	}


	private void ファイルからデッキに入れる() {
		while(selenium.isElementPresent("//img[contains(@src, 'btn_setdeck.gif')]")) {
			selenium.chooseOkOnNextConfirmation();
			selenium.click("//img[contains(@src, 'btn_setdeck.gif')]");
			System.out.println(selenium.getConfirmation());
			sleep(5);
		}
	}


	protected void デッキを表示させる() {
		selenium.click("//a[contains(text(), 'デッキ')]");
		waitForElementPresent("//a[@href='#deckTop']", 30);
	}


	private void 傷ついた武将をデッキからファイルに戻す() {
		//ファイルに戻すボタンがあるってことは傷ついてる武将がいるってことなので戻す（このスクリプトが走ってる間は、デッキには傷ついた武将しかいないはず）
		while(selenium.isElementPresent("//img[contains(@src, 'btn_return.gif')]")) {
			selenium.chooseOkOnNextConfirmation();
			selenium.click("//img[contains(@src, 'btn_return.gif')]");
			System.out.println(selenium.getConfirmation());
			sleep(5);
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
		Point[] targetPoints;
		public GeneralsOperationSetting(Op op, int[] params, Point[] targetPoints) {
			this.op = op;
			this.params = params;
			this.targetPoints = targetPoints;
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

		public Point getNextTargetPoint() {
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
		operations.add(new GeneralsOperationSetting(
				Op.Upper, 
				new int[] { 500 },
				new Point[] {
						new Point(208, -43),
						new Point(207, -42)
						}
				));
		operations.add(new GeneralsOperationSetting(
				Op.Between, 
				new int[] { 500, 0 },
				new Point[] {
						new Point(205, -41),
						new Point(206, -41),
						new Point(204, -41),
						new Point(203, -41),
						}
				));
		
		operations.get(0).getNextTargetPoint();
	}
	
	
//
//	private int 指定したデッキの位置の武将の現在のHPを取得(int position) {
//		
//		String statusHp = selenium.getText("//div[@id='cardListDeck']/form/div["+(position+1)+"]//span[@class='status_hp']");
//		return Integer.valueOf(statusHp.split("/")[0]);
//	}
//
//
//	private int デッキにある武将の枚数取得() {
//		return getXPathCountBySequencial("//div[@id='cardListDeck']/form/div");
//	}
//
//
//	private int getXPathCountBySequencial(String path) {
//		sleep(5);
//		
//		int count = 1;
//		
//		if(!selenium.isElementPresent(path+"["+count+"]")) {
//			return 0;
//		}
//		count++;
//		while(selenium.isElementPresent(path+"["+count+"]")) {
//			count++;
//		}
//		return count - 1;
//	}
//


}
