package com.mit.community.service;

import com.ace.cache.annotation.Cache;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.mit.community.entity.Zone;
import com.mit.community.mapper.ZoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分区业务处理层
 * @author Mr.Deng
 * @date 2018/12/7 17:50
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: mitesofor </p>
 */
@Service
public class ZoneService {
    @Autowired
    private ZoneMapper zoneMapper;

    /**
     * 查询分区信息，通过小区code
     * @param communityCode 小区code
     * @return 分区列表
     * @author Mr.Deng
     * @date 17:57 2018/12/7
     */
    public List<Zone> listByCommunityCode(String communityCode) {
        EntityWrapper<Zone> wrapper = new EntityWrapper<>();
        wrapper.eq("community_code", communityCode);
        wrapper.eq("zone_status", 1);
        return zoneMapper.selectList(wrapper);
    }

    /**
     * 查询分区信息，通过分区
     * @param zoneId 分区Id
     * @return 分区信息
     * @author Mr.Deng
     * @date 14:09 2018/12/21
     */
    @Cache(key = "zone:communityCode:zoneId:{1}:{2}")
    public Zone getByZoneId(String communityCode, Integer zoneId) {
        EntityWrapper<Zone> wrapper = new EntityWrapper<>();
        wrapper.eq("zone_id", zoneId);
        wrapper.eq("community_code", communityCode);
        wrapper.eq("zone_status", 1);
        List<Zone> zones = zoneMapper.selectList(wrapper);
        if (zones.isEmpty()) {
            return null;
        }
        return zones.get(0);
    }
    public Zone getByCommunityName(String communityName) {
        EntityWrapper<Zone> wrapper = new EntityWrapper<>();
        wrapper.eq("zone_name", communityName);
        List<Zone> zones = zoneMapper.selectList(wrapper);
        if (zones.isEmpty()) {
            return null;
        }
        return zones.get(0);
    }

    public Zone getByCommunityCode(String communityCode) {
        EntityWrapper<Zone> wrapper = new EntityWrapper<>();
        wrapper.eq("community_code", communityCode);
        wrapper.eq(false,"zone_name","默认分区");
        List<Zone> zones = zoneMapper.selectList(wrapper);
        if (zones.isEmpty()) {
            return null;
        }
        return zones.get(0);
    }

    public Zone getByZoneId( Integer zoneId) {
        EntityWrapper<Zone> wrapper = new EntityWrapper<>();
        wrapper.eq("zone_id", zoneId);
        wrapper.eq("zone_status", 1);
        List<Zone> zones = zoneMapper.selectList(wrapper);
        if (zones.isEmpty()) {
            return null;
        }
        return zones.get(0);
    }
}
