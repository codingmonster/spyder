package com.data;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerAddressConfig {
	public String MongoDBAddress;
	public int    MongoDBPort;

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

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
