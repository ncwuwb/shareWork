package com.jy.sharework.controller.app;

import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.service.BizSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/spaces")
@RequiredArgsConstructor
public class AppSpaceController {
    private final BizSpaceService bizSpaceService;

    @GetMapping("/children")
    public R<List<BizSpace>> children(@RequestParam(defaultValue = "0") long parentId) {
        return R.ok(bizSpaceService.listByParent(parentId));
    }
}
