package hamacho;
import java.util.Map;

import org.ho.yaml.Yaml;
import org.junit.Test;
import org.openqa.selenium.server.SeleniumServer;



public class MostLowLevelResourceLevelup extends SangokushiBase {

	public static int SLEEP_TIME = 60 * 1000 * 5; //ループを何秒末か
	public static int BUILD_WAIT_TIME = 60 * 1000 * 50; //建築を待つ
	public static int START_WAIT_TIME = 60 * 1000 * 0; //一番最初のスリープ

	public static final boolean DEBUG = false;

	protected SeleniumServer seleniumServer;

	public static final int MAX_LEVEL = 10;

	private ResourceEnum nowBuildResource;



	@Test
	public void start() throws Exception {
		Map settings = (Map) Yaml.load(getClass().getResourceAsStream("MostLowLevelResourceLevelup.yaml"));
		String 拠点名 = (String) settings.get("拠点名");
		int errorCount = 0;
		while(true) {
		try {
		if (DEBUG) {
			SLEEP_TIME = 60 * 1000 * 2;
			BUILD_WAIT_TIME = 60 * 1000 * 3;
			START_WAIT_TIME = 60 * 10000 * 0;
		}

		Thread.sleep(START_WAIT_TIME);

		selenium = openSelenium();
		selenium.start();

		top開いてログイン();
		ブラウザ三国志のリンククリックしてワールドが開くまで();

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
				拠点を選択して都市を表示させる(拠点名);
				//伐採所、製鉄所、石切り場 、畑の現在の生産を取得する
				waitForElementPresent("//img[@alt='木']", 30);

				minLebelMap.put(ResourceEnum.鉄 ,getMostLowLevel(ResourceEnum.鉄));
				minLebelMap.put(ResourceEnum.木 ,getMostLowLevel(ResourceEnum.木));
				minLebelMap.put(ResourceEnum.石 ,getMostLowLevel(ResourceEnum.石));

				MinimunResource minimunResource = new MinimunResource();
				minimunResource.resourceEnum = ResourceEnum.木;
				minimunResource.minValue = MAX_LEVEL;
				for (Map.Entry<ResourceEnum, Integer> entry : minLebelMap.entrySet()) {
					if (entry.getValue() < minimunResource.minValue) {
						minimunResource.resourceEnum = entry.getKey();
						minimunResource.minValue = entry.getValue();
					}
				}

				//最低の生産性の最低のレベルの土地をクリック！
				selenium.click(getResourceAreaPath(minimunResource.resourceEnum, minimunResource.minValue));

				try {
					waitForElementPresent("//div[@class='lvupFacility']/p[@class='main']/a", 30);


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

//					} else {
//						waitForElementPresent("//div[@class='back']//a", 10);
//						selenium.click("//div[@class='back']//a");
//					}
				} catch (Throwable e) {

					selenium.click("//div[@class='back']//a");
					//資源が足りなくて建築ができない
				}
				Thread.sleep(SLEEP_TIME); //10分
			} catch (Throwable ee) {
				Thread.sleep(SLEEP_TIME); //10分
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			Thread.sleep(SLEEP_TIME);
			if (errorCount > 10) {
				return;
			}
			errorCount++;
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
