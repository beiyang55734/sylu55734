<template>
  <div class="ai-ppt-container">
    <div class="ai-ppt-header">
      <h2>AI生成PPT</h2>
      <p class="subtitle">AI一键搞定PPT，告别繁琐，仅需一分钟，帮您制作出令人赞叹的专业PPT演示文稿！</p>
    </div>

    <el-card class="ppt-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>AI创作中心</span>
          <el-button type="primary" size="small" @click="refreshIframe" icon="el-icon-refresh">刷新</el-button>
        </div>
      </template>

      <div v-if="!tokenReady" class="loading-container">
        <el-spinner size="large"></el-spinner>
        <p>正在初始化AI创作界面...</p>
        <p v-if="error" class="error-text">{{ error }}</p>
      </div>

      <div v-show="tokenReady" class="iframe-container">
        <iframe
          ref="docmeeIframe"
          :src="iframeUrl"
          frameborder="0"
          width="100%"
          height="600px"
          allow="camera; microphone"
        ></iframe>
      </div>
    </el-card>

    <div class="features">
      <el-tag size="small" effect="plain">🎨 智能排版</el-tag>
      <el-tag size="small" effect="plain">🖼️ AI文生图</el-tag>
      <el-tag size="small" effect="plain">📊 一键配图</el-tag>
      <el-tag size="small" effect="plain">✏️ 在线编辑</el-tag>
      <el-tag size="small" effect="plain">📚 海量模板</el-tag>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiPpt',
  data() {
    return {
      tokenReady: false,
      token: '',
      error: null,
      iframeUrl: ''
    }
  },
  mounted() {
    this.initToken()
  },
  methods: {
    async initToken() {
      try {
        this.error = null
        this.tokenReady = false

        const response = await this.$request.post('/ai-ppt/create-token', {
          apiKey: 'ak_64LGva5E335F5yKLEU',
          uid: 'student_' + Date.now(),
          limit: 10,
          timeOfHours: 24
        })

        if (response.code === '200' && response.data && response.data.token) {
          this.token = response.data.token
          this.iframeUrl = `https://docmee.cn/chat-builder?token=${this.token}`
          this.tokenReady = true
        } else {
          this.error = 'Token创建失败: ' + (response.msg || '未知错误')
        }
      } catch (error) {
        console.error('创建Token错误:', error)
        this.error = '创建Token失败: ' + (error.message || '网络错误')
      }
    },
    refreshIframe() {
      this.initToken()
    }
  }
}
</script>

<style scoped>
.ai-ppt-container {
  padding: 20px;
}

.ai-ppt-header {
  text-align: center;
  margin-bottom: 30px;
}

.ai-ppt-header h2 {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: #666;
  line-height: 1.5;
}

.ppt-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.iframe-container {
  border-radius: 8px;
  overflow: hidden;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #666;
}

.loading-container p {
  margin-top: 20px;
  font-size: 16px;
}

.error-text {
  color: #f56c6c;
  font-size: 14px;
}

.features {
  margin-top: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
</style>
