<script>
export default {
    globalData: {
        baseUrl: 'http://localhost:8080/api',
        token: ''
    },
    onLaunch() {
        const token = uni.getStorageSync('token')
        if (token) {
            this.globalData.token = token
        }
    }
}
</script>

<style>
@import './common/style.css';
</style>
