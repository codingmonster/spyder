package com.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;
import org.dom4j.io.SAXReader;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MumayiApp extends TimerTask{
	private Boolean getIncrementData;
	private String URL;
	private String fileName_gz;
	private String fileName_xml;
	private DBCollection appdata;

	public enum DataOperationType {
		INSERT_DATA,
		UPDATE_DATA,
		DELETE_DATA
	};

	DataOperationType OperType = DataOperationType.INSERT_DATA;
	DBObject dataRec = new BasicDBObject();

	public MumayiApp() {
		getIncrementData = false;

	}

	public void DeleteFiles()
	{
		// 删除压缩文件、解压出来的目录和重命名的xml文件
		FileUtil.deleteDir("data2");
		FileUtil.deleteFile(fileName_gz);
		FileUtil.deleteFile(fileName_xml);
	}

	public Boolean ParserXML(String fileName) {
		Mongo m;
		try {
			m = new Mongo(/*ServerAddressConfig.GetInstance().MongoDBAddress*/"10.27.129.70", /*ServerAddressConfig.GetInstance().MongoDBPort*/27017);
			DB db = m.getDB("DBName");
			db.authenticate("user", "passwd".toCharArray());
			appdata = db.getCollection("Collection");
			if (!getIncrementData) {
				if (appdata.count() > 0) {
					appdata.drop();
				}
			}

			SAXReader reader = new SAXReader();
			try {
				Document doc = reader.read(new File(fileName));
				Element element = doc.getRootElement();
				Element childele = element.element("packages");
				@SuppressWarnings("unchecked")
				List<Element> itemele = childele.elements();
				for (Element e : itemele) {
					dataRec = new BasicDBObject();
					// 全量包中，e对应item节点，通过visitor模式去遍历item下的每个节点，
					// 增量包中，e对应updated/delete/item等节点
					e.accept(new MyVisitor());
					switch (OperType) {
					case INSERT_DATA:
						appdata.insert(dataRec);
						break;
					case UPDATE_DATA: {
						if (appdata.find(new BasicDBObject("appID", dataRec.get("appID"))).count() > 0)
						{
							appdata.update(new BasicDBObject("appID", dataRec.get("appID")), dataRec);
						} else {
							appdata.insert(dataRec);
						}
					}
						break;
					case DELETE_DATA: {
						if (appdata.find(new BasicDBObject("appID", dataRec.get("appID"))).count() > 0)
						{
							appdata.remove(dataRec);
						}
					}
						break;
					default:
						break;
					}
					OperType = DataOperationType.INSERT_DATA;
				}

			} catch (DocumentException e) {
				e.printStackTrace();
				return false;
			}

			m.close();
			return true;
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	// 从Http返回的结果存储到gz文件中
	public Boolean file_put_contents(String file_name, InputStream is) {
		File file = new File(file_name);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];

			int len = 0;
			while ((len = is.read(buffer)) != -1) {

				os.write(buffer, 0, len);
			}
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public Boolean Parser() {

		/*
		 * try { java.net.URL urlNet = new java.net.URL(URL); URLConnection
		 * urlConnection = urlNet.openConnection(); InputStream is =
		 * urlConnection.getInputStream(); System.out.println(urlNet.getPath());
		 * } catch (MalformedURLException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		URL = new String();
		String gzpath = new String();
		Date nowDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);

		HttpClient httpclient = new DefaultHttpClient();

		httpclient.getParams().setIntParameter("http.socket.timeout", 60000);

		if (getIncrementData) {
			if (Data.num == 0) {
				Data.num = 1;
				sdf.applyPattern("yyyy");
				Data.year = sdf.format(nowDate);
				sdf.applyPattern("MM");
				Data.month = sdf.format(nowDate);
				sdf.applyPattern("dd");
				Data.day = sdf.format(nowDate);
			}
			else{
				sdf.applyPattern("dd");
				String curDay = sdf.format(nowDate);
				if(!curDay.equals(Data.day))
				{
					Data.num = 1;
					sdf.applyPattern("yyyy");
					Data.year = sdf.format(nowDate);
					sdf.applyPattern("MM");
					Data.month = sdf.format(nowDate);
					sdf.applyPattern("dd");
					Data.day = sdf.format(nowDate);
				}
			}

			//增量包获取地址、增量包名
			String curDaynum = Data.year + Data.month + Data.day + "_" + Data.num;
			URL = "http://localhost/" + Data.year
					+ curDaynum + ".xml.gz";
			fileName_gz = curDaynum + ".xml.gz";
			fileName_xml = + curDaynum + ".xml";
			gzpath = "./data2/"
					+ Data.year + "/" + Data.month + "/" + Data.day + "/"
					+ fileName_gz;
		} else {
			//全量包获取地址、全量包名
			sdf.applyPattern("yyyyMMdd");
			String curDay = sdf.format(nowDate);
			URL = "http://localhost"
					+ curDay + ".xml.gz";
			fileName_gz = curDay + ".xml.gz";
			fileName_xml = curDay + ".xml";
			gzpath = "./data2/"
					+ fileName_gz;
		}
		System.out.println(fileName_xml);
		HttpGet httpget = new HttpGet(URL);
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();

			if (!file_put_contents(fileName_gz, is)) {
				System.out.println("Create gz file failed!");
				return false;
			}
			System.out.println(" is ok");
			String unzipcmd = "\"d:/Program Files (x86)/7-Zip/7z.exe\" x "
					+ fileName_gz + " -aoa";
			// String rename =
			// "ren ./data2/" +
			// fileName + " 123.xml";
			Process process = Runtime.getRuntime().exec(unzipcmd);
			if (0 != process.waitFor()) {
				if (process.exitValue() == 1)
					System.err.println("cmd exec erro");
				return false;
			}
			process.destroy();
			System.out.println("unzip end");
			File filegz = new File(gzpath);
			File filexml = new File(fileName_xml);
			filegz.renameTo(filexml);
			if (!filexml.exists()) {
				System.out.println("Rename file failed!");
				return false;
			}
			/*
			 * try { Runtime.getRuntime().exec(rename); } catch(Exception e) {
			 * e.printStackTrace(); }
			 */
			System.out.println("rename end");

			return true;
		} catch (RuntimeException x) {
		} catch (Error x) {
		} catch (Throwable x) {
		}
		return false;
	}

	public void Process() {

		Mongo m;
		try {
			m = new Mongo(/*ServerAddressConfig.GetInstance().MongoDBAddress*/"10.27.129.70", /*ServerAddressConfig.GetInstance().MongoDBPort*/27017);
			DB db = m.getDB("DBName");
			db.authenticate("user", "passwd".toCharArray());
			appdata = db.getCollection("Collection");
			if (appdata.count() > 0) {
				getIncrementData = true;
			}

			m.close();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileName_gz = new String();
		fileName_xml = new String();
		if (!Parser()) {
			// 解析文件失败，也需要删除已经存在的文件
			DeleteFiles();
			return;
		}
		if(!ParserXML(fileName_xml))
		{
			System.out.println("Parser XML or Operate Mongodb error!");
			DeleteFiles();
			return;
		}

		if (getIncrementData) {
			Data.num++;
			if (Data.num == 25)
				Data.num = 0;
			getIncrementData = false;
		}
		DeleteFiles();
		System.out.println("delete files success!");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Process();
	}

	// dom4j对Visitor模式的支持，可以编写访问某节点（Element/Attribute...）时执行的代码
	class MyVisitor extends VisitorSupport {
		// 访问元素代码
		public void visit(Element element) {
			String tagName = element.getName();
			String text = element.getText();
			if (tagName == "updated") {
				OperType = DataOperationType.UPDATE_DATA;
				return;
			}
			if (tagName == "delete") {
				OperType = DataOperationType.DELETE_DATA;
				return;
			}
			if (tagName == "item") {
				dataRec = new BasicDBObject();
				return;
			}
			if (tagName == "name") {
				dataRec.put(tagName, text);
				String searchName = text.toLowerCase();
				dataRec.put("searchName", searchName);
				return;
			}
			if (tagName == "score" || tagName == "downloadCount" || tagName == "allHits") {
				int intCount = Integer.valueOf(text);
				dataRec.put(tagName, intCount);
				return;
			}
			dataRec.put(tagName, text);

			// 将解析结果打印到文件中
			/*
			 * String output = "Element " + tagName + " = " + parentTagName +
			 * ".addElement(\"" + tagName + "\");\n"; if (text != null &&
			 * text.trim().length() > 0) { output += tagName + ".setText(\"" +
			 * text + "\");\n"; } File file = new File("D://test.txt"); try { if
			 * (!file.exists()) file.createNewFile(); BufferedWriter writer =
			 * new BufferedWriter(new FileWriter(file, true));
			 * writer.write(output); writer.flush(); writer.close(); } catch
			 * (IOException e) { e.printStackTrace(); }
			 */
		}

		// 访问元素属性的代码,可以用来获取packages type
		public void visit(Attribute attr) {
			Element element = attr.getParent();
			String tagName = element.getName();
			String attrName = attr.getName();
			String attrText = attr.getText();
			System.out.println(tagName + ".addAttribute(\"" + attrName
					+ "\", \"" + attrText + "\");");
		}
	}

}
