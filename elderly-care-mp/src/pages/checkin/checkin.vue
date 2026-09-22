<template>
    <view class="page">
        <!-- 无进行中申请：入住申请表单 -->
        <view v-if="!elder">
            <view class="card">
                <view class="card-title">入住申请</view>
                <view class="form-tip">请填写老人基本信息，提交后进入七步办理流程</view>
                <view class="field"><text class="label">老人姓名 *</text><input class="ipt" v-model="applyForm.name" placeholder="请输入姓名" /></view>
                <view class="field">
                    <text class="label">性别</text>
                    <radio-group class="radio-row" @change="e => applyForm.gender = Number(e.detail.value)">
                        <label class="radio-item"><radio value="1" :checked="applyForm.gender === 1" color="#0f766e" />男</label>
                        <label class="radio-item"><radio value="2" :checked="applyForm.gender === 2" color="#0f766e" />女</label>
                    </radio-group>
                </view>
                <view class="field"><text class="label">年龄 *</text><input class="ipt" type="number" v-model="applyForm.age" placeholder="0-150" /></view>
                <view class="field"><text class="label">监护人姓名</text><input class="ipt" v-model="applyForm.guardianName" placeholder="请输入监护人姓名" /></view>
                <view class="field"><text class="label">联系电话</text><input class="ipt" type="number" v-model="applyForm.guardianPhone" placeholder="请输入联系电话" /></view>
                <view class="field"><text class="label">健康状况</text><textarea class="ta" v-model="applyForm.healthStatus" placeholder="既往病史、用药情况等" /></view>
                <button class="btn-primary" @click="submitApply">提交入住申请</button>
            </view>
        </view>

        <!-- 七步流程向导 -->
        <view v-else>
            <!-- 步骤条 -->
            <view class="card steps-card">
                <view class="steps-row">
                    <view class="step-wrap" v-for="(s, i) in steps" :key="i">
                        <view class="step">
                            <view class="dot" :class="{ done: elder.checkStatus > i + 1, cur: elder.checkStatus === i + 1 }">
                                <text v-if="elder.checkStatus > i + 1">✓</text>
                                <text v-else>{{ i + 1 }}</text>
                            </view>
                            <text class="step-name" :class="{ hl: elder.checkStatus === i + 1 }">{{ s }}</text>
                        </view>
                        <view v-if="i < 6" class="bar" :class="{ filled: elder.checkStatus > i + 1 }"></view>
                    </view>
                </view>
                <view class="elder-line">申请人：{{ elder.name }}（{{ elder.gender === 1 ? '男' : '女' }}，{{ elder.age }} 岁）</view>
            </view>

            <!-- 第 1 步：申请入住 -->
            <view class="card" v-if="elder.checkStatus === 1">
                <view class="card-title">① 申请入住</view>
                <view class="info-block">
                    <view class="kv"><text class="k">老人姓名</text><text class="v">{{ elder.name }}</text></view>
                    <view class="kv"><text class="k">监护人</text><text class="v">{{ elder.guardianName || '-' }} {{ elder.guardianPhone || '' }}</text></view>
                    <view class="kv"><text class="k">健康状况</text><text class="v">{{ elder.healthStatus || '未填写' }}</text></view>
                </view>
                <view class="tip">申请资料已提交，请核对无误后进入入住评估</view>
                <button class="btn-primary" @click="advance">保存并下一步</button>
            </view>

            <!-- 第 2 步：入住评估 -->
            <view class="card" v-else-if="elder.checkStatus === 2">
                <view class="card-title">② 入住评估</view>
                <view class="field"><text class="label">健康状况自评</text><textarea class="ta" v-model="assessText" placeholder="行动能力、慢病史、用药情况等" /></view>
                <view class="tip">评估结果将同步机构护理部，用于制定照护计划</view>
                <button class="btn-primary" @click="advance">保存并下一步</button>
            </view>

            <!-- 第 3 步：入住审核（机构操作） -->
            <view class="card" v-else-if="elder.checkStatus === 3">
                <view class="card-title">③ 入住审核</view>
                <view class="wait-box">
                    <view class="wait-icon">⏳</view>
                    <view class="wait-text">机构审核中，请耐心等待</view>
                    <view class="wait-sub">机构管理员将在后台完成资质审核，审核通过后可继续办理</view>
                </view>
            </view>

            <!-- 第 4 步：入住配置 -->
            <view class="card" v-else-if="elder.checkStatus === 4">
                <view class="card-title">④ 入住配置</view>
                <view class="info-block">
                    <view class="kv"><text class="k">房间号</text><text class="v">{{ elder.roomNo || '机构分配中' }}</text></view>
                    <view class="kv"><text class="k">床位号</text><text class="v">{{ elder.bedNo || '机构分配中' }}</text></view>
                    <view class="kv"><text class="k">监测手环</text><text class="v">FALL-{{ String(elder.id).padStart(3, '0') }}（已自动绑定）</text></view>
                </view>
                <view class="tip">请确认房间床位配置，如有异议请联系机构前台</view>
                <button class="btn-primary" @click="advance">确认配置并下一步</button>
            </view>

            <!-- 第 5 步：签约办理 -->
            <view class="card" v-else-if="elder.checkStatus === 5">
                <view class="card-title">⑤ 签约办理</view>
                <view class="contract">
                    <view class="contract-title">《入住服务协议（摘要）》</view>
                    <view class="contract-item">1. 甲方为入住老人提供住宿、膳食、医疗协助及 24 小时跌倒监测服务；</view>
                    <view class="contract-item">2. 乙方（家属）应如实告知老人健康状况，配合机构照护安排；</view>
                    <view class="contract-item">3. 费用按月结算，退住按实际入住天数核算；</view>
                    <view class="contract-item">4. 紧急情况下甲方有权先行处置并第一时间通知乙方。</view>
                </view>
                <button class="btn-primary" @click="advance">确认签约并下一步</button>
            </view>

            <!-- 第 6 步：首期缴费 -->
            <view class="card" v-else-if="elder.checkStatus === 6">
                <view class="card-title">⑥ 首期缴费</view>
                <view class="bill">
                    <view class="bill-note">注：首次缴费账单包含押金和月账单费用</view>
                    <view class="kv"><text class="k">账单编号</text><text class="v hl">{{ billNo }}</text></view>
                    <view class="kv"><text class="k">账单金额</text><text class="v hl strong">¥5,000.00</text></view>
                    <view class="kv"><text class="k">费用明细</text><text class="v">床位费 ¥3,000/月 + 押金 ¥2,000</text></view>
                    <view class="kv"><text class="k">支付状态</text><text class="v red">待支付</text></view>
                </view>
                <view class="pay-title">缴费方式</view>
                <radio-group class="radio-col" @change="e => payMethod = e.detail.value">
                    <label class="radio-item pay-item"><radio value="wechat" :checked="payMethod === 'wechat'" color="#0f766e" />微信支付（线下扫码）</label>
                    <label class="radio-item pay-item"><radio value="alipay" :checked="payMethod === 'alipay'" color="#0f766e" />支付宝（线下扫码）</label>
                    <label class="radio-item pay-item"><radio value="offline" :checked="payMethod === 'offline'" color="#0f766e" />现金 / 前台 POS</label>
                </radio-group>
                <view class="tip">线下支付：家属通过微信、支付宝转账及现金等方式缴费，需进行支付确认</view>
                <button class="btn-primary" @click="advance">我已完成支付，确认缴费</button>
            </view>

            <!-- 第 7 步：完成入住 -->
            <view class="card" v-else>
                <view class="done-box">
                    <view class="done-icon">🎉</view>
                    <view class="done-text">入住办理完成</view>
                    <view class="wait-sub">{{ elder.name }} 已正式入住，跌倒监测手环已激活，告警将实时推送给您</view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import request from '../../utils/request.js'

