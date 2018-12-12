package com.mit.community.service;

import com.ace.cache.annotation.Cache;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.mit.community.entity.Region;
import com.mit.community.mapper.RegionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 地区业务处理层
 * @author Mr.Deng
 * @date 2018/12/3 14:32
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: mitesofor </p>
 */
@Service
public class RegionService {
    @Autowired
    private RegionMapper regionMapper;

    /**
     * 查询城市编码信息，通过城市英文名
     * @param englishName 城市英文名
     * @return 城市编码信息
     * @author Mr.Deng
     * @date 15:15 2018/11/29
     */
    @Cache(key = "region:englishName:{1}")
    public Region getByEnglishName(String englishName) {
        EntityWrapper<Region> wrapper = new EntityWrapper<>();
        wrapper.eq("english_name", englishName);
        return regionMapper.selectList(wrapper).get(0);
    }

}
