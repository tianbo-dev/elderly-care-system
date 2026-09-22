<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon resident"><el-icon :size="28"><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.residentCount }}</div>
            <div class="stat-label">在住老人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon alarm"><el-icon :size="28"><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.unhandledAlarms }}</div>
            <div class="stat-label">待处理告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon device"><el-icon :size="28"><Monitor /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.deviceCount }}</div>
            <div class="stat-label">在线设备</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon resident2"><el-icon :size="28"><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalAlarms }}</div>
            <div class="stat-label">累计告警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header><b>最新告警</b></template>
      <el-table :data="recentAlarms" stripe>
        <el-table-column prop="residentName" label="老人" width="120" />
        <el-table-column prop="alarmType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.alarmType === 'FALL' ? '跌倒' : row.alarmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const stats = ref({ residentCount: 0, unhandledAlarms: 0, deviceCount: 0, totalAlarms: 0 })
const recentAlarms = ref([])

onMounted(async () => {
  try {
    const alarmRes = await request.get('/alarms', { params: { pageNum: 1, pageSize: 5 } })
    recentAlarms.value = alarmRes.data.records
    stats.value.unhandledAlarms = await (await request.get('/alarms/unhandled-count')).data
    stats.value.totalAlarms = alarmRes.data.total
    const resRes = await request.get('/residents', { params: { pageNum: 1, pageSize: 1, residentStatus: 1 } })
    stats.value.residentCount = resRes.data.total || 0
    const devRes = await request.get('/devices', { params: { pageNum: 1, pageSize: 1 } })
    stats.value.deviceCount = devRes.data.total || 0
  } catch {}
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.stat-icon.resident { background: #14b8a6; }
.stat-icon.alarm { background: #f59e0b; }
.stat-icon.device { background: #3b82f6; }
.stat-icon.resident2 { background: #ef4444; }
.stat-value { font-size: 24px; font-weight: bold; color: #1f2937; }
.stat-label { color: #9ca3af; font-size: 13px; }
</style>
