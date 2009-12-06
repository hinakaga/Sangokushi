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


public class GeneralsDeploy {

	public static int SLEEP_TIME = 60 * 1000 * 10; //ループを何秒末か
	public static int BUILD_WAIT_TIME = 60 * 1000 * 50; //建築を待つ
	public static int START_WAIT_TIME = 60 * 1000 * 60; //一番最初のスリープ
	
	public static final boolean DEBUG = false;

	protected Selenium selenium;
	protected SeleniumServer seleniumServer;

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
						new Point(207, -42),
						new Point(204, -42)
						}
				));
		operations.add(new GeneralsOperationSetting(
				Op.Between, 
				new int[] { 500, 0 },
				new Point[] {
						new Point(205, -41),
						new Point(203, -42),
						new Point(206, -45)
						}
				));
	}
	
	
	@Test
	public void start() throws Exception {

//		seleniumServer = new SeleniumServer();
//		seleniumServer.start();

		selenium = new DefaultSelenium("localhost", 4444, "*safari", "http://mixi.jp/");

		selenium.start();
		
		top開いてログイン();
		ブラウザ三国志のリンククリックしてワールドが開くまで();
		デッキを表示させる();
		傷ついた武将をデッキからファイルに戻す();
		//現状、HPの降順、攻撃力の降順にデッキが見えてる前提でスクリプトを組む
		ファイルからデッキに入れる();
		
		//出兵画面に
		selenium.click("//a[contains(text(), '出兵')]");
		waitForElementPresent("id=raid_attack", 30);
		//強襲にする
		selenium.click("id=raid_attack");
		System.out.println();
		
		
		
	}

	private void ファイルからデッキに入れる() {
		while(selenium.isElementPresent("//img[contains(@src, 'btn_setdeck.gif')]")) {
			selenium.chooseOkOnNextConfirmation();
			selenium.click("//img[contains(@src, 'btn_setdeck.gif')]");
			System.out.println(selenium.getConfirmation());
			sleep(5);
		}
	}


	private void デッキを表示させる() {
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
	


	private int 指定したデッキの位置の武将の現在のHPを取得(int position) {
		
		String statusHp = selenium.getText("//div[@id='cardListDeck']/form/div["+(position+1)+"]//span[@class='status_hp']");
		return Integer.valueOf(statusHp.split("/")[0]);
	}


	private int デッキにある武将の枚数取得() {
		return getXPathCountBySequencial("//div[@id='cardListDeck']/form/div");
	}


	private int getXPathCountBySequencial(String path) {
		sleep(5);
		
		int count = 1;
		
		if(!selenium.isElementPresent(path+"["+count+"]")) {
			return 0;
		}
		count++;
		while(selenium.isElementPresent(path+"["+count+"]")) {
			count++;
		}
		return count - 1;
	}


	private void sleep(int second) {
		try {
			Thread.sleep(second * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}


	private void ブラウザ三国志のリンククリックしてワールドが開くまで() throws InterruptedException {
		waitForElementPresent("//a[contains(text(), 'ブラウザ三国志')]", 30);
		selenium.click("//a[contains(text(), 'ブラウザ三国志')]");
		
		waitForElementPresent("//iframe[contains(@src, 'gadgets')]", 30);
		
		String url = selenium.getAttribute("//iframe[contains(@src, 'gadgets')]@src");
		selenium.open(url);
		
		waitForElementPresent("//a[contains(text(), 'mixi 第9ワールド')]", 30);
		selenium.click("//a[contains(text(), 'mixi 第9ワールド')]");
		waitForElementPresent("//a[contains(text(), 'デッキ')]", 30);
	}


	private void top開いてログイン() throws IOException {
		selenium.open("/");

		InputStream is = getClass().getResourceAsStream("login.properties");
		Properties p = new Properties();
		p.load(is);
		is.close();

		selenium.type("name=email", p.getProperty("email"));
		selenium.type("name=password", p.getProperty("password"));
		selenium.click("xpath=//p[@class='loginButton']/input");
		selenium.waitForPageToLoad("5000");
	}
	


	public void waitForElementPresent(String element, int waitSecond)  {
		for (int second = 0;; second++) {
			if (second >= waitSecond) fail("timeout");
			try { if (selenium.isElementPresent(element)) break; } catch (Exception e) {}
			try { 
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				throw new RuntimeException();
			}
		}
	}

	public void waitForTextPresent(String pattern, int waitSecond) throws InterruptedException {
		for (int second = 0;; second++) {
			if (second >= waitSecond) fail("timeout");
			try { if (selenium.isTextPresent(pattern)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
	}

	public void fail(String message) {
		throw new AssertionFailedError(message);
	}


	private int getMostLowLevel(ResourceEnum resourceEnum) {

		int level = minLebelMap.get(resourceEnum);
		for (int i = level; i < MAX_LEVEL; i++) {
			for (int second = 0;; second++) {
				if (second >= 3) break;
				try {
					String path = getResourceAreaPath(resourceEnum, i);
					if (selenium.isElementPresent("//area[@alt='"+resourceEnum.resourceAreaNamePrefix+"LV."+i+"']")) {
						minLebelMap.put(resourceEnum, i);
						return i;
					}
					Thread.sleep(1000);
				 } catch (Exception e) {}
			}
		}
		return level;
	}

	private String getResourceAreaPath(ResourceEnum resourceEnum, int level) {
		return "//area[@alt='"+resourceEnum.resourceAreaNamePrefix+"LV."+level+"']";
	}


	private void ジャッジメントですの_最低資源生産性(ResourceEnum resourceEnum,
			MinimunResource minimunResource) {
		if (minimunResource.resourceEnum == nowBuildResource) {
			return;
		}
		 int resourceProductivity = getProductivity(resourceEnum);
		 if (resourceProductivity < minimunResource.minValue) {
			 minimunResource.minValue = resourceProductivity;
			 minimunResource.resourceEnum = resourceEnum;
		 }
	}

	private int getProductivity(ResourceEnum resource) {
		return Integer.parseInt(selenium.getText("//li[contains(text(), '"+resource.name()+"')]").split(" ")[1]);
	}
	
	public static final int MAX_LEVEL = 10;

	private Map<ResourceEnum, Integer> minLebelMap = new HashMap<ResourceEnum, Integer>();
	{
		minLebelMap.put(ResourceEnum.木, 1);
		minLebelMap.put(ResourceEnum.石, 1);
		minLebelMap.put(ResourceEnum.鉄, 1);
		minLebelMap.put(ResourceEnum.糧, 1);
	}

	private ResourceEnum nowBuildResource;
	
	enum ResourceEnum {

		木("伐採所 "), 
		石("石切り場 "),
		鉄("製鉄所 "), 糧("畑 ");

		String resourceAreaNamePrefix;
		ResourceEnum(String resourceAreaNamePrefix){
			this.resourceAreaNamePrefix = resourceAreaNamePrefix;
		}
	}

	class MinimunResource {
		int minValue;
		ResourceEnum resourceEnum;
	}


}
