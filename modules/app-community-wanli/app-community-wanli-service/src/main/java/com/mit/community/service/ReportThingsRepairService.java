package com.mit.community.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.google.common.collect.Lists;
import com.mit.community.entity.HouseHold;
import com.mit.community.entity.HouseholdRoom;
import com.mit.community.entity.ReportThingRepairImg;
import com.mit.community.entity.ReportThingsRepair;
import com.mit.community.mapper.ReportThingsRepairMapper;
import com.mit.community.util.MakeOrderNumUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报事报修业务层
 * @author Mr.Deng
 * @date 2018/12/3 19:39
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: mitesofor </p>
 */
@Service
public class ReportThingsRepairService {
    @Autowired
    private ReportThingsRepairMapper reportThingsRepairMapper;

    @Autowired
    private ReportThingRepairImgService reportThingRepairImgService;

    @Autowired
    private HouseHoldService houseHoldService;
    @Autowired
    private HouseholdRoomService householdRoomService;

    /**
     * 添加保修报修数据
     * @param reportThingsRepair 报事报修数据
     * @return 添加条数
     * @author Mr.Deng
     * @date 19:47 2018/12/3
     */
    public Integer save(ReportThingsRepair reportThingsRepair) {
        reportThingsRepair.setGmtCreate(LocalDateTime.now());
        reportThingsRepair.setGmtModified(LocalDateTime.now());
        return reportThingsRepairMapper.insert(reportThingsRepair);
    }

    /**
     * 查询报事报修信息，通过报事报修id
     * @param id 报事报修id
     * @return 报事报修信息
     * @author Mr.Deng
     * @date 11:00 2018/12/5
     */
    public ReportThingsRepair getById(Integer id) {
        return reportThingsRepairMapper.selectById(id);
    }

    /**
     * 更新报事报修数据
     * @param reportThingsRepair 更新的数据
     * @return 更新条数
     * @author Mr.Deng
     * @date 11:03 2018/12/5
     */
    public Integer update(ReportThingsRepair reportThingsRepair) {
        reportThingsRepair.setGmtModified(LocalDateTime.now());
        return reportThingsRepairMapper.updateById(reportThingsRepair);
    }

    /**
     * 查询报事报修状态数据，通过住户id
     * @param householdId 住户id
     * @param status      保修状态 0、未完成。1、已完成
     * @return 报事报修数据列表
     * @author Mr.Deng
     * @date 20:49 2018/12/3
     */
    public List<ReportThingsRepair> listByStatus(Integer householdId, Integer status) {
        EntityWrapper<ReportThingsRepair> wrapper = new EntityWrapper<>();
        String[] s;
        //未完成
        if (status == 0) {
            s = new String[]{"business_success", "acceptance", "being_processed"};
        } else {
            s = new String[]{"remain_evaluated", "have_evaluation"};
        }
        wrapper.in("status", s);
        wrapper.eq("household_id", householdId);
        return reportThingsRepairMapper.selectList(wrapper);
    }

    /**
     * 查询报事报修状态数据，通过手机号
     * @param cellphone 手机号
     * @param status    保修状态 0、未完成。1、已完成
     * @return List<ReportThingsRepair>
     * @author Mr.Deng
     * @date 17:01 2018/12/11
     */
    public List<ReportThingsRepair> listReportThingsRepairByStatus(String cellphone, Integer status) {
        List<ReportThingsRepair> reportThingsRepairList = Lists.newArrayListWithExpectedSize(10);
        HouseHold houseHold = houseHoldService.getByCellphone(cellphone);
        List<ReportThingsRepair> reportThingsRepairs = listByStatus(houseHold.getHouseholdId(), status);
        reportThingsRepairList.addAll(reportThingsRepairs);
        return reportThingsRepairList;
    }

    /**
     * 申请报事报修
     * @param communityCode   小区code
     * @param roomId          房间id
     * @param roomNum         房间号
     * @param content         报事内容
     * @param reportUser      报事人
     * @param reportCellphone 联系人电话
     * @param maintainType    维修类型.关联字典code maintain_type 维修类型：1、水，2、电，3、可燃气，4、锁，5、其他
     * @param creatorUserId   创建用户id
     * @author Mr.Deng
     * @date 20:02 2018/12/3
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyReportThingsRepair(String communityCode, String cellphone, Integer roomId, String roomNum, String content,
                                        String reportUser, String reportCellphone, String maintainType, Integer creatorUserId,
                                        List<String> images) {
        String number = "B" + MakeOrderNumUtil.makeOrderNum();
        //报事成功code
        String status = "business_success";
        HouseHold houseHold = houseHoldService.getByCellphoneAndCommunityCode(cellphone, communityCode);
        if (houseHold != null) {
            Integer householdId = houseHold.getHouseholdId();
            HouseholdRoom householdRoom = householdRoomService.getByHouseholdIdAndRoomNum(householdId, roomNum);
            if (householdRoom != null) {
                ReportThingsRepair reportThingsRepair = new ReportThingsRepair(number, communityCode, householdRoom.getCommunityName(),
                        householdRoom.getZoneId(), householdRoom.getZoneName(), householdRoom.getBuildingId(), householdRoom.getBuildingName(),
                        householdRoom.getUnitId(), householdRoom.getUnitName(), roomId, roomNum, householdId,
                        content, status, reportUser, reportCellphone, LocalDateTime.now(), 0, 0,
                        0, 0, StringUtils.EMPTY, maintainType, creatorUserId);
                this.save(reportThingsRepair);
                if (!images.isEmpty()) {
                    for (String image : images) {
                        ReportThingRepairImg reportThingRepairImg = new ReportThingRepairImg(reportThingsRepair.getId(), image);
                        reportThingRepairImgService.save(reportThingRepairImg);
                    }
                }
            }
        }
    }

    /**
     * 报事报修评价
     * @param applyReportId             报事报修id
     * @param evaluateResponseSpeed     响应速度评价
     * @param evaluateResponseAttitude  响应态度评价
     * @param evaluateTotal             总体评价
     * @param evaluateServiceProfession 服务专业度评价
     * @param evaluateContent           评价内容
     * @author Mr.Deng
     * @date 10:57 2018/12/5
     */
    @Transactional(rollbackFor = Exception.class)
    public void evaluateReportThingsRepair(Integer applyReportId, Integer evaluateResponseSpeed, Integer evaluateResponseAttitude,
                                           Integer evaluateTotal, Integer evaluateServiceProfession, String evaluateContent) {
        String status = "have_evaluation";
        ReportThingsRepair reportThingsRepair = this.getById(applyReportId);
        reportThingsRepair.setEvaluateContent(evaluateContent);
        reportThingsRepair.setEvaluateResponseAttitude(evaluateResponseAttitude);
        reportThingsRepair.setEvaluateResponseSpeed(evaluateResponseSpeed);
        reportThingsRepair.setEvaluateServiceProfession(evaluateServiceProfession);
        reportThingsRepair.setEvaluateTotal(evaluateTotal);
        reportThingsRepair.setStatus(status);
        this.update(reportThingsRepair);
    }

}