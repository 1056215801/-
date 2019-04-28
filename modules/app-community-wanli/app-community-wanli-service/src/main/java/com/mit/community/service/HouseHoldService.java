package com.mit.community.service;

import com.ace.cache.annotation.CacheClear;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.Lists;
import com.mit.community.constants.Constants;
import com.mit.community.entity.*;
import com.mit.community.mapper.*;
import com.mit.community.util.AuthorizeStatusUtil;
import com.mit.community.util.ConstellationUtil;
import com.mit.community.util.DateUtils;
import com.mit.community.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 住户
 *
 * @author shuyy
 * @date 2018/11/30
 * @company mitesofor
 */
@Service
public class HouseHoldService {

    @Autowired
    private HouseHoldMapper houseHoldMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ClusterCommunityService clusterCommunityService;
    @Autowired
    private UserHouseholdMapper userHouseholdMapper;
    @Autowired
    private HouseholdRoomService householdRoomService;
    @Autowired
    private DnakeAppApiService dnakeAppApiService;
    @Autowired
    private IdCardInfoExtractorUtil idCardInfoExtractorUtil;
    @Autowired
    private AuthorizeAppHouseholdDeviceGroupService authorizeAppHouseholdDeviceGroupService;
    @Autowired
    private AuthorizeHouseholdDeviceGroupService authorizeHouseholdDeviceGroupService;
    @Autowired
    private AuthorizeAppHouseholdDeviceGroupMapper authorizeAppHouseholdDeviceGroupMapper;
    @Autowired
    private AuthorizeHouseholdDeviceGroupMapper authorizeHouseholdDeviceGroupMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 查询住户，通过住户列表
     *
     * @param householdIdList 住户列表
     * @return java.util.List<com.mit.community.entity.HouseHold>
     * @author shuyy
     * @date 2018/11/30 11:15
     * @company mitesofor
     */
    public List<HouseHold> listByHouseholdIdList(List<Integer> householdIdList) {
        EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
        wrapper.in("household_id", householdIdList);
        return houseHoldMapper.selectList(wrapper);
    }

    /**
     * 查询住户列表，通过用户id
     *
     * @param userId 用户id
     * @return com.mit.community.entity.HouseHold
     * @author shuyy
     * @date 2018/12/7 10:54
     * @company mitesofor
     */
    public HouseHold getByUserId(Integer userId) {
        EntityWrapper<UserHousehold> wrapper = new EntityWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserHousehold> userHouseholds = userHouseholdMapper.selectList(wrapper);
        if (userHouseholds.isEmpty()) {
            return null;
        } else {
            List<Integer> householdIds = userHouseholds.parallelStream().map(UserHousehold::getHouseholdId).collect(Collectors.toList());
            return this.listByHouseholdIdList(householdIds).get(0);
        }
    }

    /**
     * 查询住户列表，通过手机号
     * @param cellphone
     * @return java.util.List<com.mit.community.entity.HouseHold>
     * @throws
     * @author shuyy
     * @date 2018/12/10 15:35
     * @company mitesofor
     */
    /**
     * 防止查询空值信息，暂时注掉（胡山林）
     *
     * @param cellphone
     * @return
     */
    //@Cache(key = "household:cellphone:{1}")
    public List<HouseHold> getByCellphone(String cellphone) {
        EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
        wrapper.eq("mobile", cellphone);
        List<HouseHold> houseHolds = houseHoldMapper.selectList(wrapper);
        return houseHolds;
    }

    /**
     * 查询住户信息，通过手机号和小区code
     *
     * @param cellphone     手机号
     * @param communityCode 小区code
     * @return 住户信息
     * @author Mr.Deng
     * @date 14:15 2018/12/12
     */
    public HouseHold getByCellphoneAndCommunityCode(String cellphone, String communityCode) {
        EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
        wrapper.eq("mobile", cellphone);
        wrapper.eq("community_code", communityCode);
        wrapper.eq("household_status", 1);//只查询状态“正常”的数据
        List<HouseHold> houseHolds = houseHoldMapper.selectList(wrapper);
        if (houseHolds.isEmpty()) {
            return null;
        }
        return houseHolds.get(0);
    }

