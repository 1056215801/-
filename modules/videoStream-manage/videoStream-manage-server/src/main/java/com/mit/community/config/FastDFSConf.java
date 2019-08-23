package com.mit.community.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
/**
 * 
*  fastdfs�����ļ�
* @author shuyy
* @date 2018��8��14��
 */
@Component
@Slf4j
public class FastDFSConf {
	
	public static String nginxUrl;
	
	static{
		log.info("����fastdfs�����ļ�");
		Properties prop = new Properties();
		InputStream in = FastDFSConf.class.getResourceAsStream("/config/FastDFS/FastDFS.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		nginxUrl = prop.getProperty("FastDFS.nginx");
	}
	
}
