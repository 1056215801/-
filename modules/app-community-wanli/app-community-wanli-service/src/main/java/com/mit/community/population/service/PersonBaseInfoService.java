package com.mit.community.population.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.mit.community.entity.entity.PersonBaseInfo;
import com.mit.community.mapper.mapper.PersonBaseInfoMapper;
import com.mit.community.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人员基本信息
 *
 * @author xiong
 * @date 2019/5/25
 * @company mitesofor
 */
@Service
public class PersonBaseInfoService {
    @Autowired
    private PersonBaseInfoMapper personBaseInfoMapper;

    public Integer save(int age, String idCardNum, String name, String formerName, String gender, LocalDateTime birthday, String nation, String nativePlace, String matrimony, String politicCountenance,
                        String education, String religion, String jobType, String profession, String cellphone, String placeOfDomicile, String placeOfDomicileDetail, String placeOfReside,
                        String placeOfResideDetail, String placeOfServer, String photoBase64) {
        PersonBaseInfo personBaseInfo = new PersonBaseInfo(idCardNum, name, formerName, gender, birthday, nation, nativePlace, matrimony, politicCountenance, education, religion, jobType, profession, cellphone, placeOfDomicile,
                placeOfDomicileDetail, placeOfReside, placeOfResideDetail, placeOfServer, photoBase64, 0, age,0,null);
        personBaseInfo.setGmtCreate(LocalDateTime.now());
        personBaseInfo.setGmtModified(LocalDateTime.now());
        personBaseInfoMapper.insert(personBaseInfo);
        return personBaseInfo.getId();
    }

    public boolean isExist (String idCardNum) {
        boolean flag = true;
        EntityWrapper<PersonBaseInfo> wrapper = new EntityWrapper<>();
        wrapper.eq("id_card_num", idCardNum);
        List<PersonBaseInfo> list = personBaseInfoMapper.selectList(wrapper);
        if (list.isEmpty()) {
            flag = false;
        }
        return flag;
    }

    public void updateByIdCardNum(int age, String idCardNum, String name, String formerName, String gender, LocalDateTime birthday,
                                  String nation, String nativePlace, String matrimony, String politicCountenance, String education, String religion, String jobType, String profession, String cellphone, String placeOfDomicile,
                                  String placeOfDomicileDetail, String placeOfReside, String placeOfResideDetail, String placeOfServer, String base64) {
        PersonBaseInfo personBaseInfo = new PersonBaseInfo(idCardNum, name, formerName, gender, birthday, nation, nativePlace, matrimony, politicCountenance, education, religion, jobType, profession, cellphone, placeOfDomicile,
                placeOfDomicileDetail, placeOfReside, placeOfResideDetail, placeOfServer, base64, 0, age,0,null);
        personBaseInfo.setGmtModified(LocalDateTime.now());
        EntityWrapper<PersonBaseInfo> wrapper = new EntityWrapper<>();
        wrapper.eq("id_card_num", idCardNum);
        personBaseInfoMapper.update(personBaseInfo, wrapper);
    }

    @Transactional
    public void delete(Integer id) {
        PersonBaseInfo personBaseInfo = new PersonBaseInfo();
        personBaseInfo.setIsDelete(1);
        EntityWrapper<PersonBaseInfo> dalete = new EntityWrapper<>();
        dalete.eq("id", id);
        personBaseInfoMapper.update(personBaseInfo, dalete);
    }

}
