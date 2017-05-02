package com.data;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerAddressConfig {
	public String MongoDBAddress;
	public int    MongoDBPort;
	public String ClawerDBAddress;
	public int    ClawerDBPort;
	public String NewsSolrAddress;
	public String EulerSolrAddress;
	
	private static ServerAddressConfig instance = null;
	
	public static ServerAddressConfig GetInstance()
	{
		if(instance == null)
		{
			instance = new ServerAddressConfig();
		}
		return instance;
	}
	
	public void Init(InputStream in)
	{
        Properties prop = new Properties();   
        try {   
            prop.load(in);   
            MongoDBAddress = prop.getProperty("mongodb.address").trim();   
            MongoDBPort = Integer.parseInt(prop.getProperty("mongodb.port").trim());   
            
            ClawerDBAddress = prop.getProperty("clawerdb.address").trim();   
            ClawerDBPort = Integer.parseInt(prop.getProperty("clawerdb.port").trim());   

            
            NewsSolrAddress = prop.getProperty("NewsSolr.address").trim();   
            EulerSolrAddress = prop.getProperty("EulerSolr.address").trim();   
            in.close();
        } catch (IOException e) {   
            e.printStackTrace();   
        } 
	}
}
