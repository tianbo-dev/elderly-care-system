<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-input v-model="search.name" placeholder="老人姓名" style="width: 160px" clearable />
        <el-input v-model="search.roomNo" placeholder="房间号" style="width: 160px" clearable />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="openDialog()">新增老人</el-button>
      </div>

      <el-table :data="pageData.records" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="roomNo" label="房间号" width="120" />
        <el-table-column prop="guardianName" label="监护人" width="100" />
        <el-table-column prop="guardianPhone" label="联系电话" width="130" />
        <el-table-column prop="checkStatus" label="入住进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="(row.checkStatus / 7) * 100" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="residentStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.residentStatus === 1 ? 'success' : 'info'">
              {{ row.residentStatus === 1 ? '在住' : '退住' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.checkStatus < 7 && row.residentStatus === 1"
              link type="warning" @click="openAudit(row)"
            >审核</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑老人' : '新增老人'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="10"><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="性别" label-width="56px">
            <el-radio-group v-model="form.gender"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio></el-radio-group>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="年龄" label-width="56px"><el-input v-model="form.age" placeholder="请输入年龄" maxlength="3" @input="onAgeInput" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="身份证"><el-input v-model="form.idCard" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房间号"><el-input v-model="form.roomNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="床位号"><el-input v-model="form.bedNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入住日期"><el-date-picker v-model="form.checkInDate" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="监护人"><el-input v-model="form.guardianName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.guardianPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关系"><el-input v-model="form.guardianRel" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="健康状况"><el-input v-model="form.healthStatus" type="textarea" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入住进度">
            <el-select v-model="form.checkStatus" style="width:100%">
              <el-option v-for="(s, i) in checkSteps" :key="i" :value="i" :label="i + '. ' + s" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 入住审核对话框 -->
    <el-dialog v-model="auditVisible" title="入住审核" width="560px">
      <el-descriptions :column="2" border size="small" class="audit-info">
        <el-descriptions-item label="姓名">{{ auditRow?.name }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ auditRow?.age }} 岁</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ auditRow?.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="床位号">{{ auditRow?.bedNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="监护人">{{ auditRow?.guardianName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ auditRow?.guardianPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前进度" :span="2">
          <el-tag size="small">{{ checkSteps[auditRow?.checkStatus || 0] }}</el-tag>
          <span class="audit-step-tip">（第 {{ auditRow?.checkStatus || 0 }} / 7 步）</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-form label-width="90px" style="margin-top: 16px">
        <el-form-item label="审核结论">
          <el-radio-group v-model="auditForm.result">
            <el-radio value="pass">通过审核（进入入住配置，家属端继续办理）</el-radio>
            <el-radio value="reject">退回修改（退回申请状态）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="auditForm.remark"
            type="textarea" :rows="3" maxlength="200" show-word-limit
            :placeholder="auditForm.result === 'reject' ? '退回必填，请填写退回原因' : '选填，如：资料齐全，同意入住'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button :type="auditForm.result === 'pass' ? 'success' : 'danger'" @click="submitAudit">
          {{ auditForm.result === 'pass' ? '确认通过' : '确认退回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const checkSteps = ['未开始', '申请', '评估', '审核', '配置', '签约', '缴费', '完成入住']
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const pageData = ref({ records: [], total: 0 })
const search = ref({ name: '', roomNo: '' })
const dialogVisible = ref(false)
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/residents', { params: { ...search.value, pageNum: pageNum.value, pageSize: pageSize.value } })
    pageData.value = res.data
  } catch {} finally { loading.value = false }
}

const resetSearch = () => { search.value = { name: '', roomNo: '' }; loadData() }

const openDialog = (row) => {
  if (row) {
    form.value = { ...row }
  } else {
    form.value = { gender: 1, age: 65, checkStatus: 0, residentStatus: 1 }
  }
  dialogVisible.value = true
}

// 年龄输入实时清洗：只允许非负整数，范围 0-150，输入即时可见
const onAgeInput = (val) => {
  let v = String(val == null ? '' : val).replace(/[^\d]/g, '')
  v = v.replace(/^0+(?=\d)/, '')
  if (v !== '' && Number(v) > 150) v = '150'
  form.value.age = v
}

const handleSave = async () => {
  const raw = form.value.age
  if (raw === '' || raw == null) {
    ElMessage.warning('请输入年龄')
    return
  }
  const age = Number(raw)
  if (isNaN(age) || age < 0 || age > 150) {
    ElMessage.warning('请输入正确的年龄（0-150 岁）')
    return
  }
  form.value.age = age
  try {
    if (form.value.id) {
      await request.put('/residents', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/residents', form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {}
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该老人信息？', '提示', { type: 'warning' })
  try {
    await request.delete('/residents/' + row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

// ============ 入住审核 ============
// 参照机构 7 步流程（申请→评估→审核→配置→签约→缴费→完成入住）
// 审核是第 3 步关卡：通过 → 进入入住配置(4)，由家属端继续配置/签约/缴费；退回 → 退回申请状态(1)
const auditVisible = ref(false)
const auditRow = ref(null)
const auditForm = ref({ result: 'pass', remark: '' })

const openAudit = (row) => {
  auditRow.value = row
  auditForm.value = { result: 'pass', remark: '' }
  auditVisible.value = true
}

const submitAudit = async () => {
  const pass = auditForm.value.result === 'pass'
  const remark = (auditForm.value.remark || '').trim()
  if (!pass && remark.length < 2) {
    ElMessage.warning('退回时必须填写退回原因（至少 2 个字）')
    return
  }
  const target = pass ? 4 : 1
  const prefix = pass ? '【审核通过】' : '【审核退回】'
  const newRemark = remark ? prefix + remark : prefix + (pass ? '审核通过，进入入住配置' : '请修改后重新提交')
  try {
    await request.put('/residents', {
      ...auditRow.value,
      checkStatus: target,
      remark: newRemark
    })
    ElMessage.success(pass ? '审核通过，家属端可继续办理配置/签约/缴费' : '已退回申请状态，请通知家属修改')
    auditVisible.value = false
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
.audit-step-tip { color: #909399; font-size: 12px; margin-left: 8px; }
</style>
