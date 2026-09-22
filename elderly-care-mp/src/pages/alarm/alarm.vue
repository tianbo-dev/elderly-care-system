<template>
    <view>
        <view class="filter-bar">
            <view class="filter-item" :class="{ active: status === null }" @click="status = null">全部</view>
            <view class="filter-item" :class="{ active: status === 0 }" @click="status = 0">待处理</view>
            <view class="filter-item" :class="{ active: status === 1 }" @click="status = 1">已处理</view>
        </view>

        <view v-if="loading" class="loading">加载中...</view>
        <view v-else-if="alarms.length === 0" class="empty-card">
            <text>暂无告警记录</text>
        </view>
        <view v-else class="list">
            <view v-for="item in alarms" :key="item.id" class="alarm-card">
                <view class="card-top">
                    <view class="type-tag" :class="'type-' + getTypeClass(item.alarmType)">
                        {{ getTypeLabel(item.alarmType) }}
                    </view>
                    <view class="status-tag" :class="item.status === 0 ? 'pending' : 'handled'">
                        {{ item.status === 0 ? '待处理' : '已处理' }}
                    </view>
                </view>
                <view class="card-body">
                    <view class="info-row">
                        <text class="label">老人</text>
                        <text class="value">{{ item.residentName || '-' }}</text>
                    </view>
                    <view class="info-row">
                        <text class="label">设备</text>
                        <text class="value">{{ item.deviceId || '-' }}</text>
                    </view>
                    <view class="info-row">
                        <text class="label">时间</text>
                        <text class="value">{{ formatTime(item.alarmTime) }}</text>
                    </view>
                    <view v-if="item.status === 1" class="info-row">
                        <text class="label">处理人</text>
                        <text class="value">{{ item.handleBy || '-' }}</text>
                    </view>
                    <view v-if="item.status === 1 && item.handleRemark" class="info-row">
                        <text class="label">备注</text>
                        <text class="value">{{ item.handleRemark }}</text>
                    </view>
                </view>
            </view>
        </view>
        <view v-if="loadingMore" class="loading">加载更多...</view>
        <view v-else-if="noMore && alarms.length > 0" class="no-more">没有更多了</view>
        <view v-else-if="alarms.length > 0" class="load-more" @click="loadMore">上拉加载更多</view>
    </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
    data() {
        return {
            alarms: [],
            status: null,
            pageNum: 1,
            pageSize: 20,
            loading: false,
            loadingMore: false,
            noMore: false
        }
    },
    onShow() {
        this.resetAndLoad()
    },
    methods: {
        resetAndLoad() {
            this.alarms = []
            this.pageNum = 1
            this.noMore = false
            this.loadData()
        },
        async loadData() {
            this.loading = true
            try {
                const params = { pageNum: this.pageNum, pageSize: this.pageSize }
                if (this.status !== null) params.status = this.status
                const res = await request({ url: '/alarms', method: 'GET', data: params })
                const records = res.data?.records || []
                if (this.pageNum === 1) {
                    this.alarms = records
                } else {
                    this.alarms.push(...records)
                }
                if (records.length < this.pageSize) {
                    this.noMore = true
                }
            } catch (e) {
                // toast 已在 request.js 中处理
            } finally {
                this.loading = false
                this.loadingMore = false
            }
        },
        loadMore() {
            if (this.noMore || this.loadingMore) return
            this.loadingMore = true
            this.pageNum++
            this.loadData()
        },
        getTypeLabel(type) {
            const map = {
                'FALL': '跌倒',
                'HEART': '心率异常',
                'BLOOD_PRESSURE': '血压异常',
                'TEMP': '体温异常',
                'LEAVE_BED': '离床',
                'SMOKE': '烟雾报警',
                'WATER': '水浸'
            }
            return map[type] || type || '未知'
        },
        getTypeClass(type) {
            const map = { 'FALL': 'danger', 'HEART': 'danger', 'BLOOD_PRESSURE': 'warning', 'TEMP': 'warning', 'LEAVE_BED': 'info' }
            return map[type] || 'info'
        },
        formatTime(t) {
            if (!t) return '-'
            // t 可能是 "2025-07-15T08:30:00" 格式
            return String(t).replace('T', ' ').substring(0, 19)
        }
    },
    watch: {
        status() {
            this.resetAndLoad()
        }
    }
}
</script>

<style scoped>
.filter-bar {
    display: flex;
    padding: 20rpx;
    background: #fff;
    gap: 16rpx;
    position: sticky;
    top: 0;
    z-index: 10;
}
.filter-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    border-radius: 32rpx;
    background: #f3f4f6;
    color: #6b7280;
    font-size: 28rpx;
}
.filter-item.active {
    background: #0f766e;
    color: #fff;
}
.loading, .no-more {
    text-align: center;
    padding: 40rpx;
    color: #9ca3af;
    font-size: 26rpx;
}
.empty-card {
    margin: 40rpx;
    padding: 120rpx;
    text-align: center;
    background: #fff;
    border-radius: 16rpx;
    color: #9ca3af;
    font-size: 28rpx;
}
.list {
    padding: 20rpx;
}
.alarm-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
}
.card-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
}
.type-tag {
    font-size: 26rpx;
    padding: 6rpx 16rpx;
    border-radius: 8rpx;
}
.type-danger { background: #fef2f2; color: #dc2626; }
.type-warning { background: #fffbeb; color: #d97706; }
.type-info { background: #eff6ff; color: #2563eb; }
.status-tag {
    font-size: 24rpx;
    padding: 6rpx 16rpx;
    border-radius: 8rpx;
}
.status-tag.pending { background: #fef2f2; color: #dc2626; }
.status-tag.handled { background: #d1fae5; color: #059669; }
.card-body {
    background: #f9fafb;
    border-radius: 12rpx;
    padding: 16rpx 20rpx;
}
.info-row {
    display: flex;
    padding: 6rpx 0;
    font-size: 26rpx;
}
.info-row .label {
    width: 140rpx;
    color: #9ca3af;
}
.info-row .value {
    flex: 1;
    color: #374151;
}
.load-more {
    text-align: center;
    padding: 30rpx;
    color: #0f766e;
    font-size: 26rpx;
}
</style>
