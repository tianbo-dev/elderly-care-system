<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-select v-model="search.status" placeholder="处理状态" style="width:140px" clearable>
          <el-option :value="0" label="待处理" />
          <el-option :value="1" label="已处理" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="search.status = null; loadData()">重置</el-button>
      </div>

      <el-table :data="pageData.records" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="residentName" label="老人" width="120" />
        <el-table-column prop="deviceId" label="设备编号" width="140" />
        <el-table-column prop="alarmType" label="告警类型" width="100">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.alarmType === 'FALL' ? '跌倒' : row.alarmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="告警时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleBy" label="处理人" width="100" />
        <el-table-column prop="handleRemark" label="处理备注" show-overflow-tooltip />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="openHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="pageData.total || 0"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="handleVisible" title="处理告警" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="老人">{{ currentAlarm?.residentName }}</el-descriptions-item>
        <el-descriptions-item label="设备">{{ currentAlarm?.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ currentAlarm?.alarmTime }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag type="danger">跌倒</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form :model="handleForm" label-width="80px" style="margin-top:16px">
        <el-form-item label="处理人"><el-input v-model="handleForm.handleBy" placeholder="请输入处理人" /></el-form-item>
        <el-form-item label="处理备注"><el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const pageData = ref({ records: [], total: 0 })
const search = ref({ status: null })
const handleVisible = ref(false)
const currentAlarm = ref(null)
const handleForm = ref({ handleBy: '', handleRemark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (search.value.status !== null) params.status = search.value.status
    const res = await request.get('/alarms', { params })
    pageData.value = res.data
  } catch {} finally { loading.value = false }
}

const openHandle = (row) => {
  currentAlarm.value = row
  handleForm.value = { handleBy: 'admin', handleRemark: '' }
  handleVisible.value = true
}

const submitHandle = async () => {
  try {
    await request.post(`/alarms/${currentAlarm.value.id}/handle`, handleForm.value)
    ElMessage.success('处理成功')
    handleVisible.value = false
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
