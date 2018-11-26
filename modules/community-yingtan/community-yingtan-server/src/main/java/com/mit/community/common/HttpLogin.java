package com.mit.community.common;

import com.mit.community.constants.CommonConstatn;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * java通过httpclient获取cookie模拟登录
 *
 * @author shuyy
 * @date 2018/11/14 15:17
 * @company mitesofor
 */
@Slf4j
@Data
@Component
public class HttpLogin implements CommandLineRunner {

    private String cookie;

    public void login() {
        // 登陆 Url
        String loginUrl = "http://cmp.ishanghome.com/cmp/login";
        // 需登陆后访问的 Url
        HttpClient httpClient = new HttpClient();
        // 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
        PostMethod postMethod = new PostMethod(loginUrl);
        // 设置登陆时要求的信息，用户名和密码
        NameValuePair[] data = {new NameValuePair("username", "mitadmin"),
                new NameValuePair("password", "654321")};
        postMethod.setRequestBody(data);
        try {
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.executeMethod(postMethod);

            // 获得登陆后的 Cookie
            Cookie[] cookies = httpClient.getState().getCookies();
            StringBuilder tmpcookies = new StringBuilder();
            for (Cookie c : cookies) {
                tmpcookies.append(c.toString() + ";");
                System.out.println("cookies = " + c.toString());
            }
            this.cookie = tmpcookies.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 发送post请求
     * @param url 请求url
     * @param data 请求参数
     * @param cookie cookie 可以为空
     * @return java.lang.String
     * @author shuyy
     * @date 2018/11/21 15:15
     * @company mitesofor
    */
    public String post(String url, NameValuePair[] data, String cookie) {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestBody(data);
        if(StringUtils.isNotBlank(cookie)){
            postMethod.setRequestHeader("cookie", cookie);
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        }
        String result = StringUtils.EMPTY;
        try {
            int status = httpClient.executeMethod(postMethod);
            int healthStatus = 200;
            if(status != healthStatus) {
                return CommonConstatn.ERROR;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream()));
            StringBuilder stringBuffer = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                stringBuffer.append(str);
            }
            result = stringBuffer.toString();
        } catch (Exception e){
           log.error("发送post请求错误", e);
        } finally {
            postMethod.releaseConnection();
        }

        return result;
    }

    @Override
    public void run(String... args) throws Exception {
        this.login();
    }
}