package com.mit.community.util;

import org.apache.commons.collections.map.HashedMap;

<<<<<<< HEAD
<<<<<<< HEAD
import java.net.URLEncoder;
import java.util.Map;

public class Test3 {
    public static void main(String[] args) throws Exception{
=======
import java.util.Map;

public class Test3 {
    public static void main(String[] args){
>>>>>>> remotes/origin/newdev
=======
import java.net.URLEncoder;
import java.util.Map;

public class Test3 {
    public static void main(String[] args) throws Exception{
>>>>>>> 575d0536f7a990502d9678f3d35bb9f1fab83d10
       /* Map<String,String> params = new HashedMap();
        params.put("cmd","cardRemove");
        params.put("id","123");
        params.put("cardNum","4170481");
        String result = HttpPostUtil.sendPost1("http://192.168.1.140:28085",params);*/
<<<<<<< HEAD
<<<<<<< HEAD
        /*Map<String,String> params = new HashedMap();
        params.put("cmd","faceRemove");
        params.put("id","123");
        params.put("cardNum","4170481");
        String result = HttpPostUtil.sendPost1("http://192.168.1.140:28085",params);*/
        Map<String,String> params = new HashedMap();
        params.put("cmd","paoMaDeng");
        params.put("textContent", URLEncoder.encode("为什么乱码了呢", "utf-8"));
        params.put("id","1231");
=======
        Map<String,String> params = new HashedMap();
        params.put("cmd","faceRemove");
        params.put("id","123");
        params.put("cardNum","4170481");
>>>>>>> remotes/origin/newdev
=======
        /*Map<String,String> params = new HashedMap();
        params.put("cmd","faceRemove");
        params.put("id","123");
        params.put("cardNum","4170481");
        String result = HttpPostUtil.sendPost1("http://192.168.1.140:28085",params);*/
        Map<String,String> params = new HashedMap();
        params.put("cmd","paoMaDeng");
        params.put("textContent", URLEncoder.encode("为什么乱码了呢", "utf-8"));
        params.put("id","1231");
>>>>>>> 575d0536f7a990502d9678f3d35bb9f1fab83d10
        String result = HttpPostUtil.sendPost1("http://192.168.1.140:28085",params);
    }
}
