<template>
  <!--
    根组件模板：整个Vue应用的顶层容器
    id="app"：组件的唯一标识，可用于CSS样式定位或JS DOM操作
  -->
  <div id="app">
    <!--
      路由视图组件（Router View）
      作用：根据当前路由路径，动态渲染对应的页面组件
      例如：访问"/login"时，此处会渲染Login组件；访问"/front/home"时，渲染Front布局下的Home组件
      是Vue Router实现单页应用（SPA）页面切换的核心容器
    -->
    <RouterView/>
  </div>
</template>
