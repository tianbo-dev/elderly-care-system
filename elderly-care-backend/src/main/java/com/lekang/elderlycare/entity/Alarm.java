package com.lekang.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm")
public class Alarm {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private Long residentId;
    private String residentName;
    private String alarmType;
    private LocalDateTime alarmTime;
    private Integer status;
    private LocalDateTime handleTime;
    private String handleBy;
    private String handleRemark;
    private LocalDateTime createTime;
}
