package com.jy.sharework;

import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.entity.SysAdminSpace;
import com.jy.sharework.mapper.BizSpaceMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import com.jy.sharework.mapper.SysAdminSpaceMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminScopeServiceTest {

    @Mock
    private SysAdminSpaceMapper sysAdminSpaceMapper;

    @Mock
    private BizSpaceMapper bizSpaceMapper;

    @Mock
    private BizWorkstationMapper bizWorkstationMapper;

    @InjectMocks
    private AdminScopeService adminScopeService;

    @Test
    void accessibleSpaceIdsIncludeDescendantsForAreaAdmin() {
        SysAdminSpace root = new SysAdminSpace();
        root.setUserId(9L);
        root.setSpaceId(1L);
        when(sysAdminSpaceMapper.selectList(any())).thenReturn(Collections.singletonList(root));
        when(bizSpaceMapper.selectList(any())).thenReturn(Arrays.asList(
                space(1L, 0L),
                space(2L, 1L),
                space(3L, 2L),
                space(4L, 0L)
        ));

        Set<Long> result = adminScopeService.accessibleSpaceIds(new LoginUser(9L, "13800000000", 2, "", true));

        assertEquals(new HashSet<>(Arrays.asList(1L, 2L, 3L)), result);
    }

    private BizSpace space(Long id, Long parentId) {
        BizSpace space = new BizSpace();
        space.setId(id);
        space.setParentId(parentId);
        return space;
    }
}
