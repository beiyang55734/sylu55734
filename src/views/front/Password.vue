<template>
  <div class="container">
    <div class="content">
      <el-card class="password-card">
        <div class="card-header">密码修改</div>
        <el-form ref="formRef" :model="user" :rules="rules" label-width="80px" class="password-form">
          <el-form-item label="原始密码" prop="password">
            <el-input show-password v-model="user.password" placeholder="请输入原始密码" clearable></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input show-password v-model="user.newPassword" placeholder="新密码长度至少8位，包含字母和数字" clearable></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input show-password v-model="user.confirmPassword" placeholder="请再次输入新密码" clearable></el-input>
          </el-form-item>
          <div class="form-actions">
            <el-button type="warning" @click="resetForm">重置</el-button>
            <el-button type="primary" @click="update">确认修改</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: "Password",
  data() {
    // 验证确认密码与新密码一致性
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入确认密码'));
      } else if (value !== this.user.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };

    // 验证新密码强度
    const validateNewPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入新密码'));
      } else if (value.length < 8) {
        callback(new Error('密码长度不能少于8位'));
      } else if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
        callback(new Error('密码必须包含字母和数字'));
      } else if (value === this.user.password) {
        callback(new Error('新密码不能与原始密码相同'));
      } else {
        callback();
      }
    };
    return {
      user: {
        password: '',
        newPassword: '',
        confirmPassword: '',
        id: JSON.parse(localStorage.getItem("user")).id
      },
      rules: {
        password: [{ required: true, message: '请输入原始密码', trigger: 'blur' }],
        newPassword: [{ validator: validateNewPassword, required: true, trigger: 'blur' }],
        confirmPassword: [{ validator: validateConfirmPassword, required: true, trigger: 'blur' }]
      }
    };
  },
  methods: {
    // 重置表单
    resetForm() {
      this.$refs.formRef.resetFields();
    },
    // 提交密码修改
    update() {
      console.log(this.user)
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            // 准备提交的数据，只包含必要字段
            const submitData = {
              id: this.user.id,
              newPassword: this.user.newPassword,
              password: this.user.password
            };

            const res = await this.$request.post('/user/password', submitData);
            if (res.code === '200') {
              this.$notify.warning({title: '成功', message: '密码修改成功，请重新登录', showClose: false, duration: 2000});

              // 清除本地存储的用户信息，确保安全退出
              localStorage.removeItem('user');
              localStorage.removeItem('userId');
              // 延迟跳转，让用户看到成功提示
              setTimeout(() => {
                this.$router.push('/login');
              }, 1500);
            } else {
              this.$notify.error({message: res.msg || '修改失败，请稍后重试', showClose: false, duration: 2000});
            }
          } catch (error) {
            this.$notify.error({message:'网络异常，请稍后重试', showClose: false, duration: 2000});
          }
        }
      });
    }
  }
};
</script>

<style scoped>
/* 全局容器样式 - 控制整体页面布局 */
.container {
  min-height: 90vh;          /* 最小高度为视口高度的70%，确保内容区域有足够高度 */
  padding: 20px 20px;        /* 上下内边距40px，左右20px，使内容不贴边 */
  box-sizing: border-box;     /* 盒模型包含padding和border，避免宽度计算问题 */
}

/* 内容区容器 - 控制表单的居中显示 */
.content {
  display: flex;              /* 使用弹性布局 */
  justify-content: center;    /* 水平方向居中对齐 */
  align-items: flex-start;    /* 垂直方向顶部对齐 */
}

/* 密码修改卡片样式 - 表单的外层容器 */
.password-card {
  width: 30%;                /* 基础宽度100% */
  max-width: 1000px;           /* 最大宽度限制为500px，避免宽屏上表单过宽 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 轻微阴影，增强卡片立体感 */
  border-radius: 8px;         /* 8px圆角，提升视觉体验 */
  overflow: hidden;           /* 隐藏超出容器的内容，配合圆角使用 */
}

/* 卡片标题样式 - 增强视觉层次感 */
.card-header {
  font-size: 18px;            /* 标题字体大小18px */
  font-weight: 600;           /* 半粗体，突出标题 */
  color: #303133;             /* 深灰色文字，清晰可见 */
  padding: 10px 20px;         /* 内边距，控制标题区域大小 */
  border-bottom: 1px solid #f0f0f0; /* 底部边框，分隔标题与内容 */
  background-color: #fff;     /* 白色背景，与内容区一致 */
}

/* 表单样式 - 调整表单内部布局 */
.password-form {
  padding: 25px 20px;         /* 内边距，使表单内容与边框有距离 */
  background-color: #fff;     /* 白色背景，提供清晰的输入环境 */
}

/* 穿透修改Element UI表单标签样式 */
/deep/.el-form-item__label {
  font-weight: 500;           /* 标签字体半粗体，提升可读性 */
  color: #606266;             /* 中灰色，区分标签与输入内容 */
}

/* 表单操作按钮区域样式 */
.form-actions {
  display: flex;              /* 弹性布局，使按钮水平排列 */
  justify-content: flex-end;  /* 按钮靠右对齐 */
  gap: 10px;                  /* 按钮之间间距10px */
  margin-top: 20px;           /* 上方间距20px，与表单内容分隔 */
  padding-right: 10px;        /* 右侧内边距，避免按钮贴边 */
}
</style>
