<template>
  <div class="ppt-video-container">
    <iframe
      src="http://localhost:8889"
      frameborder="0"
      width="100%"
      height="100%"
    ></iframe>
  </div>
</template>

<script>
export default {
  name: 'PptVideo'
}
</script>

<style scoped>
.ppt-video-container {
  width: 100%;
  height: calc(100vh - 120px);
  background: #f0f0f0;
}

.ppt-video-container iframe {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
