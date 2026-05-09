<template>
  <div class="digital-human-fullscreen">
    <iframe
      src="http://localhost:8000/web/home.html"
      frameborder="0"
      width="100%"
      height="100%"
      allow="camera; microphone"
    ></iframe>
  </div>
</template>

<script>
export default {
  name: 'DigitalHuman'
}
</script>

<style scoped>
.digital-human-fullscreen {
  width: 100%;
  height: calc(100vh - 120px);
  background: #000;
}

.digital-human-fullscreen iframe {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
