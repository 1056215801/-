package com.mit.community.importdata;

import com.alibaba.fastjson.JSON;
import com.dnake.common.DnakeWebApiUtil;
import com.dnake.constant.DnakeConstants;
import com.dnake.entity.DnakeAppUser;
import com.mit.community.importdata.modelTest.*;
import com.mit.community.modeular.webcommons.model.ClusterCommunity;
import com.mit.community.service.IClusterCommunityService;
import com.mit.community.service.impl.ClusterCommunityServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入数据测试
 *
 * @author shuyy
 * @date 2018/11/13
 * @company mitesofor
 */
public class ImportDataTest {

    private static IClusterCommunityService clusterCommunity = new ClusterCommunityServiceImpl();

    /**
     * 获取集群所有小区
     *
     * @author Mr.Deng
     * @date 16:37 2018/11/13
     */
    @Test
    public void queryClusterCommunity() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/community/queryClusterCommunity";
        HashMap<String, Object> map = new HashMap<>();
        map.put("clusterAccountId", DnakeAppUser.clusterAccountid);
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //处理返回json数据
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("communityList");
        List<ClusterCommunityTest> clusterCommunities = JSON.parseArray(jsonArray.toString(), ClusterCommunityTest.class);
        for (ClusterCommunityTest c : clusterCommunities) {
            ClusterCommunity cc = new ClusterCommunity();
            cc.setAddress(c.getAddress());
            cc.setAreaName(c.getAreaName());
            cc.setCityName(c.getCityName());
            cc.setCommunityCode(c.getCommunityCode());
            cc.setCommunityId(c.getCommunityId());
            cc.setCommunityName(c.getCommunityName());
            cc.setProvinceName(c.getProvinceName());
            cc.setStreetName(c.getStreetName());
            cc.setThirdPartyId(c.getThirdPartyId());
            clusterCommunity.save(cc);
        }
    }

    /**
     * 获取分区列表
     *
     * @author Mr.Deng
     * @date 17:36 2018/11/13
     */
    @Test
    public void getZoneList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1//zone/getZoneList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        map.put("zoneStatus", 1);
        String invoke = DnakeWebApiUtil.invoke(url, map);

        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("zoneList");
        List<ZoneList> zoneLists = JSON.parseArray(jsonArray.toString(), ZoneList.class);
        for (ZoneList s : zoneLists) {
            System.out.println(s.getZoneId());
        }
    }

    /**
     * 获取楼栋列表
     *
     * @author Mr.Deng
     * @date 17:44 2018/11/13
     */
    @Test
    public void getBuildingList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/building/getBuildingList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        map.put("zoneId", 363);
        map.put("buildingStatus", "1");
        String invoke = DnakeWebApiUtil.invoke(url, map);
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("buildingList");
        List<Building> buildings = JSON.parseArray(jsonArray.toString(), Building.class);
        for (Building b : buildings) {
            System.out.println(b.getBuildingId());
        }
    }

    /**
     * 获取单元
     *
     * @author Mr.Deng
     * @date 18:01 2018/11/13
     */
    @Test
    public void getUnitList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1//unit/getUnitList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        map.put("zoneId", "363");
        map.put("buildingId", "423");
        map.put("unitStatus", 1);
        String invoke = DnakeWebApiUtil.invoke(url, map);
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("unitList");
        List<Unit> units = JSON.parseArray(jsonArray.toString(), Unit.class);
        for (Unit u : units) {
            System.out.println(u.getUnitId());
        }
    }

    /**
     * 获取房间列表
     *
     * @author Mr.Deng
     * @date 18:02 2018/11/13
     */
    @Test
    public void getRoomList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/room/getRoomList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        map.put("zoneId", 363);
        map.put("buildingId", 423);
        map.put("unitId", 565);
        map.put("roomStatus", 1);
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //{"roomNum":"0101","roomStatus":1,"roomId":15448}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("roomList");
        List<Room> rooms = JSON.parseArray(jsonArray.toString(), Room.class);
        for (Room r : rooms) {
            System.out.println(r.getRoomNum());
        }
    }

    /**
     * 获取住户列表
     *
     * @author Mr.Deng
     * @date 18:10 2018/11/13
     */
    @Test
    public void getHouseholdList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/household/getHouseholdList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        map.put("zoneId", 363);
        map.put("buildingId", 423);
        map.put("unitId", 565);
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //{"householdName":"舒园园","householdType":8,"buildingName":"二号楼","authorizeStatus":6,"householdId":37137,"zoneName":"珉轩智能大厦","doorDeviceGroupIds":"","householdStatus":1,"unitName":"一单元","roomNum":"0101","gender":0,"residenceTime":"2100-01-01 00:00:00","appDeviceGroupIds":"629","mobile":"13064102937"}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("householdList");
        List<House> houses = JSON.parseArray(jsonArray.toString(), House.class);
        for (House h : houses) {
            System.out.println(h.getHouseholdName());
        }
    }

    /**
     * 获取访客列表
     *
     * @author Mr.Deng
     * @date 18:21 2018/11/13
     */
    @Test
    public void getVisitorList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/visitor/getVisitorList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //"visitorList":[{"visitorStatus":1,"buildingName":"凯翔外滩门口机1","expiryDate":"2018-10-16 23:59:59","zoneName":"鹰潭人脸测试","inviteName":"严波","inviteType":1,"unitName":"一单元","roomNum":"0201","inviteMobile":"13407901037","visitorId":4796}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("visitorList");
        List<Visitor> visitors = JSON.parseArray(jsonArray.toString(), Visitor.class);
        for (Visitor v : visitors) {
            System.out.println(v.getVisitorId() + "-》" + v.getInviteName());
        }
    }

    /**
     * 获取设备列表
     *
     * @author Mr.Deng
     * @date 10:02 2018/11/14
     */
    @Test
    public void getDeviceList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/device/getDeviceList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //"deviceList":[{"deviceNum":"AB900DX8880285879170","deviceName":"凯翔演示-出门","deviceType":"W","deviceCode":"2","deviceStatus":0,"buildingCode":"","deviceSip":"61723086239699","deviceId":2602,"unitCode":""}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
        List<Device> devices = JSON.parseArray(jsonArray.toString(), Device.class);
        for (Device d : devices) {
            System.out.println(d.getDeviceId() + "-》" + d.getDeviceNum());
        }
    }

    /**
     * 获取设备组列表
     *
     * @author Mr.Deng
     * @date 10:09 2018/11/14
     */
    @Test
    public void getDeviceGroupList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/deviceGroup/getDeviceGroupList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //"deviceGroupList":[{"deviceGroupId":"629","deviceGroupName":"公共权限组","groupType":"2","deviceList":[{"deviceNum":"AB900DX8880285879170","deviceName":"凯翔演示-出门","deviceType":"W","deviceStatus":"0","buildingCode":"","deviceId":"2602","unitCode":""},{"deviceNum":"AB900DX88801e86a7770","deviceName":"凯翔演示-进门","deviceType":"W","deviceStatus":"1","buildingCode":"","unitId":"996","deviceId":"2572","unitCode":"","buildingId":"603"}]}]
        JSONObject json = JSONObject.fromObject(invoke);
        JSONArray deviceGroupList = json.getJSONArray("deviceGroupList");
        for (int i = 0; i < deviceGroupList.size(); i++) {
            JSONObject jsonObject = JSONObject.fromObject(deviceGroupList.get(i));
            System.out.println(jsonObject.get("deviceGroupId") + "->" + jsonObject.get("deviceGroupName") +
                    "->" + jsonObject.get("groupType") + "->" + "deviceList:");
            JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
            List<Device> devices = JSON.parseArray(jsonArray.toString(), Device.class);
            for (Device d : devices) {
                System.out.println(d.getDeviceId() + "-》" + d.getDeviceNum());
            }
        }
    }

    /**
     * 获取邀请码列表
     *
     * @author Mr.Deng
     * @date 10:57 2018/11/14
     */
    @Test
    public void getInviteCodeList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/visitor/getInviteCodeList";
        Map<String, Object> map = new HashMap<>();
        map.put("appUserId", 77626);
        map.put("pageIndex", 0);
        map.put("pageSize", 100);
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //"list":[{"modifytime":"2018-11-14 11:17:14","validityEndTime":"2018-11-14 23:59:59","dataStatus":1,"inviteCode":"8291","deviceGroupId":0,"useTimes":1,"inviteCodeType":2}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        List<InviteCode> inviteCodes = JSON.parseArray(jsonArray.toString(), InviteCode.class);
        for (InviteCode i : inviteCodes) {
            System.out.println(i.getInviteCode() + "->" + i.getModifytime());
        }
    }

    /**
     * 获取门禁记录
     *
     * @author Mr.Deng
     * @date 16:37 2018/11/13
     */
    @Test
    public void getAccessControlList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/device/getAccessControlList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //{"accessControlList":[{"householdName":"严波","accessTime":"2018-11-14 11:17:14","buildingName":"2栋","householdId":22376,"buildingCode":"0002","accessImgUrl":"http://image.ishanghome.com/1542165434.jpg","zoneName":"珉轩智能大厦","householdMobile":"13407901037","id":2464172,"deviceNum":"AB900DX88801e86a7770","deviceName":"凯翔演示-进门","interactiveType":2,"unitName":"一单元","cardNum":"8291","unitCode":"01"}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("accessControlList");
        List<AccessControl> accessControls = JSON.parseArray(jsonArray.toString(), AccessControl.class);
        for (AccessControl a : accessControls) {
            System.out.println(a.getHouseholdName() + "->" + a.getAccessImgUrl());
        }
    }

    /**
     * 获取呼叫记录
     *
     * @author Mr.Deng
     * @date 11:35 2018/11/14
     */
    @Test
    public void getDeviceCallList() {
        DnakeConstants.choose(DnakeConstants.MODEL_PRODUCT);
        String url = "/v1/device/getDeviceCallList";
        Map<String, Object> map = new HashMap<>();
        map.put("communityCode", "ab497a8a46194311ad724e6bf79b56de");
        String invoke = DnakeWebApiUtil.invoke(url, map);
        //"deviceCallList":[{"deviceNum":"AB900DX88801e86a7770","callDuration":24,"openDoorType":4,"receiver":"61723128437027","deviceName":"凯翔演示-进门","callTime":"2018-11-14 08:45:45","callImgUrl":"http://image.ishanghome.com/1542156369.jpg","callType":0},{"deviceNum":"AB900DX8880285879170","callDuration":9,"openDoorType":4,"receiver":"61723128437027","deviceName":"凯翔外滩测试","callTime":"2018-11-14 07:17:48","callImgUrl":"http://image.ishanghome.com/1542151078.jpg","callType":6},{"deviceNum":"AB900DX87689f06d7c70","callDuration":10,"openDoorType":0,"receiver":"61723130553520","deviceName":"北京演示","callTime":"2018-11-13 17:59:10","roomNum":"1010102","callImgUrl":"http://image.ishanghome.com/1542103151.jpg","callType":6}
        JSONObject jsonObject = JSONObject.fromObject(invoke);
        JSONArray jsonArray = jsonObject.getJSONArray("deviceCallList");
        List<DeviceCall> deviceCalls = JSON.parseArray(jsonArray.toString(), DeviceCall.class);
        for (DeviceCall d : deviceCalls) {
            System.out.println(d.getDeviceName() + "->" + d.getCallImgUrl());
        }
    }

    /**
     * 获取门禁记录
     */
    @Test
    public void getAccessControlList(){
        String url = "/v1/device/getAccessControlList";
        HashMap<String, Object> map = new HashMap<>();
        map.put("clusterAccountId", DnakeAppUser.clusterAccountid);
        DnakeWebApiUtil.invoke(url, map);
    }

}
