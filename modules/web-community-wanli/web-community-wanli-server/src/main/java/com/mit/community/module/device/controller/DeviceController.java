package com.mit.community.module.device.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.mit.community.constants.RedisConstant;
import com.mit.community.entity.SysUser;
import com.mit.community.entity.UrgentButton;
import com.mit.community.entity.WellShift;
import com.mit.community.model.WarnInfo;
import com.mit.community.population.service.TaskMessageService;
import com.mit.community.service.*;
import com.mit.community.util.CookieUtils;
import com.mit.community.util.Result;
import com.mit.community.util.SmsCommunityAppUtil;
import com.mit.community.util.WebPush;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 设备感知
 *
 * @author xq
 * @date 2019/7/23
 * @company mitesofor
 */
@RestController
@RequestMapping(value = "/deviceController")
@Slf4j
@Api(tags = "设备感知")
public class DeviceController {
    @Autowired
    private DevicePerceptionService devicePerceptionService;
    @Autowired
    private RedisService redisService;

    /*@PostMapping("/wellListPage")
    @ApiOperation(value = "井盖移位分页查询", notes = "传参：")
    public Result listPage(HttpServletRequest request, String deviceNum, Integer swStatus, Integer deviceStatus,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeStart,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeEnd,Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        Page<WellShift> page = devicePerceptionService.getWellShiftPage(communityCode, deviceNum, swStatus, deviceStatus, timeimeStart, timeimeEnd, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/smokeListPage")
    @ApiOperation(value = "烟感分页查询", notes = "传参：")
    public Result smokeListPage(HttpServletRequest request, String deviceNum, Integer swStatus, Integer deviceStatus,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeStart,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeEnd,Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        Page<WellShift> page = devicePerceptionService.smokeListPage(communityCode, deviceNum, swStatus, deviceStatus, timeimeStart, timeimeEnd, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/dcListPage")
    @ApiOperation(value = "地磁分页查询", notes = "传参：") //没有表
    public Result dcListPage(HttpServletRequest request, String deviceNum, Integer swStatus, Integer deviceStatus,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeStart,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeEnd,Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        Page<WellShift> page = devicePerceptionService.dcListPage(communityCode, deviceNum, swStatus, deviceStatus, timeimeStart, timeimeEnd, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/urgentButtonListPage")
    @ApiOperation(value = "紧急按钮分页查询", notes = "传参：") //没有表
    public Result urgentButtonListPage(HttpServletRequest request, String name, String phone, Integer deviceStatus,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeStart,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeimeEnd,Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        Page<UrgentButton> page = devicePerceptionService.urgentButtonListPage(communityCode, name, phone, deviceStatus, timeimeStart, timeimeEnd, pageNum, pageSize);
        return Result.success(page);
    }
*/
    /**
     * 事件
     */

    @PostMapping("/wellListPage")
    @ApiOperation(value = "井盖位移分页查询", notes = "传参：") //没有表
    public Result wellListPage(HttpServletRequest request, Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        String problem = "井盖非法位移";
        Page<WarnInfo> page = devicePerceptionService.wellListPage(communityCode, problem, pageNum, pageSize);
        List<WarnInfo> list = page.getRecords();
        if (!list.isEmpty()){
            for (int i=0; i<list.size(); i++) {
                list.get(i).setDeviceName("井盖设备");
                list.get(i).setDeviceType("NB-IOT");
                list.get(i).setDeviceNum("2019"+game(9));
            }
            page.setRecords(list);
        }
        return Result.success(page);
    }

    @PostMapping("/dcListPage")
    @ApiOperation(value = "消防占道分页查询", notes = "传参：") //没有表
    public Result dcListPage(HttpServletRequest request, Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        String problem = "消防占道";
        Page<WarnInfo> page = devicePerceptionService.wellListPage(communityCode, problem, pageNum, pageSize);
        List<WarnInfo> list = page.getRecords();
        if (!list.isEmpty()){
            for (int i=0; i<list.size(); i++) {
                list.get(i).setDeviceName("地磁设备");
                list.get(i).setDeviceType("NB-IOT");
                list.get(i).setDeviceNum("2019"+game(9));
            }
            page.setRecords(list);
        }
        return Result.success(page);
    }

    @PostMapping("/smokeListPage")
    @ApiOperation(value = "烟感分页查询", notes = "传参：") //没有表
    public Result smokeListPage(HttpServletRequest request, Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        String problem = "疑似火情";
        Page<WarnInfo> page = devicePerceptionService.wellListPage(communityCode, problem, pageNum, pageSize);
        List<WarnInfo> list = page.getRecords();
        if (!list.isEmpty()){
            for (int i=0; i<list.size(); i++) {
                list.get(i).setDeviceName("烟感设备");
                list.get(i).setDeviceType("NB-IOT");
                list.get(i).setDeviceNum("2019"+game(9));
            }
            page.setRecords(list);
        }
        return Result.success(page);
    }

    @PostMapping("/urgentButtonListPage")
    @ApiOperation(value = "紧急按钮分页查询", notes = "传参：") //没有表
    public Result urgentButtonListPage(HttpServletRequest request, Integer pageNum, Integer pageSize){
        String sessionId = CookieUtils.getSessionId(request);
        SysUser user = (SysUser) redisService.get(RedisConstant.SESSION_ID + sessionId);
        String communityCode = user.getCommunityCode();
        String problem = "紧急按钮报警";
        Page<WarnInfo> page = devicePerceptionService.wellListPage(communityCode, problem, pageNum, pageSize);
        List<WarnInfo> list = page.getRecords();
        if (!list.isEmpty()){
            for (int i=0; i<list.size(); i++) {
                list.get(i).setDeviceName("紧急按钮");
                list.get(i).setDeviceType("NB-IOT");
                list.get(i).setDeviceNum("2019"+game(9));
            }
            page.setRecords(list);
        }
        return Result.success(page);
    }

    public String game(int count){
        StringBuffer sb = new StringBuffer();
        String str = "0123456789";
        Random r = new Random();
        for(int i=0;i<count;i++){
            int num = r.nextInt(str.length());
            sb.append(str.charAt(num));
            str = str.replace((str.charAt(num)+""), "");
        }
        return sb.toString();
    }



    /*@PostMapping("/baojin")
    @ApiOperation(value = "报警", notes = "传参：place(利雅轩小区、南标小区)，type(烟感、地磁、井盖位移、紧急按钮)") //没有表
    @Transactional
    public Result baoJin(String place,String type){
        if ("烟感".equals(type)){
            WarnInfo warnInfo = new WarnInfo();
            warnInfo.setPhone("152****7130");
            warnInfo.setPlace(place + "");//这里需要补齐
            warnInfo.setProblem("疑似火情");
            warnInfo.setWarnInfo("出现大量烟雾，请及时处理");
            devicePerceptionService.saveBaoJin(warnInfo);
            String title = "消息通知";
            String MSG = "收到新的问题反馈，请登录网格助手进行处理";
            Integer id = reportProblemService.save(70, warnInfo.getPlace()+warnInfo.getWarnInfo(), warnInfo.getProblem(), warnInfo.getPlace(), 1, new ArrayList<>());
            taskMessageService.save(0,id,title,MSG,1,0,0,null);
            WebPush.sendAllsetNotification(MSG,title);
            SmsCommunityAppUtil.sendMsg("18170879118", MSG);
        } else if ("地磁".equals(type)) {

        }
    }*/

}
