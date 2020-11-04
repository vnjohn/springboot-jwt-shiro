package com.demo.controller;


import com.demo.entity.SysMenuEntity;
import com.demo.result.R;
import com.demo.service.SysMenuService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 菜单管理 前端控制器
 * </p>
 * @author vnjohn
 * @since 2020-11-04
 */
@RestController
@RequestMapping("/sys-menu")
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    @RequiresPermissions("sys:menu:list")
    @PostMapping("list")
    public R list(){
        List<SysMenuEntity> list = sysMenuService.list(null);
        return R.ok().put("data",list);
    }
}

