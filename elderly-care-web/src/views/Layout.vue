<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <div class="logo">康</div>
        <span>乐康养老</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#0f766e"
        text-color="#ccfbf1"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-sub-menu index="checkin-out">
          <template #title>
            <el-icon><Suitcase /></el-icon>
            <span>入退管理</span>
          </template>
          <el-menu-item index="/residents">
            <el-icon><User /></el-icon>
            <span>入住办理</span>
          </el-menu-item>
          <el-menu-item index="/checkout">
            <el-icon><SwitchButton /></el-icon>
            <span>退住管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/alarms">
          <el-icon><Bell /></el-icon>
          <span>告警管理</span>
          <el-badge v-if="unhandledCount > 0" :value="unhandledCount" class="badge" />
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">{{ currentTitle }}</div>
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" style="background:#14b8a6">A</el-avatar>
              {{ userInfo?.username || 'Admin' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')
const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || 'null') } catch { return null }
})

const unhandledCount = ref(0)
let timer = null

const fetchUnhandled = async () => {
  try {
    const res = await request.get('/alarms/unhandled-count')
    unhandledCount.value = res.data
  } catch {}
}

onMounted(() => {
  fetchUnhandled()
  timer = setInterval(fetchUnhandled, 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}
</script>

<style scoped>
.aside { background: #0f766e; }
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.brand .logo {
  width: 36px;
  height: 36px;
  background: #14b8a6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title { font-size: 16px; font-weight: 500; }
.el-dropdown-link { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.main { background: #f3f4f6; }
.badge { margin-left: auto; }
</style>
