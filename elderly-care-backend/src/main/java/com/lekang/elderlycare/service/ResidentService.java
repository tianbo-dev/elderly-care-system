package com.lekang.elderlycare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lekang.elderlycare.entity.Device;
import com.lekang.elderlycare.entity.Resident;
import com.lekang.elderlycare.mapper.DeviceMapper;
import com.lekang.elderlycare.mapper.ResidentMapper;
import com.lekang.elderlycare.security.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ResidentService {

    private final ResidentMapper residentMapper;
    private final DeviceMapper deviceMapper;

    public ResidentService(ResidentMapper residentMapper, DeviceMapper deviceMapper) {
        this.residentMapper = residentMapper;
        this.deviceMapper = deviceMapper;
    }

    public Page<Resident> page(int pageNum, int pageSize, String name, String roomNo,
                               Integer residentStatus, LoginUser user) {
        LambdaQueryWrapper<Resident> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Resident::getName, name);
        }
        if (StringUtils.hasText(roomNo)) {
            wrapper.like(Resident::getRoomNo, roomNo);
        }
        // 在住/退住过滤（退住管理页使用）
        if (residentStatus != null) {
            wrapper.eq(Resident::getResidentStatus, residentStatus);
        }
        // 数据权限：家属只能查看自己绑定的老人，管理员查看全部
        if (user != null && user.isFamily()) {
            wrapper.eq(Resident::getUserId, user.userId());
        }
        // 按 ID 升序展示，保证列表编号 1→N 连续可读
        wrapper.orderByAsc(Resident::getId);
        return residentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public Resident getById(Long id, LoginUser user) {
        Resident resident = residentMapper.selectById(id);
        if (resident == null) {
            return null;
        }
        if (user != null && user.isFamily() && !user.userId().equals(resident.getUserId())) {
            throw new RuntimeException("无权查看该老人信息");
        }
        return resident;
    }

    /**
     * 家属端入住申请：创建老人档案并绑定当前家属，进入第 1 步（申请入住），同步派发手环
     */
    public Resident apply(Resident body, LoginUser user) {
        if (!StringUtils.hasText(body.getName())) {
            throw new IllegalArgumentException("老人姓名不能为空");
        }
        if (body.getAge() == null || body.getAge() < 0 || body.getAge() > 150) {
            throw new IllegalArgumentException("请输入正确的年龄（0-150 岁）");
        }
        body.setId(null);
        body.setUserId(user.userId());
        body.setCheckStatus(1);
        body.setResidentStatus(1);
        residentMapper.insert(body);
        bindDefaultDevice(body);
        return body;
    }

    /**
     * 家属端按顺序推进入住流程（1→2→…→7），仅限本人绑定的老人
     */
    public void advanceStep(Long id, Integer target, LoginUser user) {
        Resident resident = residentMapper.selectById(id);
        if (resident == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!user.userId().equals(resident.getUserId())) {
            throw new RuntimeException("无权操作该老人的入住流程");
        }
        if (resident.getResidentStatus() == null || resident.getResidentStatus() != 1) {
            throw new RuntimeException("当前状态不可推进流程");
        }
        if (target == null || target < 2 || target > 7) {
            throw new RuntimeException("流程步骤不合法");
        }
        if (target != resident.getCheckStatus() + 1) {
            throw new RuntimeException("只能按顺序推进流程");
        }
        Resident upd = new Resident();
        upd.setId(id);
        upd.setCheckStatus(target);
        residentMapper.updateById(upd);
    }

    /**
     * 家属端发起退住申请：在住(1) → 退住申请中(2)，待机构确认
     */
    public void checkoutApply(Long id, LoginUser user) {
        Resident resident = residentMapper.selectById(id);
        if (resident == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!user.userId().equals(resident.getUserId())) {
            throw new RuntimeException("无权操作该老人的退住申请");
        }
        if (resident.getResidentStatus() == null || resident.getResidentStatus() != 1) {
            throw new RuntimeException("当前状态不可申请退住");
        }
        Resident upd = new Resident();
        upd.setId(id);
        upd.setResidentStatus(2);
        upd.setRemark("【家属退住申请】家属在小程序端发起退住申请，待机构确认");
        residentMapper.updateById(upd);
    }

    public void save(Resident resident) {
        validateAge(resident);
        if (resident.getCheckStatus() == null) {
            resident.setCheckStatus(0);
        }
        if (resident.getResidentStatus() == null) {
            resident.setResidentStatus(1);
        }
        residentMapper.insert(resident);
        bindDefaultDevice(resident);
    }

    public void update(Resident resident) {
        validateAge(resident);
        residentMapper.updateById(resident);
    }

    /**
     * 校验年龄取值范围：0-150 岁，防止录入 6959 这类异常数据
     */
    private void validateAge(Resident resident) {
        Integer age = resident.getAge();
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("年龄必须在 0-150 岁之间");
        }
    }

    /**
     * 入住即监护：新老人自动绑定跌倒监测手环（编号与老人ID一致，如 FALL-004），
     * 保证 MQTT 跌倒告警能通过 deviceId 匹配到该老人
     */
    private void bindDefaultDevice(Resident resident) {
        String deviceId = String.format("FALL-%03d", resident.getId());
        if (deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId)) > 0) {
            return;
        }
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setDeviceName("跌倒监测手环" + String.format("%03d", resident.getId()));
        device.setDeviceType("FALL");
        device.setResidentId(resident.getId());
        device.setStatus(1);
        deviceMapper.insert(device);
    }

    public void delete(Long id) {
        residentMapper.deleteById(id);
    }
}
