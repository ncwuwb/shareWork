package com.jy.sharework.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.mapper.BizSpaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BizSpaceService {
    private final BizSpaceMapper bizSpaceMapper;

    public List<BizSpace> listByParent(long parentId) {
        return bizSpaceMapper.selectList(
                new LambdaQueryWrapper<BizSpace>()
                        .eq(BizSpace::getParentId, parentId)
                        .orderByAsc(BizSpace::getSort)
                        .orderByAsc(BizSpace::getId));
    }

    public BizSpace get(Long id) {
        return bizSpaceMapper.selectById(id);
    }

    public void save(BizSpace s) {
        if (s.getId() == null) {
            bizSpaceMapper.insert(s);
        } else {
            bizSpaceMapper.updateById(s);
        }
    }

    public void delete(Long id) {
        bizSpaceMapper.deleteById(id);
    }
}
