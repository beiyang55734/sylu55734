// Babel 配置文件
// Babel 是一个 JavaScript 编译器，用于将 ES6+ 语法转换为浏览器兼容的 ES5 语法
module.exports = {
  // presets（预设）：一组预先配置好的 Babel 插件集合，用于简化配置
  presets: [
    // 确保你写的现代 JavaScript 代码（如 ES6 语法、Vue 组件中的代码）能被正确转换为所有目标浏览器都能识别的代码，解决浏览器兼容性问题。
    // @vue/cli-plugin-babel/preset 是 Vue CLI 内置的 Babel 预设
    // 包含了针对 Vue 项目的最佳实践配置，支持：
    // 1. 转换 ES6+ 语法
    // 2. 处理 Vue 单文件组件中的 JavaScript
    // 3. 根据目标浏览器自动调整转换规则
    '@vue/cli-plugin-babel/preset'
  ]
}