    /**
     * 查找住户信息，通过住户id
     *
     * @param householdId 住户id
     * @return 住户信息
     * @author Mr.Deng
     * @date 16:06 2018/12/7
     */
    public HouseHold getByHouseholdId(Integer householdId) {
        EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
        wrapper.eq("household_id", householdId);
        List<HouseHold> houseHolds = houseHoldMapper.selectList(wrapper);
        if (houseHolds.isEmpty()) {
            return null;
        }
        return houseHolds.get(0);
    }

    /**
     * 更新数据
     *
     * @param houseHold 住户信息
     * @author Mr.Deng
     * @date 14:11 2018/12/13
     */
    @CacheClear(key = "household:cellphone:{1.mobile}")
    public void update(HouseHold houseHold) {
        houseHoldMapper.updateById(houseHold);
    }

    /**
     * 保存住户
     *
     * @param houseHold 住户对象
     * @author shuyy
     * @date 2019-01-24 10:51
     * @company mitesofor
     */
    @CacheClear(key = "household:cellphone:{1.mobile}")
    public void save(HouseHold houseHold) {
        houseHold.setGmtModified(LocalDateTime.now());
        houseHold.setGmtCreate(LocalDateTime.now());
        houseHoldMapper.insert(houseHold);
    }

    /**
     * 根据手机号和社区code查询本地钥匙信息
     *
     * @return
     */
    public HouseHold getHouseholdByPhoneAndCode(String cellphone, String communityCode) {
        EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
        wrapper.eq("community_code", communityCode);
        wrapper.eq("mobile", cellphone);
        List<HouseHold> houseHolds = houseHoldMapper.selectList(wrapper);
        if (houseHolds.isEmpty()) {
            return null;
        }
        return houseHolds.get(0);
    }

