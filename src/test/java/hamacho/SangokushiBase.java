package hamacho;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.thoughtworks.selenium.Selenium;

import junit.framework.AssertionFailedError;

public class SangokushiBase {
	
	protected Selenium selenium;

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

		InputStream is = getClass().getResourceAsStream("login.properties");
		Properties p = new Properties();
		p.load(is);
		is.close();

		selenium.type("name=email", p.getProperty("email"));
		selenium.type("name=password", p.getProperty("password"));
		selenium.click("xpath=//p[@class='loginButton']/input");
		selenium.waitForPageToLoad("5000");
	}
	


	public void waitForElementPresent(String element) {
		this.waitForElementPresent(element, 30);
	}
	public void waitForElementPresent(String element, int waitSecond)  {
		for (int second = 0;; second++) {
			if (second >= waitSecond) throw new TimeOutException("timeout element:"+element);
			try { if (selenium.isElementPresent(element)) break; } catch (Exception e) {}
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
}