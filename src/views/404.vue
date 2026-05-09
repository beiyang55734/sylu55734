<template>
  <div>
    <div class="empty-container">
      <el-empty :image-size="400" :image="require('@/assets/404.svg')" :description="`404 找不到页面~`"></el-empty>
    </div>
  </div>
</template>

<script>
export default {
  name: "page-404",
  data() {
    return {}
  },
  created() {
  },
  methods: {

  }
}
</script>

<style scoped>
.empty-container {
  background-color: white;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100vh;
}
</style>
