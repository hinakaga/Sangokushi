package hamacho;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;



public class ResourceAutoLevelUp extends SangokushiBase {

	public static int SLEEP_TIME = 60 * 1000 * 10; //ループを何秒末か
	public static int BUILD_WAIT_TIME = 60 * 1000 * 50; //建築を待つ
	public static int START_WAIT_TIME = 60 * 1000 * 220; //一番最初のスリープ

	public static final boolean DEBUG = false;

	protected Selenium selenium;
	protected SeleniumServer seleniumServer;

	public static final int MAX_LEVEL = 10;

	Map<ResourceEnum, Integer> minLebelMap = new HashMap<ResourceEnum, Integer>();
	{
		minLebelMap.put(ResourceEnum.木, 1);
		minLebelMap.put(ResourceEnum.石, 1);
		minLebelMap.put(ResourceEnum.鉄, 1);
		minLebelMap.put(ResourceEnum.糧, 1);
	}

	private ResourceEnum nowBuildResource;


	@Test
	public void start() throws Exception {
		if (DEBUG) {
			SLEEP_TIME = 60 * 1000 * 2;
			BUILD_WAIT_TIME = 60 * 1000 * 3;
			START_WAIT_TIME = 60 * 10000 * 0;
		}

		Thread.sleep(START_WAIT_TIME);

		selenium = new DefaultSelenium("localhost", 4444, "*safari", "http://mixi.jp/");
		selenium.start();
		selenium.open("/");

		InputStream is = getClass().getResourceAsStream("login.properties");
		Properties p = new Properties();
		p.load(is);
		is.close();

		selenium.type("name=email", p.getProperty("email"));
		selenium.type("name=password", p.getProperty("password"));
		selenium.click("xpath=//p[@class='loginButton']/input");
		selenium.waitForPageToLoad("5000");

		// selenium.open("http://mixi.jp/run_appli.pl?id=6598");
		waitForElementPresent("//a[contains(text(), 'ブラウザ三国志')]", 30);
		selenium.click("//a[contains(text(), 'ブラウザ三国志')]");
		waitForElementPresent("//a[contains(text(), 'mixi 第9ワールド')]", 30);
		selenium.click("//a[contains(text(), 'mixi 第9ワールド')]");

		int count = 1;

		while(true) {
			if (count % 10 == 0) { //たまにセッションで変になるので、入り直す
				selenium.open("/");
				waitForElementPresent("//a[contains(text(), 'ブラウザ三国志')]", 30);
				selenium.click("//a[contains(text(), 'ブラウザ三国志')]");
				waitForElementPresent("//a[contains(text(), 'mixi 第9ワールド')]", 30);
				selenium.click("//a[contains(text(), 'mixi 第9ワールド')]");
			}
			count++;

			try {
				//伐採所、製鉄所、石切り場 、畑の現在の生産を取得する
				waitForElementPresent("//img[@alt='木']", 30);

				final MinimunResource minimunResource = new MinimunResource();
				minimunResource.minValue = Integer.MAX_VALUE;
				minimunResource.resourceEnum = ResourceEnum.木;

//				ジャッジメントですの_最低資源生産性(ResourceEnum.糧, minimunResource);
				ジャッジメントですの_最低資源生産性(ResourceEnum.木, minimunResource);
				ジャッジメントですの_最低資源生産性(ResourceEnum.石, minimunResource);
				ジャッジメントですの_最低資源生産性(ResourceEnum.鉄, minimunResource);

				//最低の生産性の資源に関して、土地のパスを得る
				final int level = getMostLowLevel(minimunResource.resourceEnum);

				//最低の生産性の最低のレベルの土地をクリック！
				if (DEBUG) {
					selenium.click("//area[@alt='宿舎 LV.1']");
				} else {
					selenium.click(getResourceAreaPath(minimunResource.resourceEnum, level));
				}

				try {
					waitForElementPresent("//div[@class='lvupFacility']/p[@class='main']/a", 10);

					if (!DEBUG) {

						selenium.click("//div[@class='lvupFacility']/p[@class='main']/a");
						//作成しはじめたら、しばらくのあいだその資源のレベルは高いってこととする。
						nowBuildResource = minimunResource.resourceEnum;
						Thread buildWaitThread = new Thread(new Runnable() {
							@Override
							public void run() {
								nowBuildResource = null;
							}
						});
						buildWaitThread.start();

					} else {
						waitForElementPresent("//div[@class='back']//a", 10);
						selenium.click("//div[@class='back']//a");
					}
				} catch (Throwable e) {

					selenium.click("//div[@class='back']//a");
					//資源が足りなくて建築ができない
				}
				Thread.sleep(SLEEP_TIME); //10分
			} catch (Throwable ee) {
				Thread.sleep(SLEEP_TIME); //10分
			}
		}
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

}
