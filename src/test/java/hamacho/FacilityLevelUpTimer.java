package hamacho;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;


public class FacilityLevelUpTimer extends SangokushiBase {
	
	public static int SLEEP_TIME = 60 * 5; //ループを何秒するか

	public static final boolean DEBUG = false;

	@Test
	public void start() throws Exception {
		Properties settings = loadProperties(getClass().getName());
		selenium = openSelenium();

		selenium.start();

		top開いてログイン();
		ブラウザ三国志のリンククリックしてワールドが開くまで();
		
		
	}
	
}