    /**
     * 分页查询住户信息数据
     *
     * @param request
     * @param zoneId
     * @param communityCode
     * @param buildingId
     * @param unitId
     * @param roomId
     * @param contactPerson
     * @param contactCellphone
     * @param status
     * @param search_validEndFlag
     * @param select_autyType
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<HouseHold> listHouseholdByPage(HttpServletRequest request,
                                               Integer zoneId,
                                               String communityCode,
                                               Integer buildingId,
                                               Integer unitId,
                                               Integer roomId,
                                               String contactPerson,
                                               String contactCellphone,
                                               Integer houseType,
                                               Integer status,
                                               Integer search_validEndFlag,//有效期标识：1-即将到期，2-已过期
                                               Integer select_autyType,//授权类型（二进制相加而来，查询时后台需要拆分）
                                               Integer pageNum, Integer pageSize) {
        EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
        if (zoneId != null) {
            wrapper.eq("zone_id", zoneId);
        }
        if (buildingId != null) {
            wrapper.eq("building_id", buildingId);
        }
        if (unitId != null) {
            wrapper.eq("unit_id", unitId);
        }
        if (roomId != null) {
            wrapper.eq("room_id", roomId);
        }
        if (StringUtils.isNotBlank(communityCode)) {
            wrapper.eq("community_code", communityCode);
        }
        if (StringUtils.isNotBlank(contactPerson)) {
            wrapper.like("household_name", contactPerson);
        }
        if (StringUtils.isNotBlank(contactCellphone)) {
            wrapper.like("mobile", contactCellphone);
        }
        //户主关系
        if (houseType != null) {
            wrapper.eq("housetype", houseType);
        }
        //住户状态
        if (status != null) {
            if (status == 2) {//停用
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                wrapper.lt("validity_time", LocalDate.parse(sdf.format(new Date()), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                wrapper.eq("household_status", status);
            }
        } else {//默认不查询注销数据
            wrapper.in("household_status", new Integer[]{1, 2});
        }
        //有效期限查询字段处理
        if (search_validEndFlag != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (search_validEndFlag == 1) {//期限时间小于30天，即将过期
                    //查询residenceTime(LocalDate)字段
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, 1);
                    Date startDate = cal.getTime();
                    cal.add(Calendar.DATE, 30);
                    Date endDate = cal.getTime();
                    wrapper.ge("validity_time", LocalDate.parse(sdf.format(startDate), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    wrapper.le("validity_time", LocalDate.parse(sdf.format(endDate), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
                if (search_validEndFlag == 2) {//已过期
                    wrapper.lt("validity_time", LocalDate.parse(sdf.format(new Date()), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //授权类型字段处理
        if (select_autyType != null) {
            wrapper.eq("authorize_status", select_autyType);
        }
        wrapper.orderBy("gmt_create", false);
        List<HouseHold> houseHolds = new ArrayList<>();
        if (pageNum != null && pageSize != null) {
            Page<HouseHold> page = new Page<>(pageNum, pageSize);
            houseHolds = houseHoldMapper.selectPage(page, wrapper);
            for (HouseHold houseHold : houseHolds) {
                //权限到期天数计算diffDay,前端需要用来做样式判断
                if (houseHold.getValidityTime() != null) {
                    houseHold.setDiffDay(DateUtils.getDateInter(new Date(), houseHold.getValidityTime()));
                } else {
                    LocalDate residenceTime = houseHold.getResidenceTime();
                    LocalDate currentTime = LocalDate.now();
                    houseHold.setDiffDay(DateUtils.getLocalDateInter(currentTime, residenceTime));
                }
                //查询房屋信息
                List<HouseholdRoom> rooms = householdRoomService.listByHouseholdId(houseHold.getHouseholdId());
                if (rooms.size() != 0) {
                    //与户主关系字段赋值
                    HouseholdRoom room = rooms.get(0);//默认取最早注册的房屋信息
                    Integer householdType = Integer.valueOf(room.getHouseholdType());
                    switch (householdType) {
                        case 1:
                            houseHold.setHouseholdType("本人");
                            break;
                        case 2:
                            houseHold.setHouseholdType("配偶");
                            break;
                        case 3:
                            houseHold.setHouseholdType("父母");
                            break;
                        case 4:
                            houseHold.setHouseholdType("子女");
                            break;
                        case 5:
                            houseHold.setHouseholdType("亲属");
                            break;
                        case 6:
                            houseHold.setHouseholdType("非亲属");
                            break;
                        case 7:
                            houseHold.setHouseholdType("租赁");
                            break;
                        case 8:
                            houseHold.setHouseholdType("其他");
                            break;
                        case 9:
                            houseHold.setHouseholdType("保姆");
                            break;
                        case 10:
                            houseHold.setHouseholdType("护理人员");
                            break;
                        default:
                            houseHold.setHouseholdType(null);
                            break;
                    }
                    //房屋信息处理
                    StringBuffer roomInfo = new StringBuffer();
                    for (int i = 0; i < rooms.size(); i++) {
                        if (i == rooms.size() - 1) {
                            roomInfo.append(rooms.get(i).getZoneName() + "-" + rooms.get(i).getBuildingName() + "-" + rooms.get(i).getUnitName() + "-" + rooms.get(i).getRoomNum());
                        } else {
                            roomInfo.append(rooms.get(i).getZoneName() + "-" + rooms.get(i).getBuildingName() + "-" + rooms.get(i).getUnitName() + "-" + rooms.get(i).getRoomNum() + ",");
                        }
                    }
                    houseHold.setHousing(roomInfo.toString());
                }
            }
//            if (houseType != null) {
//                List<HouseHold> list = new ArrayList<>();
//                for (HouseHold houseHold : houseHolds) {
//                    //查询房屋信息
//                    List<HouseholdRoom> rooms = householdRoomService.listByHouseholdId(houseHold.getHouseholdId());
//                    if (houseType == Integer.valueOf(rooms.get(0).getHouseholdType())) {
//                        list.add(houseHold);
//                    }
//                }
//                page.setRecords(list);
//                return page;
//            }
            page.setRecords(houseHolds);
            return page;
        }
        return null;
    }

    /**
     * 保存住户房屋信息
     *
     * @param jsonObject
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String SaveHouseholdInfoByStepOne(String communityCode, JSONObject jsonObject) {
        String msg = "";
        //参数获取
        String data = jsonObject.toJSONString();
        JSONObject json = JSON.parseObject(data);
        String certificateStr = json.getString("certificateStr");//用户身份信息
        String contactType = json.getString("contactType");//联系人类型
        String houseProperties = json.getString("houseProperties");//房屋列表
        String householdName = json.getString("householdName");//住户姓名
        String householdStr = json.getString("householdStr");//住户信息
        String mobile = json.getString("mobile");//手机号码
        String realCertificateStr = json.getString("realCertificateStr");//未成年人身份
        String realNameFlag = json.getString("realNameFlag");//实名身份标识
        String roomIdArr = json.getString("roomIdArr");//房屋ID
        String urgentMobile = json.getString("urgentMobile");//代理人手机号
        String idCard = json.getString("idCard");//证件号码
        Integer householdId = Integer.valueOf(json.getString("householdId"));
        try {
            List<HouseRoomsVo> list = new ArrayList<>();
            if (StringUtils.isNoneEmpty(houseProperties)) {
                JSONArray roomsArray = JSONArray.parseArray(houseProperties);
                for (int i = 0; i < roomsArray.size(); i++) {
                    HouseRoomsVo roomsVo = new HouseRoomsVo();
                    roomsVo.setZoneId(JSONObject.parseObject(roomsArray.get(i).toString()).getString("zoneId"));
                    roomsVo.setZoneName(JSONObject.parseObject(roomsArray.get(i).toString()).getString("zoneName"));
                    roomsVo.setBuildingId(JSONObject.parseObject(roomsArray.get(i).toString()).getString("buildingId"));
                    roomsVo.setBuildingName(JSONObject.parseObject(roomsArray.get(i).toString()).getString("buildingName"));
                    roomsVo.setUnitId(JSONObject.parseObject(roomsArray.get(i).toString()).getString("unitId"));
                    roomsVo.setUnitName(JSONObject.parseObject(roomsArray.get(i).toString()).getString("unitName"));
                    roomsVo.setRoomId(JSONObject.parseObject(roomsArray.get(i).toString()).getString("roomId"));
                    roomsVo.setRoomNum(JSONObject.parseObject(roomsArray.get(i).toString()).getString("roomNum"));
                    roomsVo.setHouseholdType(JSONObject.parseObject(roomsArray.get(i).toString()).getString("householdType"));
                    list.add(roomsVo);
                }
            }
            /**
             * 判断是新增还是修改
             */
            HouseHold existHouseHold = this.getByHouseholdId(householdId);
            if (existHouseHold == null) {//新增
//            List<Map<String, Object>> houseList = Lists.newArrayListWithCapacity(10);
//            Map<String, Object> h = Maps.newHashMapWithExpectedSize(4);
//            for(HouseRoomsVo room : list){
//                Integer zoneId = Integer.valueOf(room.getZoneId());
//                Integer buildingId = Integer.valueOf(room.getBuildingId());
//                Integer unitId = Integer.valueOf(room.getUnitId());
//                Integer roomId = Integer.valueOf(room.getRoomId());
//                h.put("zoneId", zoneId);
//                h.put("buildingId", buildingId);
//                h.put("unitId", unitId);
//                h.put("roomId", roomId);
//                h.put("householdType", room.getHouseholdType());
//                houseList.add(h);
//            }
//            JSONObject message = dnakeAppApiService.saveHousehold(communityCode, mobile, gender,
//                    householdName, residenceTimeStr, houseList);
//            if (message.get("errorCode") != null && !message.get("errorCode").equals(0)) {
//                msg = message.get("msg").toString();
//                throw new RuntimeException(message.get("msg").toString());
//            }
                // 本地数据库保存住户信息
                JSONObject householdStrJson = JSON.parseObject(householdStr);
                Integer gender = Integer.valueOf(householdStrJson.getString("gender"));
                String residenceTimeStr = householdStrJson.getString("stayEndTime");
                IdCardInfo idCardInfo = idCardInfoExtractorUtil.idCardInfo(idCard);
                String constellation = ConstellationUtil.calc(idCardInfo.getBirthday());
                HouseHold houseHold = new HouseHold(communityCode, constellation, householdId, householdName,
                        1, 0,
                        gender, com.mit.common.util.DateUtils.parseStringToLocalDate(residenceTimeStr, "yyyy-MM-dd"),
                        mobile, StringUtils.EMPTY,
                        StringUtils.EMPTY, idCard, idCardInfo.getProvince(),
                        idCardInfo.getCity(), idCardInfo.getRegion(), idCardInfo.getBirthday(),
                        (short) 99, null, null, null, null, null, null, null,
                        Integer.valueOf(list.get(0).getHouseholdType()));
                houseHold.setGmtModified(LocalDateTime.now());
                houseHold.setGmtCreate(LocalDateTime.now());
                houseHoldMapper.insert(houseHold);
                // 本地关联房屋
                ClusterCommunity clusterCommunity = clusterCommunityService.getByCommunityCode(communityCode);
                for (HouseRoomsVo room : list) {
                    HouseholdRoom householdRoom = new HouseholdRoom(communityCode,
                            clusterCommunity.getCommunityName(),
                            Integer.valueOf(room.getZoneId()),
                            room.getZoneName(),
                            Integer.valueOf(room.getBuildingId()),
                            room.getBuildingName(),
                            Integer.valueOf(room.getUnitId()),
                            room.getUnitName(),
                            Integer.valueOf(room.getRoomId()),
                            room.getRoomNum(),
                            Short.valueOf(room.getHouseholdType()),
                            householdId,
                            null);
                    householdRoomService.save(householdRoom);
                }
                //更新本地用户住户信息id
                User user = userService.getByCellphone(mobile);
                if (user != null) {
                    userService.updateCellphoneByHouseholdId(mobile, householdId);
                }
                msg = "success";
            } else {//修改
                //根据住户id修改住户信息
                JSONObject householdStrJson = JSON.parseObject(householdStr);
                Integer gender = Integer.valueOf(householdStrJson.getString("gender"));
                String residenceTimeStr = householdStrJson.getString("stayEndTime");
                IdCardInfo idCardInfo = idCardInfoExtractorUtil.idCardInfo(idCard);
                String constellation = ConstellationUtil.calc(idCardInfo.getBirthday());
                HouseHold edidHousehold = new HouseHold(null, constellation, householdId, householdName, existHouseHold.getHouseholdStatus(),
                        existHouseHold.getAuthorizeStatus(), gender, com.mit.common.util.DateUtils.parseStringToLocalDate(residenceTimeStr, "yyyy-MM-dd"),
                        mobile, StringUtils.EMPTY,
                        StringUtils.EMPTY, idCard, idCardInfo.getProvince(),
                        idCardInfo.getCity(), idCardInfo.getRegion(), idCardInfo.getBirthday(),
                        (short) 99, null, null, null, null, null, null, null,
                        Integer.valueOf(list.get(0).getHouseholdType()));
                edidHousehold.setGmtModified(LocalDateTime.now());
                houseHoldMapper.updateHouseholdByHouseholdId(edidHousehold);
                //删除房屋信息
                householdRoomService.deleteByHouseholdId(householdId);
                // 本地关联房屋
                ClusterCommunity clusterCommunity = clusterCommunityService.getByCommunityCode(communityCode);
                for (HouseRoomsVo room : list) {
                    HouseholdRoom householdRoom = new HouseholdRoom(communityCode,
                            clusterCommunity.getCommunityName(),
                            Integer.valueOf(room.getZoneId()),
                            room.getZoneName(),
                            Integer.valueOf(room.getBuildingId()),
                            room.getBuildingName(),
                            Integer.valueOf(room.getUnitId()),
                            room.getUnitName(),
                            Integer.valueOf(room.getRoomId()),
                            room.getRoomNum(),
                            Short.valueOf(room.getHouseholdType()),
                            householdId,
                            null);
                    householdRoomService.save(householdRoom);
                }
                //同步修改用户手机号码
                //String setSql1 = "cellphone = " + " ' " + mobile + " ' ";
                //EntityWrapper<User> ew1 = new EntityWrapper<>();
                //ew1.eq("cellphone", householdId);
                //userMapper.updateForSet(setSql1, ew1);
                userMapper.updateMobileByHouseholdId(mobile, householdId);
            }
        } catch (Exception e) {
            msg = "fail";
            throw new RuntimeException(msg);
        }
        return msg;
    }

    /**
     * 保存住户授权信息
     *
     * @param editFlag        //是否编辑标识
     * @param householdId
     * @param appAuthFlag
     * @param directCall
     * @param tellNum
     * @param faceAuthFlag
     * @param deviceGIds
     * @param validityEndDate
     * @param cardListArr
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String SaveHouseholdInfoByStepThree(Integer editFlag, Integer householdId, Integer appAuthFlag, Integer directCall, String tellNum, Integer faceAuthFlag, String deviceGIds, String validityEndDate, String cardListArr) {
        String msg = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            //更新本地住户授权类型字段+本地更新住户有效期权限时间
            Integer authStatus = 0;
            String string = cardListArr.replace("[", "").replace("]", "");
            if (string.length() != 0) {
                authStatus = AuthorizeStatusUtil.GetAuthStatus(appAuthFlag, faceAuthFlag, 1);
            } else {
                authStatus = AuthorizeStatusUtil.GetAuthStatus(appAuthFlag, faceAuthFlag, null);
            }
            houseHoldMapper.updateValidityTime(simpleDateFormat.parse(validityEndDate), authStatus, householdId);
            if (editFlag != null && editFlag == 0) {//新增
                // 本地数据库保存关联设备组
                String[] deviceGroupIds = deviceGIds.split(",");
                List<String> deviceGroupIdList = Arrays.asList(deviceGroupIds);
                List<AuthorizeAppHouseholdDeviceGroup> authorizeAppHouseholdDeviceGroups = Lists.newArrayListWithCapacity(deviceGroupIdList.size());
                deviceGroupIdList.forEach(item -> {
                    AuthorizeAppHouseholdDeviceGroup authorizeAppHouseholdDeviceGroup = new AuthorizeAppHouseholdDeviceGroup(householdId, Integer.parseInt(item));
                    List<AuthorizeAppHouseholdDeviceGroup> groups = authorizeAppHouseholdDeviceGroupMapper.getObjectByIds(householdId, Integer.parseInt(item));
                    if (groups.size() == 0) {
                        authorizeAppHouseholdDeviceGroup.setGmtCreate(LocalDateTime.now());
                        authorizeAppHouseholdDeviceGroup.setGmtModified(LocalDateTime.now());
                        authorizeAppHouseholdDeviceGroups.add(authorizeAppHouseholdDeviceGroup);
                    }
                });
                if (authorizeAppHouseholdDeviceGroups.size() != 0) {
                    authorizeAppHouseholdDeviceGroupService.insertBatch(authorizeAppHouseholdDeviceGroups);
                }
                //关联本地APP已经授权的设备组，即生成本地钥匙列表，同时注册默认账号
                if (appAuthFlag == 1) {
                    List<AuthorizeHouseholdDeviceGroup> groupsList = Lists.newArrayListWithCapacity(deviceGroupIdList.size());
                    deviceGroupIdList.forEach(item -> {
                        AuthorizeHouseholdDeviceGroup authorizeAppHouseholdDeviceGroup = new AuthorizeHouseholdDeviceGroup(householdId, Integer.parseInt(item));
                        List<AuthorizeHouseholdDeviceGroup> groups = authorizeHouseholdDeviceGroupMapper.getObjectByIds(householdId, Integer.parseInt(item));
                        if (groups.size() == 0) {
                            authorizeAppHouseholdDeviceGroup.setGmtCreate(LocalDateTime.now());
                            authorizeAppHouseholdDeviceGroup.setGmtModified(LocalDateTime.now());
                            groupsList.add(authorizeAppHouseholdDeviceGroup);
                        }
                    });
                    if (groupsList.size() != 0) {
                        authorizeHouseholdDeviceGroupService.insertBatch(groupsList);
                    }
                    //注册默认账号
                    //第一步：判断是否注册
                    HouseHold existHouseHold = this.getByHouseholdId(householdId);
                    User user = userService.getByCellphoneNoCache(existHouseHold.getMobile());
                    //第二步：没有进行默认注册
                    if (user == null) {
                        user = new User(existHouseHold.getMobile(), "123456", householdId, existHouseHold.getHouseholdName(), existHouseHold.getGender().shortValue(), StringUtils.EMPTY, Constants.USER_ICO_DEFULT,
                                Constants.NULL_LOCAL_DATE, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                                "普通业主", StringUtils.EMPTY, null, null, null);
                        userService.save(user);
                    }
                }
                msg = "success";
            } else {//修改
                authorizeAppHouseholdDeviceGroupService.deleteByHouseholdId(householdId);
                authorizeHouseholdDeviceGroupService.deleteByHouseholdId(householdId);
                // 本地数据库保存关联设备组
                String[] deviceGroupIds = deviceGIds.split(",");
                List<String> deviceGroupIdList = Arrays.asList(deviceGroupIds);
                List<AuthorizeAppHouseholdDeviceGroup> authorizeAppHouseholdDeviceGroups = Lists.newArrayListWithCapacity(deviceGroupIdList.size());
                deviceGroupIdList.forEach(item -> {
                    AuthorizeAppHouseholdDeviceGroup authorizeAppHouseholdDeviceGroup = new AuthorizeAppHouseholdDeviceGroup(householdId, Integer.parseInt(item));
                    List<AuthorizeAppHouseholdDeviceGroup> groups = authorizeAppHouseholdDeviceGroupMapper.getObjectByIds(householdId, Integer.parseInt(item));
                    if (groups.size() == 0) {
                        authorizeAppHouseholdDeviceGroup.setGmtCreate(LocalDateTime.now());
                        authorizeAppHouseholdDeviceGroup.setGmtModified(LocalDateTime.now());
                        authorizeAppHouseholdDeviceGroups.add(authorizeAppHouseholdDeviceGroup);
                    }
                });
                if (authorizeAppHouseholdDeviceGroups.size() != 0) {
                    authorizeAppHouseholdDeviceGroupService.insertBatch(authorizeAppHouseholdDeviceGroups);
                }
                //关联本地APP已经授权的设备组，即生成本地钥匙列表
                if (appAuthFlag == 1) {
                    List<AuthorizeHouseholdDeviceGroup> groupsList = Lists.newArrayListWithCapacity(deviceGroupIdList.size());
                    deviceGroupIdList.forEach(item -> {
                        AuthorizeHouseholdDeviceGroup authorizeAppHouseholdDeviceGroup = new AuthorizeHouseholdDeviceGroup(householdId, Integer.parseInt(item));
                        List<AuthorizeHouseholdDeviceGroup> groups = authorizeHouseholdDeviceGroupMapper.getObjectByIds(householdId, Integer.parseInt(item));
                        if (groups.size() == 0) {
                            authorizeAppHouseholdDeviceGroup.setGmtCreate(LocalDateTime.now());
                            authorizeAppHouseholdDeviceGroup.setGmtModified(LocalDateTime.now());
                            groupsList.add(authorizeAppHouseholdDeviceGroup);
                        }
                    });
                    if (groupsList.size() != 0) {
                        authorizeHouseholdDeviceGroupService.insertBatch(groupsList);
                    }
                }
                //注册默认账号
                //第一步：判断是否注册
                HouseHold existHouseHold = this.getByHouseholdId(householdId);
                User user = userService.getByCellphoneNoCache(existHouseHold.getMobile());
                //第二步：没有进行默认注册
                if (user == null) {
                    user = new User(existHouseHold.getMobile(), "123456", householdId, existHouseHold.getHouseholdName(), existHouseHold.getGender().shortValue(), StringUtils.EMPTY, Constants.USER_ICO_DEFULT,
                            Constants.NULL_LOCAL_DATE, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            "普通业主", StringUtils.EMPTY, null, null, null);
                    userService.save(user);
                } else {
                    List<Integer> list = AuthorizeStatusUtil.Contrast(authStatus);
                    //修改之后不包含APP授权，重置用户的住户信息，用户依旧可以登录APP
                    if (!list.contains(10)) {
                        userMapper.updateHouseholdIdByMobile(0, existHouseHold.getMobile());
                    }
                }
                msg = "success";
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage().toString());
        }
        return msg;
    }

    /**
     * 注销住户信息
     * 清空用户信息缓存
     *
     * @param communityCode
     * @param ids
     * @return
     */
    @CacheClear(key = "user:cellphone{1}")
    @Transactional(rollbackFor = Exception.class)
    public String logOut(String communityCode, String ids) {
        String msg = "";
        if (!StringUtils.isEmpty(ids)) {
            String[] id = ids.split(",");
            for (String s : id) {
                /**
                 * 调用狄耐克接口注销
                 */
                JSONObject message = dnakeAppApiService.logOut(Integer.valueOf(s), communityCode);
                if (message.get("errorCode") != null && !message.get("errorCode").equals(0)) {
                    msg = message.get("msg").toString();
                    throw new RuntimeException(message.get("msg").toString());
                }
                /**
                 * 本地注销
                 */
                //注销住户
                HouseHold houseHold = this.getByHouseholdId(Integer.valueOf(s));
                houseHold.setGmtModified(LocalDateTime.now());
                houseHold.setHouseholdStatus(0);//注销状态
                this.update(houseHold);
                //EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
                //wrapper.eq("household_id", Integer.valueOf(s));
                //houseHoldMapper.delete(wrapper);
                //注销用户（重置用户的住户id）
                userMapper.updateByHouseholdId(Integer.valueOf(s));
                //EntityWrapper<User> wrapper1 = new EntityWrapper<>();
                //wrapper1.eq("household_id", Integer.valueOf(s));
                //userMapper.delete(wrapper1);
            }
        }
        return msg;
    }

    /**
     * 停用住户
     *
     * @param communityCode
     * @param id
     * @return
     */
    public String Stop(String communityCode, Integer id) {
        String msg = "";
        if (id != null) {
            try {
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                date = calendar.getTime();
                HouseHold houseHold = new HouseHold();
                houseHold.setGmtModified(LocalDateTime.now());
                houseHold.setHouseholdId(id);
                houseHold.setValidityTime(date);
                houseHold.setHouseholdStatus(2);//停用状态
                houseHoldMapper.updateHouseholdByHouseholdId(houseHold);
                msg = "success";
            } catch (Exception e) {
                msg = "fail";
            }
        }
        return msg;
    }

    /**
     * 获取狄耐克设备组列表
     *
     * @param communityCode
     * @return
     */
    public Result getDeviceGroupList(String communityCode) {
        List<DeviceGroupVo> list = new ArrayList<>();
        if (!StringUtils.isEmpty(communityCode)) {
            JSONObject message = dnakeAppApiService.getDeviceGroupList(communityCode);
            if (message.get("errorCode") != null && !message.get("errorCode").equals(0)) {
                throw new RuntimeException(message.get("msg").toString());
            }
            //获取需要的数据进行封装
            String data = message.toJSONString();
            JSONObject json = JSON.parseObject(data);
            String deviceGroupList = message.getString("deviceGroupList");
            JSONArray roomsArray = JSONArray.parseArray(deviceGroupList);
            for (int i = 0; i < roomsArray.size(); i++) {
                DeviceGroupVo deviceGroupVo = new DeviceGroupVo();
                //设备组类型
                String groupType = JSONObject.parseObject(roomsArray.get(i).toString()).getString("groupType");
                //设备组id
                String deviceGroupId = JSONObject.parseObject(roomsArray.get(i).toString()).getString("deviceGroupId");
                //设备组名称
                String deviceGroupName = JSONObject.parseObject(roomsArray.get(i).toString()).getString("deviceGroupName");
                deviceGroupVo.setGroupType(groupType);
                deviceGroupVo.setDeviceGroupId(deviceGroupId);
                deviceGroupVo.setDeviceGroupName(deviceGroupName);
                list.add(deviceGroupVo);
            }
        }
        Result result = new Result();
        result.setResultStatus(true);
        result.setObject(list);
        result.setMessage("success");
        return result;
    }

    /**
     * 平台修改手机号码
     *
     * @param mobile
     * @param householdId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String AlterMobile(String mobile, Integer householdId) {
        String msg = "";
        try {
            //修改住户信息手机号码
            String setSql = "mobile = " + " ' " + mobile + " ' ";
            EntityWrapper<HouseHold> ew = new EntityWrapper<>();
            ew.eq("mobile", householdId);
            houseHoldMapper.updateForSet(setSql, ew);
            //修改用户信息手机号码
            String setSql1 = "cellphone = " + " ' " + mobile + " ' ";
            EntityWrapper<User> ew1 = new EntityWrapper<>();
            ew1.eq("cellphone", householdId);
            userMapper.updateForSet(setSql1, ew1);
            msg = "success";
        } catch (Exception ex) {
            msg = "fail:" + ex.getMessage().toString();
        }
        return msg;
    }
}
