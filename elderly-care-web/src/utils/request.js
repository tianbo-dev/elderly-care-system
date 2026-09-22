import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = 'Bearer ' + token
    }
    return config
})

request.interceptors.response.use(
    res => {
        const data = res.data
        if (data.code !== 200) {
            ElMessage.error(data.message || '请求失败')
            if (data.code === 401) {
                localStorage.removeItem('token')
                window.location.href = '#/login'
            }
            return Promise.reject(data)
        }
        return data
    },
    err => {
        ElMessage.error(err.message || '网络错误')
        return Promise.reject(err)
    }
)

export default request
