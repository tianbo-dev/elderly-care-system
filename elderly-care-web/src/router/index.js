import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
    { path: '/login', component: () => import('../views/Login.vue') },
    {
        path: '/',
        component: () => import('../views/Layout.vue'),
        redirect: '/dashboard',
        children: [
            { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
            { path: 'residents', component: () => import('../views/ResidentList.vue'), meta: { title: '入住办理' } },
            { path: 'checkout', component: () => import('../views/Checkout.vue'), meta: { title: '退住管理' } },
            { path: 'alarms', component: () => import('../views/AlarmList.vue'), meta: { title: '告警管理' } }
        ]
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router
