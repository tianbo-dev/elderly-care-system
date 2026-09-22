<template>
  <div>
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="在住中" name="in" />
        <el-tab-pane label="退住申请" name="applying">
          <template #label>
            退住申请
            <el-badge v-if="applyCount > 0" :value="applyCount" style="margin-left: 4px" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已退住" name="out" />
      </el-tabs>

      <el-table :data="pageData.records" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="roomNo" label="房间号" width="120" />
        <el-table-column prop="bedNo" label="床位号" width="100" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="guardianName" label="监护人" width="100" />
        <el-table-column prop="guardianPhone" label="联系电话" width="130" />
        <el-table-column prop="residentStatus" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.residentStatus).type">{{ statusTag(row.residentStatus).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="activeTab === 'in'" label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleCheckout(row)">办理退住</el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'applying'" label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleConfirm(row, 0)">确认退住</el-button>
            <el-button link type="warning" @click="handleConfirm(row, 1)">驳回申请</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

// tab → residentStatus 映射（1=在住 2=退住申请中 0=已退住）
const statusMap = { in: 1, applying: 2, out: 0 }
const statusTagMap = {
  1: { text: '在住', type: 'success' },
  2: { text: '退住申请中', type: 'warning' },
  0: { text: '已退住', type: 'info' }
}
const statusTag = (s) => statusTagMap[s] || { text: '未知', type: 'info' }

const activeTab = ref('in')
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const pageData = ref({ records: [], total: 0 })
const applyCount = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    // 按当前 tab 的状态过滤（residentStatus: 1=在住 2=退住申请中 0=已退住）
    const res = await request.get('/residents', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        residentStatus: statusMap[activeTab.value]
      }
    })
    pageData.value = res.data
    // 退住申请角标
    if (activeTab.value === 'applying') {
      applyCount.value = res.data.total || 0
    } else {
      const c = await request.get('/residents', {
        params: { pageNum: 1, pageSize: 1, residentStatus: 2 }
      })
      applyCount.value = c.data.total || 0
    }
  } catch {} finally { loading.value = false }
}

const handleCheckout = async (row) => {
  await ElMessageBox.confirm(
    `确认为老人「${row.name}」办理退住？退住后其账号将不再接收实时告警推送。`,
    '退住确认',
    { type: 'warning', confirmButtonText: '确认退住', cancelButtonText: '取消' }
  )
  try {
    await request.put('/residents', { ...row, residentStatus: 0, remark: '【退住确认】机构办理退住，监测服务已停止' })
    ElMessage.success('退住办理成功')
    loadData()
  } catch {}
}

// 机构处理家属的退住申请：0=确认退住 1=驳回继续在住
const handleConfirm = async (row, target) => {
  const pass = target === 0
  await ElMessageBox.confirm(
    pass
      ? `确认「${row.name}」的退住申请并办理退住？`
      : `驳回「${row.name}」的退住申请？驳回后老人继续在住。`,
    pass ? '确认退住' : '驳回申请',
    { type: 'warning', confirmButtonText: pass ? '确认退住' : '驳回', cancelButtonText: '取消' }
  )
  try {
    await request.put('/residents', {
      ...row,
      residentStatus: target,
      remark: pass
        ? '【退住确认】家属退住申请已确认，监测服务已停止'
        : '【退住驳回】家属退住申请被驳回，老人继续在住'
    })
    ElMessage.success(pass ? '退住办理成功' : '已驳回申请')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
