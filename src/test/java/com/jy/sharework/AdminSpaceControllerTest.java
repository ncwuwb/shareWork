package com.jy.sharework;

import com.jy.sharework.common.BusinessException;
import com.jy.sharework.config.ShareProperties;
import com.jy.sharework.controller.admin.AdminSpaceController;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import com.jy.sharework.service.BizSpaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSpaceControllerTest {

    @Mock
    private BizSpaceService bizSpaceService;

    @Mock
    private AdminScopeService adminScopeService;

    @Mock
    private ShareProperties shareProperties;

    @InjectMocks
    private AdminSpaceController adminSpaceController;

    @Test
    void areaAdminCannotCreateRootSpace() {
        LoginUser areaAdmin = new LoginUser(2L, "13800000000", 2, "", true);
        BizSpace space = new BizSpace();
        space.setParentId(0L);
        when(adminScopeService.isSuper(areaAdmin)).thenReturn(false);

        assertThrows(BusinessException.class, () -> adminSpaceController.save(areaAdmin, space));
    }
}
