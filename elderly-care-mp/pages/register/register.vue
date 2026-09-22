<template>
    <view class="card">
        <view class="input-item">
            <text class="label">账号</text>
            <input class="input" v-model="form.username" placeholder="请输入账号" />
        </view>
        <view class="input-item">
            <text class="label">密码</text>
            <input class="input" v-model="form.password" type="password" placeholder="请输入密码" />
        </view>
        <view class="input-item">
            <text class="label">手机号</text>
            <input class="input" v-model="form.phone" placeholder="请输入手机号" />
        </view>
        <view class="input-item">
            <text class="label">角色</text>
            <picker :range="roles" @change="onRoleChange">
                <view class="picker">{{ roleIndex === null ? '请选择角色' : roles[roleIndex] }}</view>
            </picker>
        </view>
        <view class="btn-primary" @click="handleRegister">注册</view>
    </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
    data() {
        return {
            form: { username: '', password: '', phone: '', role: 'FAMILY' },
            roles: ['FAMILY 家属', 'ELDER 老人'],
            roleIndex: 0
        }
    },
    methods: {
        onRoleChange(e) {
            this.roleIndex = e.detail.value
            this.form.role = this.roleIndex === 0 ? 'FAMILY' : 'ELDER'
        },
        async handleRegister() {
            if (!this.form.username || !this.form.password) {
                uni.showToast({ title: '请填写完整信息', icon: 'none' })
                return
            }
            const res = await request({ url: '/auth/register', method: 'POST', data: this.form })
            if (res) {
                uni.showToast({ title: '注册成功，请登录', icon: 'success' })
                setTimeout(() => uni.navigateBack(), 800)
            }
        }
    }
}
</script>

<style scoped>
.picker { flex: 1; color: #374151; font-size: 28rpx; }
</style>
