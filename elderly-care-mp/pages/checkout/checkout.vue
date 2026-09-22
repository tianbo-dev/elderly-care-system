<template>
    <view class="page">
        <view class="card" v-for="r in list" :key="r.id">
            <view class="card-title">退住办理 · {{ r.name }}</view>
            <view class="info-row">
                <text class="info-item">{{ r.gender === 1 ? '男' : '女' }} · {{ r.age }} 岁</text>
                <text class="info-item">房间 {{ r.roomNo || '-' }}</text>
            </view>
            <view v-if="r.residentStatus === 1">
                <view class="tip">已完成入住，跌倒监测服务运行中。如需退住请提交申请，机构确认后完成结算</view>
                <button class="btn-danger" @click="applyCheckout(r)">申请退住</button>
            </view>
            <view v-else-if="r.residentStatus === 2" class="wait-box small">
                <view class="wait-text">退住申请审核中，请联系机构办理费用结算</view>
            </view>
            <view v-else class="wait-box small">
                <view class="wait-text">已办理退住</view>
            </view>
        </view>
        <view class="empty" v-if="!list.length">
            <view class="empty-icon">📋</view>
            <view class="empty-text">暂无可办理退住的老人</view>
        </view>
    </view>
</template>

<script>
import request from '../../utils/request.js'

export default {
    data() {
        return { list: [] }
    },
    onShow() {
        if (!uni.getStorageSync('token')) { uni.redirectTo({ url: '/pages/login/login' }); return }
        this.load()
    },
    methods: {
        async load() {
            try {
                const res = await request({ url: '/residents?pageNum=1&pageSize=50' })
                const records = res.data.records || []
                this.list = records.filter(r =>
                    r.checkStatus === 7 || r.residentStatus === 2 || r.residentStatus === 0)
            } catch (e) {}
        },
        applyCheckout(row) {
            uni.showModal({
                title: '退住申请',
                content: `确认为「${row.name}」申请退住？提交后由机构确认结算。`,
                confirmColor: '#0f766e',
                success: async (m) => {
                    if (!m.confirm) return
                    try {
                        await request({ url: `/residents/${row.id}/checkout-apply`, method: 'POST' })
                        uni.showToast({ title: '申请已提交', icon: 'success' })
                        this.load()
                    } catch (e) {}
                }
            })
        }
    }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f8; padding: 24rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 24rpx; }
.card-title { font-size: 32rpx; font-weight: bold; color: #1f2937; margin-bottom: 16rpx; }
.info-row { display: flex; gap: 24rpx; margin-bottom: 16rpx; }
.info-item { font-size: 26rpx; color: #6b7280; }
.tip { font-size: 24rpx; color: #9ca3af; margin: 12rpx 0 6rpx; }
.btn-danger { background: #ef4444; color: #fff; border-radius: 44rpx; font-size: 30rpx; margin-top: 16rpx; }
.wait-box { text-align: center; padding: 20rpx 0; }
.wait-box.small { padding: 10rpx 0; }
.wait-text { font-size: 28rpx; color: #1f2937; font-weight: bold; }
.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 96rpx; }
.empty-text { font-size: 28rpx; color: #9ca3af; margin-top: 16rpx; }
</style>
