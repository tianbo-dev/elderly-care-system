package com.lekang.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resident")
public class Resident {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private Integer gender;
    private Integer age;
    private String idCard;
    private LocalDate birthday;
    private String roomNo;
    private String bedNo;
    private LocalDate checkInDate;
    private String healthStatus;
    private String guardianName;
    private String guardianPhone;
    private String guardianRel;
    private String photoOneInch;
    private String idCardFront;
    private String idCardBack;
    private Integer checkStatus;
    private Integer residentStatus;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
