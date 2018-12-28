package com.mit.community.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.Lists;
import com.mit.community.entity.ExpressAddress;
import com.mit.community.entity.ExpressReadUser;
import com.mit.community.mapper.ExpressAddressMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ExpressAddressService
 * @author Mr.Deng
 * @date 2018/12/14 16:51
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: mitesofor </p>
 */
@Service
public class ExpressAddressService {
    @Autowired
    private ExpressAddressMapper expressAddressMapper;
    @Autowired
    private ExpressInfoService expressInfoService;
    @Autowired
    private ExpressReadUserService expressReadUserService;

    /**
     * 添加快递位置信息
     * @param communityCode  小区code
     * @param name           快递名称
     * @param address        领取地址
     * @param imgUrl         图片地址
     * @param createUserName 创建人姓名
     * @author Mr.Deng
     * @date 16:03 2018/12/26
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(String communityCode, String name, String address, String imgUrl, String createUserName) {
        ExpressAddress expressAddress = new ExpressAddress();
        expressAddress.setCommunityCode(communityCode);
        expressAddress.setName(name);
        expressAddress.setAddress(address);
        expressAddress.setImgUrl(imgUrl);
        expressAddress.setCreateUserName(createUserName);
        expressAddress.setGmtCreate(LocalDateTime.now());
        expressAddress.setGmtModified(LocalDateTime.now());
        expressAddressMapper.insert(expressAddress);
    }

    /**
     * 更新快递数据
     * @param id             快递位置id
     * @param name           快递名
     * @param address        领取位置
     * @param imageUrl       图片地址
     * @param createUserName 添加人
     * @author Mr.Deng
     * @date 17:17 2018/12/26
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, String name, String address, String imageUrl, String createUserName) {
        ExpressAddress expressAddress = new ExpressAddress();
        expressAddress.setId(id);
        if (StringUtils.isNotBlank(name)) {
            expressAddress.setName(name);
        }
        if (StringUtils.isNotBlank(address)) {
            expressAddress.setAddress(address);
        }
        if (StringUtils.isNotBlank(imageUrl)) {
            expressAddress.setImgUrl(imageUrl);
        }
        if (StringUtils.isNotBlank(createUserName)) {
            expressAddress.setCreateUserName(createUserName);
        }
        expressAddress.setGmtModified(LocalDateTime.now());
        expressAddressMapper.updateById(expressAddress);
    }

    /**
     * 删除快递位置信息
     * @param id 快递位置信息
     * @author Mr.Deng
     * @date 17:30 2018/12/26
     */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Integer id) {
        expressAddressMapper.deleteById(id);
    }

    /**
     * 查询快递位置信息，通过小区code
     * @param communityCode 小区code
     * @return 快递位置信息
     * @author Mr.Deng
     * @date 17:02 2018/12/14
     */
    public List<Map<String, Object>> listByCommunityCode(String communityCode) {
        EntityWrapper<ExpressAddress> wrapper = new EntityWrapper<>();
        wrapper.eq("community_code", communityCode);
        return expressAddressMapper.selectMaps(wrapper);
    }

    /**
     * 查询快递位置信息，通过用户id和小区code
     * @param userId        用户id
     * @param communityCode 小区code
     * @return 快递位置信息
     * @author Mr.Deng
     * @date 17:15 2018/12/14
     */
    public List<Map<String, Object>> listExpressAddress(Integer userId, String communityCode) {
        List<Map<String, Object>> list = Lists.newArrayListWithExpectedSize(30);
        List<Map<String, Object>> expressAddresses = this.listByCommunityCode(communityCode);
        if (!expressAddresses.isEmpty()) {
            for (Map<String, Object> expressAddress : expressAddresses) {
                Integer integer = expressInfoService.countNotExpressNum(userId, Integer.parseInt(expressAddress.get("id").toString()));
                ExpressReadUser expressReadUser = expressReadUserService.ByUserIdAndExpressAddressId(userId, Integer.parseInt(expressAddress.get("id").toString()));
                if (expressReadUser == null) {
                    expressAddress.put("readStatus", false);
                }
                expressAddress.put("readStatus", true);
                expressAddress.put("expressNum", integer);
                list.add(expressAddress);
            }
        }
        return list;
    }

    /**
     * 分页获取本小区快递地址信息
     * @param communityCode  小区code
     * @param name           快递名称
     * @param address        快递地址
     * @param createUserName 领取位置
     * @param pageNum        页数
     * @param pageSize       一页数量
     * @return 分页快递地址信息
     * @author Mr.Deng
     * @date 9:23 2018/12/27
     */
    public Page<ExpressAddress> listPage(String communityCode, String name, String address, String createUserName,
                                         Integer pageNum, Integer pageSize) {
        Page<ExpressAddress> page = new Page<>(pageNum, pageSize);
        EntityWrapper<ExpressAddress> wrapper = new EntityWrapper<>();
        wrapper.orderBy("gmt_modified", false);
        wrapper.eq("community_code", communityCode);
        if (StringUtils.isNotBlank(name)) {
            wrapper.eq("name", name);
        }
        if (StringUtils.isNotBlank(address)) {
            wrapper.eq("address", address);
        }
        if (StringUtils.isNotBlank(createUserName)) {
            wrapper.eq("create_user_name", createUserName);
        }
        List<ExpressAddress> expressAddresses = expressAddressMapper.selectPage(page, wrapper);
        return page.setRecords(expressAddresses);
    }

}
