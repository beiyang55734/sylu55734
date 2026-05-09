package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Choose;
import com.example.springboot.mapper.ChooseMapper;
import com.example.springboot.service.IChooseService;
import org.springframework.stereotype.Service;

@Service
public class ChooseServiceImpl extends ServiceImpl<ChooseMapper, Choose> implements IChooseService {

}