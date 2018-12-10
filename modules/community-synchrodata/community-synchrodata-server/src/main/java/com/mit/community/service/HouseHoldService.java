package com.mit.community.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dnake.common.DnakeWebApiUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mit.community.common.HttpLogin;
import com.mit.community.common.ThreadPoolUtil;
import com.mit.community.constants.CommonConstatn;
import com.mit.community.entity.AgeConstruction;
import com.mit.community.entity.Building;
import com.mit.community.entity.HouseHold;
import com.mit.community.entity.IdCardInfo;
import com.mit.community.entity.Room;
import com.mit.community.entity.Unit;
import com.mit.community.entity.Zone;
import com.mit.community.mapper.HouseHoldMapper;
import com.mit.community.util.IdCardInfoExtractorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 住户业务层
 *
 * @author Mr.Deng
 * @date 2018/11/14 19:33
 *       <p>
 *       Copyright: Copyright (c) 2018
 *       </p>
 *       <p>
 *       Company: mitesofor
 *       </p>
 */
@Slf4j
@Service
public class HouseHoldService extends ServiceImpl<HouseHoldMapper, HouseHold> {
	private final HouseHoldMapper houseHoldMapper;

	private final ClusterCommunityService clusterCommunityService;
	private final ZoneService zoneService;

	private final BuildingService buildingService;

	private final UnitService unitService;

	private final RoomService roomService;

	private final IdCardInfoExtractorUtil idCardInfoExtractorUtil;

	@Autowired
	private HttpLogin httpLogin;

	@Autowired
	public HouseHoldService(HouseHoldMapper houseHoldMapper, ClusterCommunityService clusterCommunityService,
			ZoneService zoneService, BuildingService buildingService, UnitService unitService, RoomService roomService,
			IdCardInfoExtractorUtil idCardInfoExtractorUtil) {
		this.houseHoldMapper = houseHoldMapper;
		this.clusterCommunityService = clusterCommunityService;
		this.zoneService = zoneService;
		this.buildingService = buildingService;
		this.unitService = unitService;
		this.roomService = roomService;
		this.idCardInfoExtractorUtil = idCardInfoExtractorUtil;
	}

	/**
	 * 查询住户信息，通过小区code列表
	 * 
	 * @param communityCode 小区code
	 * @return 住户信息列表
	 * @author Mr.Deng
	 * @date 15:11 2018/11/21
	 */
	public List<HouseHold> listByCommunityCode(String communityCode) {
		EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
		wrapper.eq("community_code", communityCode);
		return houseHoldMapper.selectList(wrapper);
	}

	/***
	 * 统计年龄结构，通过小区code列表
	 * 
	 * @param communityCodeList 小区code列表
	 * @author shuyy
	 * @return List<AgeConstruction>
	 * @date 2018/11/22 16:32
	 */
	public List<AgeConstruction> countAgeConstructionByCommuintyCodeList(List<String> communityCodeList) {
		List<AgeConstruction> ageConstructions = Lists.newArrayListWithCapacity(30);
		communityCodeList.forEach(item -> {
			List<HouseHold> houseHolds = this.listByCommunityCode(item);
			LocalDate now = LocalDate.now();
			LocalDate childTime = now.minusYears(10);
			LocalDate youngTime = now.minusYears(18);
			LocalDate youthTime = now.minusYears(45);
			LocalDate middleTime = now.minusYears(60);
			int childNum = 0;
			int youngNum = 0;
			int youthNum = 0;
			int middleNum = 0;
			int oldNum = 0;
			houseHolds = houseHolds.parallelStream().filter(a -> a.getBirthday().getYear() != 1900)
					.collect(Collectors.toList());
			for (HouseHold houseHold : houseHolds) {
				LocalDate birthday = houseHold.getBirthday();
				if (birthday.isAfter(childTime)) {
					childNum++;
				} else if (birthday.isAfter(youngTime)) {
					youngNum++;
				} else if (birthday.isAfter(youthTime)) {
					youthNum++;
				} else if (birthday.isAfter(middleTime)) {
					middleNum++;
				} else {
					oldNum++;
				}
			}
			AgeConstruction ageConstruction = new AgeConstruction(item, childNum, youngNum, youthNum, middleNum,
					oldNum);
			ageConstruction.setGmtModified(LocalDateTime.now());
			ageConstruction.setGmtCreate(LocalDateTime.now());
			ageConstructions.add(ageConstruction);
		});
		return ageConstructions;
	}

	/**
	 * 添加住户信息
	 *
	 * @param house 住户信息
	 * @author Mr.Deng
	 * @date 19:34 2018/11/14
	 */
	public void save(HouseHold house) {
		houseHoldMapper.insert(house);
	}

