package com.lekang.elderlycare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lekang.elderlycare.entity.Alarm;
import com.lekang.elderlycare.entity.Resident;
import com.lekang.elderlycare.mapper.AlarmMapper;
import com.lekang.elderlycare.mapper.ResidentMapper;
import com.lekang.elderlycare.security.LoginUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmService {

    private final AlarmMapper alarmMapper;
    private final ResidentMapper residentMapper;

    public AlarmService(AlarmMapper alarmMapper, ResidentMapper residentMapper) {
        this.alarmMapper = alarmMapper;
        this.residentMapper = residentMapper;
    }

    public Page<Alarm> page(int pageNum, int pageSize, Integer status, LoginUser user) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Alarm::getStatus, status);
        }
        applyFamilyScope(wrapper, user);
        wrapper.orderByDesc(Alarm::getAlarmTime);
        return alarmMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public void handle(Long id, String handleBy, String handleRemark) {
        Alarm alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw new RuntimeException("告警不存在");
        }
        alarm.setStatus(1);
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setHandleBy(handleBy);
        alarm.setHandleRemark(handleRemark);
        alarmMapper.updateById(alarm);
    }

    public long countUnhandled(LoginUser user) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alarm::getStatus, 0);
        applyFamilyScope(wrapper, user);
        return alarmMapper.selectCount(wrapper);
    }

    /**
     * 数据权限：家属只能查看自己绑定老人的告警，管理员查看全部
     */
    private void applyFamilyScope(LambdaQueryWrapper<Alarm> wrapper, LoginUser user) {
        if (user == null || !user.isFamily()) {
            return;
        }
        List<Long> residentIds = residentMapper.selectList(new LambdaQueryWrapper<Resident>()
                        .eq(Resident::getUserId, user.userId())
                        .select(Resident::getId))
                .stream().map(Resident::getId).toList();
        // 家属未绑定任何老人时返回空集（-1 保证 SQL 合法且无结果）
        wrapper.in(Alarm::getResidentId, residentIds.isEmpty() ? List.of(-1L) : residentIds);
    }
}
