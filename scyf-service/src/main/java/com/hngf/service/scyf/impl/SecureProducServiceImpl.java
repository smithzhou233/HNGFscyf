package com.hngf.service.scyf.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.List;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hngf.common.utils.PageUtils;
import com.hngf.common.utils.Constant;

import com.hngf.entity.scyf.SecureProduc;
import com.hngf.service.scyf.SecureProducService;
import com.hngf.mapper.scyf.SecureProducMapper;
@Service("SecureProducService")
public class SecureProducServiceImpl implements SecureProducService {

    @Autowired
    private SecureProducMapper secureProducMapper;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        int pageNum = null!= params.get("pageNum") ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        int pageSize = null != params.get("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : Constant.PAGE_SIZE;
        //利用PageHelper分页查询 注意：这个一定要放查询语句的前一行,否则无法进行分页,因为它对紧随其后第一个sql语句有效
        PageHelper.startPage(pageNum, pageSize);
        List<SecureProduc> list = secureProducMapper.findList(params);
        PageInfo<SecureProduc> pageInfo = new PageInfo<SecureProduc>(list);
        return new PageUtils(list,(int)pageInfo.getTotal(),pageInfo.getPages(),pageInfo.getPageSize(),pageInfo.getPrePage() + 1);
    }

    @Override
    public SecureProduc getById(Long id){
        return secureProducMapper.findById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SecureProduc SecureProduc) {
        secureProducMapper.add(SecureProduc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SecureProduc SecureProduc) {
        secureProducMapper.update(SecureProduc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List ids) {
        secureProducMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        secureProducMapper.deleteById(id);
    }
}
