package test.htmlunit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestBaiduJoke {
	private String baseOutPath = "C:/Users/Administrator/Desktop/out/";
	private WebClient webClient;
	private HtmlPage page;

	public void process() throws Exception {
		HtmlElement container = page.getHtmlElementById("1");
		
		HtmlElement nextPage = null;
		HtmlElement typesBox = container.getElementsByAttribute("div", "class", "c-tag-cont").get(0);
		DomNodeList<HtmlElement> types = typesBox.getElementsByTagName("span");
		
		List<HtmlElement> content = null;
		PrintWriter out = null;
		File file = null;
		int page = 0;
		int count = 0;
		String typeName = "";
		for (HtmlElement type : types) {
			type.click();
			typeName = StringUtils.trim(type.asText());
			file = new File(baseOutPath + typeName + ".txt");
			out = new PrintWriter(file, "UTF-8");
			System.out.printf("create file: %s%n", file.getName());
			nextPage = container.getElementsByAttribute("span", "class", "opui-page-next OP_LOG_BTN").get(0);
			while (nextPage.getAttribute("style").equals("visibility:visible;")) {				
				page++;
				content = container.getElementsByAttribute("table", "class", "c-table op-imprecise-main").get(0)
						.getElementsByAttribute("div", "class", "op-imprecise-left");
				for (HtmlElement e : content) {
					count++;
					out.println(e.asText());
				}
				content = null;
				nextPage.click();
				nextPage = container.getElementsByAttribute("span", "class", "opui-page-next OP_LOG_BTN").get(0);
				System.out.printf("[type:%s] process page %d finished.%n", typeName, page);
			}
			System.out.printf("process type:%s finished. count:%d.%n", typeName, count);
			out.printf("%npageCount:%d, sum:%d%n", page, count);
			page = 0;
			count = 0;
			out.close();
		}
		
	}
	
	@Test
	public void process4Excel() throws Exception {
		Pattern p = Pattern.compile("^(.*)(\\s*——.*)$");
		Matcher m = null;
		
		HtmlElement container = page.getHtmlElementById("1");
		HtmlElement nextPage = null;
		HtmlElement typesBox = container.getElementsByAttribute("div", "class", "c-tag-cont").get(0);
		DomNodeList<HtmlElement> types = typesBox.getElementsByTagName("span");
		
		List<HtmlElement> content = null;
		XSSFWorkbook workbook = null;
		FileOutputStream file = null;
		int page = 0;
		int rowid = 0;
		String typeName = "";
		for (HtmlElement type : types) {
			type.click();
			typeName = StringUtils.trim(type.asText());
			
			workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet = workbook.createSheet("sheet");
			XSSFRow row = null;
			
			nextPage = container.getElementsByAttribute("span", "class", "opui-page-next OP_LOG_BTN").get(0);
			while (nextPage.getAttribute("style").equals("visibility:visible;")) {				
				page++;
				content = container.getElementsByAttribute("table", "class", "c-table op-imprecise-main").get(0)
						.getElementsByAttribute("div", "class", "op-imprecise-left");
				for (HtmlElement e : content) {
					row = spreadsheet.createRow(rowid++);
					m = p.matcher(e.asText());
					if (m.matches()) {
						row.createCell(0).setCellValue(m.group(1));
						row.createCell(1).setCellValue(m.group(2));
					}
				}
				content = null;
				nextPage.click();
				nextPage = container.getElementsByAttribute("span", "class", "opui-page-next OP_LOG_BTN").get(0);
				System.out.printf("[type:%s] process page %d finished.%n", typeName, page);
			}
			System.out.printf("process type:%s finished. count:%d.%n", typeName, rowid);
			page = 0;
			rowid = 0;
			file = new FileOutputStream(baseOutPath + typeName + ".xlsx");
			workbook.write(file);
			file.close();
		}
	}
	
	@Before
	public void before() throws Exception {
		webClient = new WebClient();
		page = webClient.getPage(
				"https://www.baidu.com/s?ie=utf-8&f=3&rsv_bp=1&rsv_idx=1&tn=baidu&wd=%E6%9C%80%E6%90%9E%E7%AC%91%E7%9A%84%E8%84%91%E7%AD%8B%E6%80%A5%E8%BD%AC%E5%BC%AF&oq=%E6%9C%80%E6%90%9E%E7%AC%91%E7%9A%84%E8%84%91%E7%AD%8B%E6%80%A5%E8%BD%AC%E5%BC%AF&rsv_pq=c430f4e600006377&rsv_t=ad0cndwTPI%2FHciIbk%2BiIQ6rQzMbyiIG6elezeFYWnDOonJLRskmA7inAZwk&rqlang=cn&rsv_enter=0&prefixsug=%E6%9C%80%E6%90%9E%E7%AC%91%E7%9A%84%E8%84%91%E7%AD%8B%E6%80%A5%E8%BD%AC%E5%BC%AF&rsp=0");
	}
	
	@After
	public void after() {
		webClient.close();
	}

}
