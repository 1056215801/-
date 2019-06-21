package com.mit.community.population.service;

import com.mit.community.mapper.mapper.PersonLabelsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonLabelsService {
    @Autowired
    private PersonLabelsMapper labelsMapper;

    public void saveLabels(String labels,Integer userId){
        labelsMapper.saveLabels(labels, userId);
    }

}