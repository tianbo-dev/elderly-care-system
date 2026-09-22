<template>
    <view>
        <view v-if="!resident" class="empty-card">
            <text>暂无绑定的老人信息</text>
        </view>
        <view v-else class="detail-card">
            <view class="header">
                <view class="avatar">{{ resident.name?.charAt(0) }}</view>
                <view class="name-row">
                    <text class="name">{{ resident.name }}</text>
                    <text class="gender">{{ resident.gender === 1 ? '男' : '女' }}</text>
                    <text class="age">{{ resident.age }}岁</text>
                </view>
                <view class="room">{{ resident.roomNo }} {{ resident.bedNo }}</view>
            </view>
            <view class="section">
                <view class="section-title">基本信息</view>
                <view class="info-row"><text class="k">身份证</text><text class="v">{{ resident.idCard || '-' }}</text></view>
                <view class="info-row"><text class="k">入住日期</text><text class="v">{{ resident.checkInDate || '-' }}</text></view>
                <view class="info-row"><text class="k">健康状况</text><text class="v">{{ resident.healthStatus || '-' }}</text></view>
            </view>
            <view class="section">
                <view class="section-title">家属信息</view>
                <view class="info-row"><text class="k">监护人</text><text class="v">{{ resident.guardianName || '-' }}</text></view>
                <view class="info-row"><text class="k">联系电话</text><text class="v">{{ resident.guardianPhone || '-' }}</text></view>
                <view class="info-row"><text class="k">关系</text><text class="v">{{ resident.guardianRel || '-' }}</text></view>
            </view>
            <view class="section">
                <view class="section-title">入住进度</view>
                <view class="steps">
                    <view v-for="(s, i) in checkSteps" :key="i"
                          class="step"
                          :class="{ active: i <= resident.checkStatus }">
                        <text>{{ i + 1 }}. {{ s }}</text>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
    data() {
        return { resident: null, checkSteps: ['未开始', '申请', '评估', '审核', '配置', '签约', '缴费', '完成入住'] }
    },
    onShow() { this.loadData() },
    methods: {
        async loadData() {
            const res = await request({ url: '/residents', method: 'GET', data: { pageNum: 1, pageSize: 1 } })
            if (res && res.data.records.length) {
                this.resident = res.data.records[0]
            }
        }
    }
}
</script>

<style scoped>
.empty-card { margin: 40rpx; padding: 80rpx; text-align: center; background: #fff; border-radius: 16rpx; color: #9ca3af; }
.detail-card { margin: 20rpx; background: #fff; border-radius: 16rpx; overflow: hidden; }
.header { background: linear-gradient(135deg, #0d9488, #14b8a6); padding: 40rpx; color: #fff; }
.avatar { width: 100rpx; height: 100rpx; background: #fff; color: #0f766e; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 48rpx; font-weight: bold; margin-bottom: 16rpx; }
.name-row { display: flex; gap: 16rpx; align-items: center; }
.name { font-size: 36rpx; font-weight: bold; }
.gender, .age { font-size: 26rpx; opacity: .9; }
.room { margin-top: 8rpx; font-size: 26rpx; opacity: .9; }
.section { padding: 32rpx; border-bottom: 1rpx solid #f3f4f6; }
.section-title { font-size: 28rpx; font-weight: bold; color: #374151; margin-bottom: 16rpx; }
.info-row { display: flex; padding: 8rpx 0; }
.k { width: 160rpx; color: #9ca3af; font-size: 26rpx; }
.v { flex: 1; color: #374151; font-size: 26rpx; }
.steps { display: flex; flex-wrap: wrap; gap: 12rpx; }
.step { padding: 8rpx 16rpx; border-radius: 8rpx; background: #f3f4f6; color: #9ca3af; font-size: 24rpx; }
.step.active { background: #14b8a6; color: #fff; }
</style>
