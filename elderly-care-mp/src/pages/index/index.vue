<template>
    <view class="page">
        <!-- 顶部品牌区 -->
        <view class="header">
            <view class="brand-row">
                <view class="logo">康</view>
                <text class="brand-name">乐康养老 · 家属端</text>
            </view>
            <view class="hello">
                <view class="hello-title">您好，{{ userInfo?.username || '家属' }}</view>
                <view class="hello-sub">{{ today }}，愿家人健康平安</view>
            </view>
        </view>

        <!-- 数据概览 -->
        <view class="stat-row">
            <view class="stat-card" @click="goResident">
                <view class="stat-num">{{ residentTotal }}</view>
                <view class="stat-label">我的老人（位）</view>
            </view>
            <view class="stat-card" @click="goAlarms">
                <view class="stat-num" :class="{ 'stat-warn': unhandled > 0 }">{{ unhandled }}</view>
                <view class="stat-label">待处理告警（条）</view>
            </view>
        </view>

        <!-- 功能宫格 -->
        <view class="grid-card">
            <view class="grid-title">常用功能</view>
            <view class="grid">
                <view class="grid-item" @click="goAlarms">
                    <view class="g-icon g-orange">⚠️</view>
                    <text>告警记录</text>
                </view>
                <view class="grid-item" @click="goCheckin">
                    <view class="g-icon g-blue">📝</view>
                    <text>入住办理</text>
                </view>
                <view class="grid-item" @click="goCheckout">
                    <view class="g-icon g-green">📦</view>
                    <text>退住办理</text>
                </view>
                <view class="grid-item" @click="goLogin">
                    <view class="g-icon g-gray">🚪</view>
                    <text>退出登录</text>
                </view>
            </view>
        </view>

        <!-- 健康贴士 -->
        <view class="tips-card">
            <view class="tips-title">💡 健康小贴士</view>
            <view class="tips-text">老人若出现头晕、胸闷等不适，请及时联系护理人员；跌倒监测手环请保持佩戴并定期充电，确保告警及时送达。</view>
        </view>
    </view>
</template>

<script>
import request from '../../utils/request.js'

export default {
    data() {
        return {
            userInfo: null,
            today: '',
            residentTotal: 0,
            unhandled: 0
        }
    },
    onShow() {
        try { this.userInfo = JSON.parse(uni.getStorageSync('userInfo') || 'null') } catch (e) {}
        if (!this.userInfo) { uni.redirectTo({ url: '/pages/login/login' }); return }
        const d = new Date()
        const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
        this.today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 星期${week}`
        this.loadStats()
    },
    methods: {
        async loadStats() {
            try {
                const res = await request({ url: '/residents?pageNum=1&pageSize=1' })
                this.residentTotal = res.data.total || 0
            } catch (e) {}
            try {
                const res = await request({ url: '/alarms/unhandled-count' })
                this.unhandled = res.data || 0
            } catch (e) {}
        },
        goResident() { uni.switchTab({ url: '/pages/resident/resident' }) },
        goAlarms() { uni.navigateTo({ url: '/pages/alarm/alarm' }) },
        goCheckin() { uni.navigateTo({ url: '/pages/checkin/checkin' }) },
        goCheckout() { uni.navigateTo({ url: '/pages/checkout/checkout' }) },
        goLogin() {
            uni.showModal({
                title: '退出登录',
                content: '确定要退出登录吗？',
                success: (res) => {
                    if (res.confirm) {
                        uni.removeStorageSync('token')
                        uni.removeStorageSync('userInfo')
                        uni.reLaunch({ url: '/pages/login/login' })
                    }
                }
            })
        }
    }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f6f8; padding-bottom: 40rpx; }

.header {
    background: linear-gradient(135deg, #0b8a7a, #14b8a6);
    padding: 40rpx 40rpx 110rpx;
    color: #fff;
}
.brand-row { display: flex; align-items: center; }
.logo {
    width: 64rpx; height: 64rpx; border-radius: 16rpx;
    background: rgba(255, 255, 255, 0.2);
    text-align: center; line-height: 64rpx;
    font-size: 36rpx; font-weight: bold;
}
.brand-name { font-size: 32rpx; font-weight: bold; margin-left: 16rpx; }
.hello { margin-top: 50rpx; }
.hello-title { font-size: 44rpx; font-weight: bold; }
.hello-sub { font-size: 26rpx; color: #ccfbf1; margin-top: 10rpx; }

.stat-row {
    display: flex; margin: -80rpx 24rpx 0;
    position: relative;
}
.stat-card {
    flex: 1; background: #fff; border-radius: 16rpx;
    margin: 0 12rpx; padding: 30rpx 0;
    display: flex; flex-direction: column; align-items: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}
.stat-num { font-size: 48rpx; font-weight: bold; color: #0b8a7a; }
.stat-warn { color: #ef4444; }
.stat-label { font-size: 24rpx; color: #6b7280; margin-top: 8rpx; }

.grid-card {
    background: #fff; border-radius: 16rpx;
    margin: 24rpx; padding: 30rpx;
}
.grid-title { font-size: 30rpx; font-weight: bold; color: #1f2937; margin-bottom: 30rpx; }
.grid { display: flex; flex-wrap: wrap; }
.grid-item {
    width: 25%;
    display: flex; flex-direction: column; align-items: center;
    padding: 20rpx 0;
}
.g-icon {
    width: 96rpx; height: 96rpx; border-radius: 24rpx;
    display: flex; align-items: center; justify-content: center;
    font-size: 48rpx;
}
.g-teal { background: #e6fffa; }
.g-orange { background: #fff4e5; }
.g-green { background: #e8f8ee; }
.g-blue { background: #e8f1ff; }
.g-gray { background: #f3f4f6; }
.grid-item text { font-size: 26rpx; color: #374151; margin-top: 14rpx; }

.tips-card {
    background: #fff; border-radius: 16rpx;
    margin: 0 24rpx; padding: 30rpx;
}
.tips-title { font-size: 28rpx; font-weight: bold; color: #1f2937; }
.tips-text { font-size: 24rpx; color: #6b7280; line-height: 1.7; margin-top: 14rpx; }
</style>
