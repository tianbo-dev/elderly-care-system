package com.lekang.elderlycare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lekang.elderlycare.common.Result;
import com.lekang.elderlycare.entity.Device;
import com.lekang.elderlycare.mapper.DeviceMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceMapper deviceMapper;

    public DeviceController(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    @GetMapping
    public Result<Page<Device>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(deviceMapper.selectPage(new Page<>(pageNum, pageSize), null));
    }
}
