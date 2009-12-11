package hamacho;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.ho.yaml.Yaml;
import org.junit.Before;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

abstract public class SangokushiBase {

	protected Selenium selenium;

	public static int STEP_SLEEP = 5;
	public int MAX_LEVEL = 20;

	protected Map<ResourceEnum, Integer> minLebelMap = new HashMap<ResourceEnum, Integer>();
	{
		initMinLabelMap();
	//	minLebelMap.put(ResourceEnum.糧, 1);
	}

	protected void initMinLabelMap() {
		minLebelMap.put(ResourceEnum.木, 1);
		minLebelMap.put(ResourceEnum.石, 1);
		minLebelMap.put(ResourceEnum.鉄, 1);
		minLebelMap.put(ResourceEnum.糧, 1);
	}
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


	protected BaseSettings baseSettings;
	enum 兵科 {
		弓兵, 槍兵, 騎兵, 歩兵;
	}

	protected <T> T loadProperties(Class<T> clazz, String path) throws IOException {
		return Yaml.loadType(getClass().getResourceAsStream(path), clazz);
	}

	@Before
	public void setUp() throws Exception {
		baseSettings = this.<BaseSettings>loadProperties(BaseSettings.class, "base_settings.yaml");
	}

	protected void sleep(int second) {
		try {
			Thread.sleep(second * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	protected void ブラウザ三国志のリンククリックしてワールドが開くまで() throws InterruptedException {
		waitForElementPresent("//a[contains(text(), 'ブラウザ三国志')]", 30);
		selenium.click("//a[contains(text(), 'ブラウザ三国志')]");

		waitForElementPresent("//iframe[contains(@src, 'gadgets')]", 30);

		String url = selenium.getAttribute("//iframe[contains(@src, 'gadgets')]@src");
		selenium.open(url);

		waitForElementPresent("//a[contains(text(), 'mixi 第9ワールド')]", 30);
		selenium.click("//a[contains(text(), 'mixi 第9ワールド')]");
		waitForElementPresent("//a[contains(text(), 'デッキ')]", 30);
	}


	protected void top開いてログイン() throws IOException {
		selenium.open("/");

		sleep(STEP_SLEEP * 2);

		if (selenium.isElementPresent("name=email")) {
			selenium.type("name=email", baseSettings.email);
			selenium.type("name=password", baseSettings.password);
			selenium.click("xpath=//p[@class='loginButton']/input");
		}
		selenium.waitForPageToLoad("5000");
	}



	public void waitForElementPresent(String element) {
		this.waitForElementPresent(element, 160);
	}
	public void waitForElementPresent(String element, int waitSecond)  {
		for (int second = 0;; second++) {
			if (second >= waitSecond) {
				throw new TimeOutException("timeout element:"+element);
			}
			try { if (selenium.isElementPresent(element)) break; } catch (Exception e) {}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				throw new RuntimeException();
			}
		}
	}

	protected void waitForElementNotPresent(String element) {
		this.waitForElementNotPresent(element, 30);

	}
	protected void waitForElementNotPresent(String element, int waitSecond) {
		for (int second = 0;; second++) {
			if (second >= waitSecond) {
				throw new TimeOutException("timeout element:"+element);
			}
			try { if (!selenium.isElementPresent(element)) break; } catch (Exception e) {}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				throw new RuntimeException();
			}
		}

	}

	class TimeOutException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public TimeOutException(String msg) {
			super(msg);
		}
	}
	public void waitForTextPresent(String pattern, int waitSecond) throws InterruptedException {
		for (int second = 0;; second++) {
			if (second >= waitSecond) throw new TimeOutException("timeout element:"+pattern);
			try { if (selenium.isTextPresent(pattern)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
	}

	public void fail(String message) {
		throw new AssertionFailedError(message);
	}
	protected DefaultSelenium openSelenium() {
		DefaultSelenium defaultSelenium = new DefaultSelenium("localhost", 4444, "*"+baseSettings.browser, "http://mixi.jp/");
		return defaultSelenium;
	}

	protected int getMostLowLevel(ResourceEnum resourceEnum) {

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
	protected String getResourceAreaPath(ResourceEnum resourceEnum, int level) {
		return "//area[@alt='"+resourceEnum.resourceAreaNamePrefix+"LV."+level+"']";
	}

	protected void デッキを表示させる() {
		selenium.click("//a[contains(text(), 'デッキ')]");
		waitForElementPresent("//a[contains(text(), '出兵')]", 60);
	}



	protected void 指定した拠点を操作する(String name) {
		selenium.click("//a[contains(text(), '都市')]");
		waitForElementPresent("//*[@id='soldier']");
		sleep(STEP_SLEEP);


	}
//	protected void 指定拠点を表示して操作する(int x, int y) {
//		selenium.click("//a[contains(text(), '全体地図')]");
//		waitForElementPresent("//*[@id='mapXY']/form/input[1]", 60);
//		selenium.type("//*[@id='mapXY']/form/input[1]", String.valueOf(x));
//		selenium.type("//*[@id='mapXY']/form/input[2]", String.valueOf(y));
//		selenium.click("//*[@id='mapXY']/form/input[3]");
//		waitForElementPresent("//*[@id='mapOverlayMap']/area[61]");
//		selenium.click("//*[@id='mapOverlayMap']/area[61]");
//
//	}

	protected void 拠点を選択して都市を表示させる(String 拠点名) {
		//一回念のため他のタブ開く
		selenium.click("//a[contains(text(), 'デッキ')]");
		sleep(STEP_SLEEP);
		String 拠点locator = "//a[contains(text(), '" + 拠点名 + "')]";
		if (selenium.isElementPresent(拠点locator)) {
			selenium.click(拠点locator);
			sleep(STEP_SLEEP);
			waitForElementNotPresent(拠点locator);
		}
		selenium.click("//a[contains(text(), '都市')]");
		waitForElementPresent("//*[@id='mapOverlayMap']/area[25]");
	}



}
