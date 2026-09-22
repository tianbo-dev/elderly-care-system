<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">康</div>
        <h2>乐康养老</h2>
        <p>后台管理系统</p>
      </div>
      <el-form :model="form" label-width="60px" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="handleLogin" :loading="loading">登 录</el-button>
        <div class="tip">默认账号: admin / 123456</div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const form = ref({ username: 'admin', password: '123456' })
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await request.post('/auth/login', form.value)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  background: linear-gradient(135deg, #0d9488 0%, #14b8a6 50%, #2dd4bf 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-box {
  width: 380px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 32px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.logo {
  width: 56px;
  height: 56px;
  background: #14b8a6;
  color: #fff;
  font-size: 28px;
  font-weight: bold;
  border-radius: 12px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-header h2 { margin-bottom: 4px; color: #1f2937; }
.login-header p { color: #9ca3af; font-size: 13px; }
.tip { text-align: center; color: #9ca3af; font-size: 12px; margin-top: 12px; }
</style>