export default {
    data() {
        return {
            steps: ['申请入住', '入住评估', '入住审核', '入住配置', '签约办理', '首期缴费', '完成入住'],
            elder: null,
            stayViewId: null,
            assessText: '',
            payMethod: 'wechat',
            billNo: '',
            applyForm: { name: '', gender: 1, age: '', guardianName: '', guardianPhone: '', healthStatus: '' },
            pollTimer: null
        }
    },
    onShow() {
        if (!uni.getStorageSync('token')) { uni.redirectTo({ url: '/pages/login/login' }); return }
        this.stayViewId = null
        this.load()
    },
    onHide() {
        this.stopPoll()
    },
    onUnload() {
        this.stopPoll()
    },
    watch: {
        'elder.checkStatus'(val) {
            if (val === 3) {
                this.startPoll()
            } else {
                this.stopPoll()
            }
        }
    },
    methods: {
        startPoll() {
            this.stopPoll()
            this.pollTimer = setInterval(async () => {
                await this.load()
                if (this.elder && this.elder.checkStatus > 3) {
                    this.stopPoll()
                    uni.showToast({ title: '审核已通过', icon: 'success' })
                }
            }, 5000)
        },
        stopPoll() {
            if (this.pollTimer) {
                clearInterval(this.pollTimer)
                this.pollTimer = null
            }
        },
        async load() {
            try {
                const res = await request({ url: '/residents?pageNum=1&pageSize=50' })
                const records = res.data.records || []
                // 优先展示进行中的申请（含退住申请中），完成后停留在完成页，否则展示退住卡片 + 新申请表单
                const inProgress = records.find(r =>
                    (r.residentStatus === 1 && r.checkStatus < 7) || r.residentStatus === 2)
                if (inProgress) {
                    this.elder = inProgress
                } else if (this.stayViewId) {
                    this.elder = records.find(r => r.id === this.stayViewId) || null
                } else {
                    this.elder = null
                }
                if (this.elder && this.elder.checkStatus === 6) {
                    this.billNo = 'ZD' + Date.now()
                }
                if (this.elder && this.elder.checkStatus === 2 && !this.assessText) {
                    this.assessText = this.elder.healthStatus || ''
                }
            } catch (e) {}
        },
        async submitApply() {
            const f = this.applyForm
            if (!f.name.trim()) { uni.showToast({ title: '请填写老人姓名', icon: 'none' }); return }
            const age = Number(f.age)
            if (!f.age || isNaN(age) || age < 0 || age > 150) { uni.showToast({ title: '请输入正确年龄（0-150）', icon: 'none' }); return }
            try {
                await request({ url: '/residents/apply', method: 'POST', data: { ...f, age } })
                uni.showToast({ title: '申请已提交', icon: 'success' })
                this.applyForm = { name: '', gender: 1, age: '', guardianName: '', guardianPhone: '', healthStatus: '' }
                this.load()
            } catch (e) {}
        },
        async advance() {
            if (this.elder.checkStatus === 2 && !this.assessText.trim()) {
                uni.showToast({ title: '请先完成健康评估', icon: 'none' }); return
            }
            if (this.elder.checkStatus === 6 && this.payMethod !== 'offline' && !this.billNo) {
                this.billNo = 'ZD' + Date.now()
            }
            try {
                await request({
                    url: `/residents/${this.elder.id}/step`, method: 'PUT',
                    data: { checkStatus: this.elder.checkStatus + 1 }
                })
                uni.showToast({ title: '已保存', icon: 'success' })
                // 走完第 7 步后停留在完成页，展示退住办理入口
                if (this.elder.checkStatus === 6) {
                    this.stayViewId = this.elder.id
                }
                this.load()
            } catch (e) {}
        }
    }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f8; padding: 24rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 24rpx; }
