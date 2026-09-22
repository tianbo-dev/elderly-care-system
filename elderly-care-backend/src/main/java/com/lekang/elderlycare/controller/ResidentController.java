package com.lekang.elderlycare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lekang.elderlycare.common.Result;
import com.lekang.elderlycare.entity.Resident;
import com.lekang.elderlycare.security.LoginUser;
import com.lekang.elderlycare.service.ResidentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping
    public Result<Page<Resident>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String roomNo,
            @RequestParam(required = false) Integer residentStatus,
            @AuthenticationPrincipal LoginUser user) {
        return Result.success(residentService.page(pageNum, pageSize, name, roomNo, residentStatus, user));
    }

    @GetMapping("/{id}")
    public Result<Resident> getById(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        try {
            return Result.success(residentService.getById(id, user));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<Void> save(@RequestBody Resident resident, @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isAdmin()) {
            return Result.error("无权限操作");
        }
        try {
            residentService.save(resident);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 家属端：发起入住申请（进入 7 步流程第 1 步） */
    @PostMapping("/apply")
    public Result<Resident> apply(@RequestBody Resident body, @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isFamily()) {
            return Result.error("仅家属可发起入住申请");
        }
        try {
            return Result.success(residentService.apply(body, user));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 家属端：按顺序推进入住流程步骤 */
    @PutMapping("/{id}/step")
    public Result<Void> advanceStep(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body,
                                    @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isFamily()) {
            return Result.error("仅家属可操作入住流程");
        }
        try {
            residentService.advanceStep(id, body.get("checkStatus"), user);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 家属端：发起退住申请（在住 → 退住申请中，待机构确认） */
    @PostMapping("/{id}/checkout-apply")
    public Result<Void> checkoutApply(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isFamily()) {
            return Result.error("仅家属可发起退住申请");
        }
        try {
            residentService.checkoutApply(id, user);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Void> update(@RequestBody Resident resident, @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isAdmin()) {
            return Result.error("无权限操作");
        }
        try {
            residentService.update(resident);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isAdmin()) {
            return Result.error("无权限操作");
        }
        residentService.delete(id);
        return Result.success();
    }
}
