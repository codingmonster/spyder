package test.htmlunit;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class BevaSongSpyder {

	private WebClient webClient;
	private HtmlPage page;
	
	private static DB db = null;
	private static DBCollection col = null;
	
	static {
		try {
			MongoClient mc = new MongoClient("127.0.0.1", 27017);
			db = mc.getDB("Media");
			if (!db.isAuthenticated()) {
				db.authenticate("dbimport", "dbimport".toCharArray());
			}
			col = db.getCollection("BevaSong");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void process() {
		webClient = new WebClient();
		try {
			page = webClient.getPage("http://g.beva.com/kan-erge--c10108.html");
			HtmlElement ele = (HtmlElement) page.getByXPath("//*[@id=\"bd\"]/section/article/div[1]/div[1]/div[3]").get(0);
			String tips = ele.getTextContent();
			
			HtmlDivision div_load = (HtmlDivision) page.getByXPath("//*[@id=\"g-js-loadMore\"]").get(0);
			HtmlDivision div_loading = (HtmlDivision) page.getByXPath("//*[@id=\"bd\"]/section/article/div[3]").get(0);
			System.out.println("style : " + div_load.getAttribute("style"));
			while (!div_load.getAttribute("style").equals("display: none;") || !div_loading.getAttribute("style").equals("display: none;")) {
				if (div_loading.getAttribute("style").equals("display: block;")) {
					// 数据正在加载中，需要等待3s
					Thread.sleep(3000);
				} else {
					HtmlElement ele_more = div_load.getElementsByTagName("a").get(0);
					ele_more.click();
				}
				div_load = (HtmlDivision) page.getByXPath("//*[@id=\"g-js-loadMore\"]").get(0);
				div_loading = (HtmlDivision) page.getByXPath("//*[@id=\"bd\"]/section/article/div[3]").get(0);
				System.out.println("style : " + div_load.getAttribute("style"));
			}
			
			List<HtmlDivision> div_data = (List<HtmlDivision>) page.getByXPath("//*[@id=\"g-js-ergeWrapper\"]/div");
			System.out.println(div_data.size());
			List<DBObject> song_list = new ArrayList<>();
			for (HtmlDivision record : div_data) {
				// HtmlElement song = (HtmlElement) record.getByXPath("//div[2]/h4/a").get(0);
				HtmlAnchor song = (HtmlAnchor) record.getElementsByAttribute("div", "class", "cont").get(0).getElementsByTagName("a").get(0);
				String title = song.getAttribute("title");
				String href = song.getAttribute("href");
				System.out.println(title + " : " + href);
				BasicDBObject data = new BasicDBObject("title", title).append("href", href);
				song_list.add(data);
			}
			col.insert(song_list);
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			webClient.close();
		}
	}
	
	public static void main(String[] args) {
		new BevaSongSpyder().process();
	}
}