.card-title { font-size: 32rpx; font-weight: bold; color: #1f2937; margin-bottom: 20rpx; }
.form-tip { font-size: 24rpx; color: #6b7280; margin-bottom: 20rpx; }
.field { margin-bottom: 20rpx; }
.label { font-size: 26rpx; color: #374151; display: block; margin-bottom: 10rpx; }
.ipt { background: #f3f4f6; border-radius: 12rpx; padding: 18rpx 20rpx; font-size: 28rpx; }
.ta { background: #f3f4f6; border-radius: 12rpx; padding: 18rpx 20rpx; font-size: 28rpx; width: 100%; box-sizing: border-box; min-height: 120rpx; }
.radio-row { display: flex; gap: 40rpx; }
.radio-item { font-size: 28rpx; color: #374151; margin-right: 30rpx; }
.btn-primary { background: #0f766e; color: #fff; border-radius: 44rpx; font-size: 30rpx; margin-top: 16rpx; }
.btn-danger { background: #ef4444; color: #fff; border-radius: 44rpx; font-size: 30rpx; margin-top: 16rpx; }

.steps-card { padding-bottom: 20rpx; }
.steps-row { display: flex; align-items: flex-start; }
.step-wrap { display: flex; flex: 1; align-items: flex-start; }
.step { display: flex; flex-direction: column; align-items: center; width: 100rpx; flex-shrink: 0; }
.dot {
    width: 48rpx; height: 48rpx; border-radius: 50%;
    background: #e5e7eb; color: #9ca3af;
    text-align: center; line-height: 48rpx; font-size: 24rpx;
}
.dot.cur { background: #0f766e; color: #fff; font-weight: bold; }
.dot.done { background: #ccfbf1; color: #0f766e; }
.step-name { font-size: 20rpx; color: #6b7280; margin-top: 8rpx; text-align: center; }
.step-name.hl { color: #0f766e; font-weight: bold; }
.bar { flex: 1; height: 6rpx; background: #e5e7eb; margin-top: 22rpx; }
.bar.filled { background: #14b8a6; }
.elder-line { font-size: 26rpx; color: #374151; margin-top: 24rpx; }

.info-block { background: #f8fafc; border-radius: 12rpx; padding: 20rpx; }
.kv { display: flex; justify-content: space-between; padding: 10rpx 0; }
.k { color: #6b7280; font-size: 26rpx; }
.v { color: #1f2937; font-size: 26rpx; }
.v.hl { color: #0f766e; }
.v.strong { font-size: 32rpx; font-weight: bold; }
.v.red { color: #ef4444; }
.tip { font-size: 24rpx; color: #9ca3af; margin: 20rpx 0 6rpx; }

.wait-box { text-align: center; padding: 40rpx 0; }
.wait-box.small { padding: 10rpx 0; }
.wait-icon { font-size: 72rpx; }
.wait-text { font-size: 30rpx; color: #1f2937; margin-top: 16rpx; font-weight: bold; }
.wait-sub { font-size: 24rpx; color: #9ca3af; margin-top: 10rpx; }

.contract { background: #f8fafc; border-radius: 12rpx; padding: 20rpx; }
.contract-title { font-weight: bold; font-size: 26rpx; margin-bottom: 12rpx; color: #0f766e; }
.contract-item { font-size: 24rpx; color: #4b5563; line-height: 1.8; }

.bill { background: #f8fafc; border-radius: 12rpx; padding: 20rpx; }
.bill-note { color: #f59e0b; font-size: 24rpx; margin-bottom: 8rpx; }
.pay-title { font-size: 28rpx; font-weight: bold; margin: 24rpx 0 10rpx; }
.radio-col { display: flex; flex-direction: column; gap: 16rpx; }
.pay-item { display: flex; align-items: center; }

.done-box { text-align: center; padding: 30rpx 0; }
.done-icon { font-size: 96rpx; }
.done-text { font-size: 36rpx; font-weight: bold; color: #0f766e; margin-top: 16rpx; }
</style>
