<template>
    <view class="login-page">
        <view class="logo-wrap">
            <view class="logo">康</view>
            <text class="title">乐康养老</text>
            <text class="subtitle">家属/老人端</text>
        </view>
        <view class="card">
            <view class="input-item">
                <text class="label">账号</text>
                <input class="input" v-model="form.username" placeholder="请输入账号" />
            </view>
            <view class="input-item">
                <text class="label">密码</text>
                <input class="input" v-model="form.password" type="password" placeholder="请输入密码" />
            </view>
            <view class="btn-primary" @click="handleLogin">登录</view>
            <view class="link-row">
                <text class="link" @click="goRegister">没有账号？去注册</text>
            </view>
        </view>
    </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
    data() {
        return { form: { username: 'family01', password: '123456' } }
    },
    methods: {
        async handleLogin() {
            const res = await request({ url: '/auth/login', method: 'POST', data: this.form })
            if (res) {
                uni.setStorageSync('token', res.data.token)
                uni.setStorageSync('userInfo', JSON.stringify(res.data))
                uni.showToast({ title: '登录成功', icon: 'success' })
                setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 800)
            }
        },
        goRegister() {
            uni.navigateTo({ url: '/pages/register/register' })
        }
    }
}
</script>

<style scoped>
.login-page { min-height: 100vh; background: linear-gradient(135deg, #0d9488 0%, #14b8a6 100%); padding: 80rpx 40rpx; }
.logo-wrap { display: flex; flex-direction: column; align-items: center; margin-bottom: 80rpx; }
.logo { width: 120rpx; height: 120rpx; background: #fff; color: #0f766e; font-size: 56rpx; font-weight: bold; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; margin-bottom: 24rpx; }
.title { font-size: 44rpx; color: #fff; font-weight: bold; }
.subtitle { font-size: 26rpx; color: #ccfbf1; margin-top: 8rpx; }
.link-row { text-align: center; margin-top: 32rpx; }
.link { color: #0f766e; font-size: 26rpx; }
</style>
