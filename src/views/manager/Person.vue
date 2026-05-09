<template>
  <div>
    <el-card style="width: 50%">
      <el-form :model="user" label-width="80px" style="padding-right: 20px">
        <div style="margin: 15px; text-align: center">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:9999/file/upload"
              :headers="{ token: user.token }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="user.username" placeholder="用户名" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="user.name" placeholder="姓名"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="user.phone" placeholder="电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="user.email" placeholder="邮箱"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input type="textarea" v-model="user.address" placeholder="地址"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio v-model="user.sex" label="男">男</el-radio>
          <el-radio v-model="user.sex" label="女">女</el-radio>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="user.age" placeholder="年龄"></el-input>
        </el-form-item>
        <el-form-item label="个人介绍" prop="infos">
          <el-input type="textarea" v-model="user.infos" placeholder="个人介绍"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">保 存</el-button>
          <el-button type="success" @click="formDetailVisible = true">修改密码</el-button>
        </div>
      </el-form>
    </el-card>

    <el-drawer :visible.sync="formDetailVisible" title="修改密码" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">详情</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '退出全屏' : '全屏'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <!-- 抽屉内容 -->
      <div class="drawer-content" ref="drawerContent">
        <el-form ref="formRef" :model="user" :rules="rules" label-width="80px" style="padding-right: 20px">
          <el-form-item label="用户名" prop="username">
            <el-input  v-model="user.username" placeholder="用户名" disabled></el-input>
          </el-form-item>
          <el-form-item label="原始密码" prop="password">
            <el-input show-password v-model="user.password" placeholder="原始密码"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input show-password v-model="user.newPassword" placeholder="新密码"></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input show-password v-model="user.confirmPassword" placeholder="确认密码"></el-input>
          </el-form-item>
        </el-form>
      </div>

      <!-- 抽屉底部 -->
      <div class="drawer-footer">
        <el-button type="primary" @click="updatePassword">确认修改</el-button>
        <el-button @click="formDetailVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "Person",
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
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        password: [
          { required: true, message: '请输入原始密码', trigger: 'blur' }
        ],
        newPassword: [
          { validator: validateNewPassword, required: true, trigger: 'blur' }
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, required: true, trigger: 'blur' }
        ]
      },
      formDetailVisible: false,
      isFullscreen: false,
      drawerWidth: '50%', // 默认抽屉宽度
      drawerPosition: 'right' // 默认抽屉位置
    }
  },
  created() {},
  methods: {
    updatePassword() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          // 保存当前的用户信息到数据库
          this.$request.post('/user/password', this.user).then(res => {
            if (res.code === '200') {
              // 成功更新
              this.$message.success('保存成功')
              this.$router.push('/login')
            } else {
              this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    update() {
      // 保存当前的用户信息到数据库
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          // 成功更新
          this.$message.success('保存成功')

          // 更新浏览器缓存里的用户信息
          localStorage.setItem('user', JSON.stringify(this.user))

          // 触发父级的数据更新
          this.$emit('update:user', this.user)
        } else {
          this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    handleAvatarSuccess(response, file, fileList) {
      // 把user的头像属性换成上传的图片的链接
      this.user.avatar = response.data
    },
    handleFullscreenChange() {
      // 如果用户通过ESC键退出全屏，同步更新状态
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        // 保存当前抽屉状态以便退出全屏时恢复
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;

        // 进入全屏模式
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';

        // 延迟执行全屏请求，确保DOM已更新
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        // 退出全屏模式
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;

        this.exitFullscreen();
      }
    },
    // 全屏API相关方法
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  // 监听全屏状态变化
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>
/deep/.el-form-item__label {
  font-weight: bold;
}
/deep/.el-upload {
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border-radius: 50%;
}
.avatar {
  width: 78px;
  height: 78px;
  display: block;
  border-radius: 50%;
}
</style>
