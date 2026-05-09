import Vue from 'vue'  // 导入Vue核心库
import App from './App.vue'  // 导入根组件App（应用入口组件）
import router from './router'  // 导入路由实例（配置好的页面路由规则）
import ElementUI from 'element-ui';  // 导入Element UI组件库（UI框架）
import 'element-ui/lib/theme-chalk/index.css';  // 导入Element UI的默认样式文件
import '@/assets/css/global.css'  // 导入全局样式文件（自定义的全局CSS样式）
import request from "@/utils/request";  // 导入自定义的axios实例（封装好的请求工具）

Vue.config.productionTip = false

Vue.use(ElementUI, {size: 'small'});  // 安装Element UI插件到Vue，并全局设置组件尺寸为"small"（统一UI组件大小）

// 关闭Vue的生产环境提示（开发环境下可看到提示，生产环境隐藏）
Vue.config.productionTip = false
// 把自定义axios实例挂载到Vue原型上，全局可通过this.$request调用
Vue.prototype.$request = request
// 把后端基础地址挂载到Vue原型上，全局可通过this.$baseUrl获取
Vue.prototype.$baseUrl = 'http://localhost:9999'

// 创建Vue实例
new Vue({
    router, // 注入路由实例，使应用支持路由功能
    render: h => h(App) // 渲染根组件App（h是createElement的简写，用于创建虚拟DOM）
}).$mount('#app') // 将Vue实例挂载到页面中id为"app"的DOM元素上（对应App.vue中的根容器）
