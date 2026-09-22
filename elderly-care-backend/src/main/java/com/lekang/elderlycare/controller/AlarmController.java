package com.lekang.elderlycare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lekang.elderlycare.common.Result;
import com.lekang.elderlycare.entity.Alarm;
import com.lekang.elderlycare.security.LoginUser;
import com.lekang.elderlycare.service.AlarmService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GetMapping
    public Result<Page<Alarm>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal LoginUser user) {
        return Result.success(alarmService.page(pageNum, pageSize, status, user));
    }

    @PostMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, String> body,
                               @AuthenticationPrincipal LoginUser user) {
        if (user == null || !user.isAdmin()) {
            return Result.error("无权限操作，告警仅限机构管理员处理");
        }
        try {
            alarmService.handle(id, body.get("handleBy"), body.get("handleRemark"));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/unhandled-count")
    public Result<Long> unhandledCount(@AuthenticationPrincipal LoginUser user) {
        return Result.success(alarmService.countUnhandled(user));
    }
}