	/**
	 * 获取所有住户信息
	 *
	 * @return 住户信息列表
	 * @author Mr.Deng
	 * @date 19:35 2018/11/14
	 */
	public List<HouseHold> list() {
		return houseHoldMapper.selectList(null);
	}

	/***
	 * 查询住户，通过社区编码
	 * 
	 * @param communityCodeList 社区编码列表
	 * @param param             其他参数
	 * @return 住户列表
	 * @author shuyy
	 * @date 2018/11/20 8:50
	 */
	public List<HouseHold> listFromDnakeByCommunityCodeList(List<String> communityCodeList, Map<String, Object> param) {
		List<HouseHold> result = Lists.newArrayListWithCapacity(12000);
		CountDownLatch countDownLatch = new CountDownLatch(communityCodeList.size());
		communityCodeList.forEach(item -> ThreadPoolUtil.execute(() -> {
			try {
				// 一个小区，最多的用户不会超过10000，所以这里不管小区用户有多少，都发送100个分页请求去查询。
				CountDownLatch pageCountDownLatch = new CountDownLatch(100);
				for (int index = 0; index < 100; index++) {
					int tmp = index;
					ThreadPoolUtil.execute(() -> {
						try {
							int pageSize = 100;
							List<HouseHold> houseHoldList = listFromDnakeByCommunityCodePage(item, tmp, pageSize,
									param);
							result.addAll(houseHoldList);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						} finally {
							pageCountDownLatch.countDown();
						}
					});
				}
				pageCountDownLatch.await();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				countDownLatch.countDown();
			}
		}));
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * result.forEach(item -> ThreadPoolUtil.execute(() -> { // 查询身份证号 try { //
		 * String credentialNum = getCredentialNumFromDnake(item.getHouseholdId());
		 * String credentialNum = StringUtils.EMPTY;
		 * item.setCredentialNum(credentialNum); item.setIdentityType(HouseHold.NORMAL);
		 * // 通过身份证号，分析省、市、区县、出生日期、年龄 // if (credentialNum.equals(StringUtils.EMPTY)) {
		 * item.setProvince(StringUtils.EMPTY); item.setCity(StringUtils.EMPTY);
		 * item.setRegion(StringUtils.EMPTY); item.setBirthday(LocalDate.of(1900, 1,
		 * 1)); item.setIdentityType((short) 99); // }
		 *//*
			 * else { IdCardInfo idCardInfo =
			 * idCardInfoExtractorUtil.idCardInfo(credentialNum); LocalDate birthday =
			 * idCardInfo.getBirthday(); item.setBirthday(birthday == null ?
			 * LocalDate.of(1900, 1, 1) : birthday); String city = idCardInfo.getCity();
			 * item.setCity(city == null ? StringUtils.EMPTY : city); String province =
			 * idCardInfo.getProvince(); item.setProvince(province == null ?
			 * StringUtils.EMPTY : province); String region = idCardInfo.getRegion();
			 * item.setRegion(region == null ? StringUtils.EMPTY : region); Integer gender =
			 * idCardInfo.getGender(); if (gender != null) { item.setGender(gender); } }
			 *//*
				 * } catch (Exception e) { item.setCredentialNum(StringUtils.EMPTY);
				 * log.error("获取身份证号码错误", e); } finally { countDownLatch.countDown(); } })); try
				 * { countDownLatch.await(); } catch (InterruptedException e) {
				 * log.error("countDownLatch.await() 报错了", e); }
				 */
		return result;
	}

	/***
	 * 获取身份证号码，从dnake平台接口
	 * 
	 * @param householdId 用户id
	 * @return java.lang.String
	 * @author shuyy
	 * @date 2018/11/21 14:14
	 * @company mitesofor
	 */
	public String getCredentialNumFromDnake(Integer householdId) throws IOException {
		return this.getCredentialNumFromDnake(householdId, 1);
	}

	/***
	 * 获取身份证号码，从dnake平台接口
	 * 
	 * @param householdId 用户id
	 * @param retryNum    重试次数
	 * @return java.lang.String
	 * @author shuyy
	 * @date 2018/11/21 14:13
	 */
	private String getCredentialNumFromDnake(Integer householdId, int retryNum) throws IOException {
		String url = "http://cmp.ishanghome.com/cmp/household/getStepOneInfo";
		NameValuePair[] data = { new NameValuePair("householdId", householdId.toString()) };
		String result = httpLogin.post(url, data, httpLogin.getCookie());
		if (StringUtils.isBlank(result) || result.equals(CommonConstatn.ERROR)) {
			int retryNumMax = 10;
			if (retryNum > retryNumMax) {
				return StringUtils.EMPTY;
			}
			httpLogin.loginUser();
			retryNum++;
			return this.getCredentialNumFromDnake(householdId, retryNum);
		}
		JSONObject stepOneInfo = null;
		JSONObject jsonObject = JSON.parseObject(result);
		if (jsonObject != null) {
			stepOneInfo = jsonObject.getJSONObject("stepOneInfo");
		}
		if (stepOneInfo == null) {
			return StringUtils.EMPTY;
		} else {
			String credentialNum = stepOneInfo.getString("credentialNum");
			if (StringUtils.isBlank(credentialNum)) {
				return StringUtils.EMPTY;
			} else {
				return credentialNum;
			}
		}
	}

	/***
	 * 从dnake平台获取身份证信息，更新
	 * 
	 * @param List<HouseHold> 住户列表
	 * @author shuyy
	 * @date 2018/12/08 15:32
	 */
	public List<HouseHold> getIdCardInfoFromDnake(List<HouseHold> list) {
		List<HouseHold> result = Lists.newCopyOnWriteArrayList();
		// 准备最大跑100个线程
		int threadSize = list.size() > 100 ? 100 : list.size();
		int preThreadDutyNum = list.size() / threadSize + 1;
		List<String> numList = Lists.newCopyOnWriteArrayList();
		CountDownLatch countDownLatch = new CountDownLatch(threadSize);
		for (int i = 0; i <threadSize; i++) {
			int tmp = i;
			ThreadPoolUtil.execute(() -> {
				try {
					for (int index = tmp * preThreadDutyNum, num = 0; num < preThreadDutyNum; index++) {
						if(index >= list.size()) {
							num++;
							continue;
						}
						HouseHold item = list.get(index);
						// 查询身份证号
						String credentialNum = getCredentialNumFromDnake(item.getHouseholdId());
						if(credentialNum.equals(StringUtils.EMPTY)) {
							num++;
							numList.add("a");
							continue;
						}
						result.add(item);
						item.setCredentialNum(credentialNum);
						item.setIdentityType(HouseHold.NORMAL);
						// 通过身份证号，分析省、市、区县、出生日期、年龄
						IdCardInfo idCardInfo = idCardInfoExtractorUtil.idCardInfo(credentialNum);
						LocalDate birthday = idCardInfo.getBirthday();
						if (birthday != null) {
							item.setBirthday(birthday);
						}
						String city = idCardInfo.getCity();
						if (city != null) {
							item.setCity(city);
						}
						String province = idCardInfo.getProvince();
						if (province != null) {
							item.setProvince(province);
						}
						String region = idCardInfo.getRegion();
						if (region != null) {
							item.setRegion(region);
						}
						Integer gender = idCardInfo.getGender();
						if (gender != null) {
							item.setGender(gender);
						}
						num++;
					}
				} catch (Exception e) {
					log.error("获取身份证号码错误", e);
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			log.error("countDownLatch.await() 报错了", e);
		}
		System.out.println(numList.size() + result.size());
		return result;
	}

	/***
	 * 从Dnake接口查询住户
	 * 
	 * @param communityCode 社区code
	 * @param pageNum       分页num
	 * @param pageSize      分页size
	 * @param param         其他参数
	 * @return 住户信息列表
	 * @author shuyy
	 * @date 2018/11/19 17:39
	 */
	private List<HouseHold> listFromDnakeByCommunityCodePage(String communityCode, Integer pageNum, Integer pageSize,
			Map<String, Object> param) {
		String url = "/v1/household/getHouseholdList";
		HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(10);
		map.put("communityCode", communityCode);
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		if (param != null && !param.isEmpty()) {
			map.putAll(param);
		}
		String result = DnakeWebApiUtil.invoke(url, map);
		return parseJSON(communityCode, result);
	}

	private ArrayList<HouseHold> parseJSON(String communityCode, String result) {
		JSONObject jsonResult = JSON.parseObject(result);
		JSONArray householdList = jsonResult.getJSONArray("householdList");
		if (householdList == null || householdList.isEmpty()) {
			return Lists.newArrayListWithCapacity(0);
		}
		ArrayList<HouseHold> householdResultList = Lists.newArrayListWithCapacity(householdList.size());
		String communityName = clusterCommunityService.getByCommunityCode(communityCode).getCommunityName();
		for (int i = 0; i < householdList.size(); i++) {
			// 构建household对象residenceTime"residenceTime" -> "2023-08-15 00:00:00"
			HouseHold houseHold = householdList.getObject(i, HouseHold.class);
			houseHold.setCommunityName(communityName);
			houseHold.setCommunityCode(communityCode);
			String zoneName = houseHold.getZoneName();
			// dnake接口返回数据有问题，这里进行转换
			String needConvertZoneName = "凯翔国际外滩";
			if (zoneName.equals(needConvertZoneName)) {
				zoneName = "凯翔外滩国际";
			}
			Zone zone = zoneService.getByNameAndCommunityCode(zoneName, communityCode);
			if (zone == null) {
				householdList.remove(i--);
				continue;
			}
			Integer zoneId = zone.getZoneId();
			houseHold.setZoneId(zoneId);
			houseHold.setZoneName(zoneName);
			String buildingName = houseHold.getBuildingName();
			Building building = buildingService.getByNameAndZoneId(buildingName, zoneId);
			if (building == null) {
				householdList.remove(i--);
				continue;
			}
			Integer buildingId = building.getBuildingId();
			houseHold.setBuildingId(buildingId);
			String unitName = houseHold.getUnitName();
			Unit unit = unitService.getByNameAndBuildingId(unitName, buildingId);
			if (unit == null) {
				householdList.remove(i--);
				continue;
			}
			Integer unitId = unit.getUnitId();
			houseHold.setUnitId(unitId);
			houseHold.setGmtCreate(LocalDateTime.now());
			houseHold.setGmtModified(LocalDateTime.now());
			if (houseHold.getSipAccount() == null) {
				houseHold.setSipAccount(StringUtils.EMPTY);
			}
			if (houseHold.getSipPassword() == null) {
				houseHold.setSipPassword(StringUtils.EMPTY);
			}
			if (houseHold.getHouseholdType() == null) {
				houseHold.setHouseholdType(1);
			}
			Room room = roomService.getByUnitIdAndRoomNum(houseHold.getRoomNum(), houseHold.getUnitId());
			if (room != null) {
				houseHold.setRoomId(room.getRoomId());
			} else {
				houseHold.setRoomId(0);
			}
			Integer health = 1;
			if (!health.equals(houseHold.getHouseholdStatus())) {
				continue;
			}
			houseHold.setCredentialNum(StringUtils.EMPTY);
			houseHold.setIdentityType(HouseHold.NORMAL);
			houseHold.setProvince(StringUtils.EMPTY);
			houseHold.setCity(StringUtils.EMPTY);
			houseHold.setRegion(StringUtils.EMPTY);
			houseHold.setBirthday(CommonConstatn.NULL_LOCAL_DATE);
			houseHold.setIdentityType((short) 99);
//            JSONObject jsonObject = householdList.getJSONObject(i);
//            parseDoorDevice(jsonObject, houseHold);
//            parseAppDevice(jsonObject, houseHold);
			householdResultList.add(houseHold);
		}
		return householdResultList;
	}

	/***
	 * 删除所有
	 * 
	 * @author shuyy
	 * @date 2018/11/19 17:45
	 */
	public void remove() {
		EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
		wrapper.setSqlSelect("1=1; truncate household");
		houseHoldMapper.delete(wrapper);
	}

	/***
	 * 查询househod， 通过住户id
	 * 
	 * @param householdId 住户id
	 * @return com.mit.community.entity.HouseHold
	 * @author shuyy
	 * @date 2018/11/23 14:51
	 */
	HouseHold getByHouseholdId(Integer householdId) {
		EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
		wrapper.eq("household_id", householdId);
		List<HouseHold> houseHolds = houseHoldMapper.selectList(wrapper);
		if (houseHolds.isEmpty()) {
			return null;
		} else {
			return houseHolds.get(0);
		}
	}

	/***
	 * 查询有住户的所有房间id
	 * 
	 * @return java.util.List<java.util.Map < java.lang.String , java.lang.Object>>
	 * @author shuyy
	 * @date 2018/11/24 9:38
	 */
	public List<Map<String, Object>> listActiveRoomId() {
		EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
		wrapper.groupBy("room_id");
		wrapper.setSqlSelect("room_id");
		return houseHoldMapper.selectMaps(wrapper);
	}

	/***
	 * 查询所有住户， 通过房间id
	 * 
	 * @param roomId 房间号
	 * @return java.util.List<com.mit.community.entity.HouseHold>
	 * @author shuyy
	 * @date 2018/11/24 9:42
	 */
	public List<HouseHold> listByRoomId(Integer roomId) {
		EntityWrapper<HouseHold> wrapper = new EntityWrapper<>();
		wrapper.eq("room_id", roomId);
		return houseHoldMapper.selectList(wrapper);
	}

}
