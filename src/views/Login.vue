<template>
  <div class="login-container">
    <!-- 动态粒子背景 -->
    <canvas id="canvas"></canvas>
    
    <!-- 左侧区域 -->
    <div class="left-section">
      <h1 class="title">班级智能教育平台</h1>
      <p class="description">一款集成数字人对话、PPT视频制作的全功能系统。</p>
      <p class="descr">基于 Spring Boot + Vue 开发，支持多角色登录，提供直观易用的前后台管理界面！</p>
      <div class="robots-container">
        <div class="robot robot-1">
          <div class="robot-head">
            <div class="robot-eyes">
              <div class="robot-eye left-eye" :class="{ 'eye-hidden': hideLeftEye }"></div>
              <div class="robot-eye right-eye" :class="{ 'eye-hidden': hideRightEye }"></div>
            </div>
            <div class="robot-mouth"></div>
          </div>
          <div class="robot-body">
            <div class="robot-arms">
              <div class="robot-arm left-arm"></div>
              <div class="robot-arm right-arm"></div>
            </div>
            <div class="robot-legs">
              <div class="robot-leg left-leg"></div>
              <div class="robot-leg right-leg"></div>
            </div>
          </div>
        </div>
        <div class="robot robot-2">
          <div class="robot-head">
            <div class="robot-eyes">
              <div class="robot-eye left-eye" :class="{ 'eye-hidden': hideLeftEye }"></div>
              <div class="robot-eye right-eye" :class="{ 'eye-hidden': hideRightEye }"></div>
            </div>
            <div class="robot-mouth"></div>
          </div>
          <div class="robot-body">
            <div class="robot-arms">
              <div class="robot-arm left-arm"></div>
              <div class="robot-arm right-arm"></div>
            </div>
            <div class="robot-legs">
              <div class="robot-leg left-leg"></div>
              <div class="robot-leg right-leg"></div>
            </div>
          </div>
        </div>
        <div class="robot robot-3">
          <div class="robot-head">
            <div class="robot-eyes">
              <div class="robot-eye left-eye" :class="{ 'eye-hidden': hideLeftEye }"></div>
              <div class="robot-eye right-eye" :class="{ 'eye-hidden': hideRightEye }"></div>
            </div>
            <div class="robot-mouth"></div>
          </div>
          <div class="robot-body">
            <div class="robot-arms">
              <div class="robot-arm left-arm"></div>
              <div class="robot-arm right-arm"></div>
            </div>
            <div class="robot-legs">
              <div class="robot-leg left-leg"></div>
              <div class="robot-leg right-leg"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 右侧区域 -->
    <div class="right-section">
      <h1 class="welcome-title">欢迎回来</h1>
      <div class="login-type-wrapper">
        <p class="login-type">账号密码登录</p>
      </div>
      <el-form :model="user" :rules="rules" ref="loginRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="user.username" size="medium" placeholder="请输入账号" prefix-icon="el-icon-user" @focus="handleUsernameFocus" @blur="handleUsernameBlur"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="user.password" size="medium" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password @focus="handlePasswordFocus" @blur="handlePasswordBlur"></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="user.role" placeholder="请选择角色" style="width: 100%" @focus="handleRoleFocus" @blur="handleRoleBlur">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="用户" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="login">登录</el-button>
        </el-form-item>

        <div class="links">
          <div style="margin-left: 10px"><a href="/register">注册账号</a></div>
        </div>
      </el-form>
    </div>

    <el-dialog title="忘记密码" :visible.sync="forgetPassDialogVis" width="30%">
      <el-form :model="forgetUserForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="forgetUserForm.username" autocomplete="off" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="forgetUserForm.phone" autocomplete="off" placeholder="请输入手机号"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forgetPassDialogVis = false">取 消</el-button>
        <el-button type="primary" @click="resetPassword">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      forgetUserForm: {},   // 忘记密码的表单数据
      forgetPassDialogVis: false,
      user: {
        username: '',
        password: '',
        role: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'blur' },
        ],
      },
      // 机器人眼睛状态
      hideLeftEye: false,
      hideRightEye: false,
      // 所有机器人的眼睛状态
      robotEyes: {
        'robot-1': { left: false, right: false },
        'robot-2': { left: false, right: false },
        'robot-3': { left: false, right: false }
      },
      // 粒子数组
      circles: []
    }
  },
  mounted() {
    // 初始化动态粒子背景
    this.initCanvasNest();
    // 初始化鼠标跟随效果
    this.initMouseFollow();
  },
  methods: {
    // 初始化Canvas Nest
    initCanvasNest() {
      const canvas = document.getElementById('canvas');
      const ctx = canvas.getContext('2d');
      const w = canvas.width = window.innerWidth;
      const h = canvas.height = window.innerHeight;
      
      // 创建粒子
      this.circles = [];
      for (let i = 0; i < 50; i++) {
        this.circles.push({
          x: Math.random() * w,
          y: Math.random() * h,
          r: Math.random() * 5 + 1,
          _mx: Math.random() * 2 - 1,
          _my: Math.random() * 2 - 1
        });
      }
      
      // 动画循环
      const animate = () => {
        requestAnimationFrame(animate);
        ctx.clearRect(0, 0, w, h);
        
        // 绘制粒子和连线
        this.circles.forEach((circle, index) => {
          // 绘制粒子
          ctx.beginPath();
          ctx.arc(circle.x, circle.y, circle.r, 0, 360);
          ctx.closePath();
          ctx.fillStyle = 'rgba(74, 144, 226, 0.3)';
          ctx.fill();
          
          // 绘制连线
          this.circles.forEach((otherCircle, otherIndex) => {
            if (index !== otherIndex) {
              const dx = circle.x - otherCircle.x;
              const dy = circle.y - otherCircle.y;
              const d = Math.sqrt(dx * dx + dy * dy);
              if (d < 150) {
                ctx.beginPath();
                ctx.moveTo(circle.x, circle.y);
                ctx.lineTo(otherCircle.x, otherCircle.y);
                ctx.closePath();
                ctx.strokeStyle = 'rgba(74, 144, 226, ' + (0.3 - d / 500) + ')';
                ctx.stroke();
              }
            }
          });
          
          // 移动粒子
          circle._mx = (circle.x < w && circle.x > 0) ? circle._mx : (-circle._mx);
          circle._my = (circle.y < h && circle.y > 0) ? circle._my : (-circle._my);
          circle.x += circle._mx;
          circle.y += circle._my;
        });
      };
      
      animate();
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        canvas.width = w = window.innerWidth;
        canvas.height = h = window.innerHeight;
      });
    },
    // 初始化鼠标跟随效果
    initMouseFollow() {
      const robots = document.querySelectorAll('.robot');
      
      if (!robots.length) return;
      
      document.addEventListener('mousemove', (e) => {
        robots.forEach(robot => {
          const leftEye = robot.querySelector('.robot-eye.left-eye');
          const rightEye = robot.querySelector('.robot-eye.right-eye');
          
          if (!leftEye || !rightEye) return;
          
          const robotRect = robot.getBoundingClientRect();
          const robotX = robotRect.left + robotRect.width / 2;
          const robotY = robotRect.top + robotRect.height / 2;
          
          const mouseX = e.clientX;
          const mouseY = e.clientY;
          
          // 计算鼠标相对于机器人的位置
          const deltaX = mouseX - robotX;
          const deltaY = mouseY - robotY;
          
          // 计算距离，限制最大偏移量
          const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          const maxDistance = 150;
          const ratio = Math.min(distance / maxDistance, 1);
          
          // 计算眼睛偏移量
          const eyeOffsetX = (deltaX / maxDistance) * 5 * ratio;
          const eyeOffsetY = (deltaY / maxDistance) * 5 * ratio;
          
          // 计算瞳孔偏移量（比眼球偏移更大）
          const pupilOffsetX = (deltaX / maxDistance) * 8 * ratio;
          const pupilOffsetY = (deltaY / maxDistance) * 8 * ratio;
          
          // 应用偏移效果
          leftEye.style.transform = `translate(${eyeOffsetX}px, ${eyeOffsetY}px)`;
          rightEye.style.transform = `translate(${eyeOffsetX}px, ${eyeOffsetY}px)`;
          
          // 应用瞳孔偏移效果
          leftEye.style.setProperty('--pupil-offset-x', `${pupilOffsetX}px`);
          leftEye.style.setProperty('--pupil-offset-y', `${pupilOffsetY}px`);
          rightEye.style.setProperty('--pupil-offset-x', `${pupilOffsetX}px`);
          rightEye.style.setProperty('--pupil-offset-y', `${pupilOffsetY}px`);
        });
      });
    },
    handleForgetPass() {   //  初始化表单的数据
      this.forgetUserForm = {}
      this.forgetPassDialogVis = true
    },
    resetPassword() {
      this.$request.put('/password', this.forgetUserForm).then(res => {
        if (res.code === '200') {
          this.$message.success('重置成功')
          this.forgetPassDialogVis = false
        } else {
          this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/login', this.user).then(res => {
            if (res.code === '200') {
              localStorage.setItem("user", JSON.stringify(res.data))  // 存储用户数据
                this.$router.push('/')
              this.$notify.success({title: '成功', message: '登录成功', showClose: false, duration: 2000});
            } else {
              this.$notify.error({message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    handleUsernameFocus() {
      // 当用户名输入框获得焦点时，猫头鹰遮住左眼
      this.hideLeftEye = true;
      this.hideRightEye = false;
    },
    handleUsernameBlur() {
      // 当用户名输入框失去焦点时，恢复眼睛状态
      this.hideLeftEye = false;
    },
    handlePasswordFocus() {
      // 当密码输入框获得焦点时，猫头鹰遮住右眼
      this.hideLeftEye = false;
      this.hideRightEye = true;
    },
    handlePasswordBlur() {
      // 当密码输入框失去焦点时，恢复眼睛状态
      this.hideRightEye = false;
    },
    handleRoleFocus() {
      // 当角色选择框获得焦点时，猫头鹰遮住两只眼睛
      this.hideLeftEye = true;
      this.hideRightEye = true;
    },
    handleRoleBlur() {
      // 当角色选择框失去焦点时，恢复眼睛状态
      this.hideLeftEye = false;
      this.hideRightEye = false;
    },
    // 切换机器人眼睛状态
    toggleRobotEyes(robotId) {
      // 切换该机器人的眼睛状态
      this.robotEyes[robotId].left = !this.robotEyes[robotId].left;
      this.robotEyes[robotId].right = !this.robotEyes[robotId].right;
      
      // 2秒后恢复眼睛状态
      setTimeout(() => {
        this.robotEyes[robotId].left = false;
        this.robotEyes[robotId].right = false;
      }, 2000);
    }
  }
}
</script>

<style scoped>
/* 登录页面整体容器：左右分区布局 */
.login-container {
  display: flex;              /* 启用弹性布局，实现左右分栏 */
  height: 100vh;              /* 高度占满整个视口 */
  position: relative;
  overflow: hidden;
}

/* 动态粒子背景 */
#canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 左侧介绍区域 */
.left-section {
  flex: 6;                    /* 占6份宽度（左侧较宽） */
  background-color: #1f2937;  /* 深灰蓝色背景 */
  color: #fff;                /* 白色文字 */
  display: flex;              /* 弹性布局 */
  flex-direction: column;     /* 垂直排列子元素 */
  align-items: center;        /* 水平居中 */
  justify-content: center;    /* 垂直居中 */
  padding: 20px;              /* 内边距20px */
  position: relative;
  z-index: 1;
}

/* 左侧标题 */
.title {
  font-size:40px;            /* 字号24px */
  font-weight: bold;          /* 加粗 */
  margin-bottom: 20px;        /* 底部间距10px */
}

/* 左侧描述文字 */
.description {
  font-size: 20px;            /* 小字号 */
  margin-bottom: 20px;        /* 底部间距30px */
  text-align: center;         /* 文字居中 */
}

/* 左侧描述文字 */
.descr {
  font-size: 14px;            /* 小字号 */
  margin-bottom: 20px;        /* 底部外边距30px */
  text-align: center;         /* 文字水平居中 */
}

/* 左侧插图 */
.illustration {
  width: 400px;               /* 固定宽度400px */
  height: auto;               /* 高度自适应，保持比例 */
  border-radius: 0;           /* 无圆角 */
}

/* 右侧登录表单区域 */
.right-section {
  flex: 4;                    /* 占4份宽度（右侧较窄） */
  display: flex;              /* 弹性布局 */
  flex-direction: column;     /* 垂直排列 */
  align-items: center;        /* 水平居中 */
  justify-content: center;    /* 垂直居中 */
  padding: 20px;              /* 内边距20px */
  background-color: #fff;     /* 白色背景 */
  position: relative;
  z-index: 1;
}

/* 右侧欢迎标题 */
.welcome-title {
  font-size: 26px;            /* 字号26px */
  font-weight: bold;          /* 加粗 */
  margin-bottom: 20px;        /* 底部间距20px */
  color: #1e293b;             /* 深灰文字 */
}

/* “账号密码登录”文字包裹容器（用于实现左右横线） */
.login-type-wrapper {
  display: flex;              /* 弹性布局，使伪元素与文字同行 */
  align-items: center;        /* 垂直居中 */
  margin-bottom: 20px;        /* 底部间距20px */
  width: 200px;               /* 宽度与表单对齐，确保横线对称 */
}

/* 左侧横线（通过伪元素实现） */
.login-type-wrapper::before {
  content: '';                /* 必须设置 content 才能显示 */
  flex: 1;                    /* 占据剩余空间 */
  height: 1px;                /* 线条高度1px */
  background-color: #e2e8f0;  /* 浅灰色线条 */
  margin-right: 10px;         /* 与文字间距10px */
}

/* 右侧横线（通过伪元素实现） */
.login-type-wrapper::after {
  content: '';                /* 必须设置 content 才能显示 */
  flex: 1;                    /* 占据剩余空间 */
  height: 1px;                /* 线条高度1px */
  background-color: #e2e8f0;  /* 浅灰色线条 */
  margin-left: 10px;          /* 与文字间距10px */
}

/* “账号密码登录”文字样式 */
.login-type {
  font-size: 13px;            /* 小字号 */
  color: #64748b;             /* 灰蓝色文字 */
  white-space: nowrap;        /* 禁止换行，保证横线对称 */
}

/* 登录表单容器 */
.login-form {
  width: 350px;               /* 固定宽度350px，统一视觉 */
}

/* 登录按钮 */
.login-btn {
  width: 100%;                /* 宽度占满父容器 */
  height: 40px;               /* 固定高度40px */
  font-size: 14px;            /* 字号14px */
}

/* 链接容器 */
.links {
  display: flex;              /* 横向排列两个链接 */
  justify-content: right;     /* 右对齐，符合表单底部布局习惯 */
  margin: 20px 0;             /* 上下外边距20px */
  font-size: 14px;            /* 小字号，弱化处理 */
  color: #409eff;             /* 蓝色链接，符合 Element UI 主题 */
}

/* 链接样式 */
.links a {
  text-decoration: none;      /* 去除默认下划线 */
  color: #409eff;             /* 统一链接颜色 */
}

/* 链接悬停效果 */
.links a:hover {
  text-decoration: underline; /* 悬停时显示下划线，增强交互反馈 */
}

/* 机器人容器 */
.robots-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 40px;
  flex-wrap: wrap;
}

/* 机器人整体 */
.robot {
  position: relative;
  width: 150px;
  height: 200px;
  animation: robot-bounce 3s infinite;
}

/* 第一个机器人（主机器人） */
.robot-1 {
  width: 200px;
  height: 250px;
}

/* 第二个机器人 */
.robot-2 {
  animation-delay: 0.5s;
}

.robot-2 .robot-head,
.robot-2 .robot-body,
.robot-2 .robot-arm,
.robot-2 .robot-leg {
  background-color: #4CAF50;
}

.robot-2 .robot-body::before {
  background-color: #388E3C;
}

/* 第三个机器人 */
.robot-3 {
  animation-delay: 1s;
}

.robot-3 .robot-head,
.robot-3 .robot-body,
.robot-3 .robot-arm,
.robot-3 .robot-leg {
  background-color: #FF9800;
}

.robot-3 .robot-body::before {
  background-color: #F57C00;
}

/* 机器人头部 */
.robot-head {
  position: relative;
  width: 90px;
  height: 75px;
  background-color: #4A90E2;
  border-radius: 15px;
  margin: 0 auto 8px;
  z-index: 2;
  animation: robot-head-turn 8s infinite alternate;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 第一个机器人头部 */
.robot-1 .robot-head {
  width: 120px;
  height: 100px;
  border-radius: 20px;
  margin-bottom: 10px;
}

/* 机器人眼睛容器 */
.robot-eyes {
  display: flex;
  justify-content: space-around;
  padding: 22px 12px 0;
}

/* 机器人眼睛 */
.robot-eye {
  width: 22px;
  height: 22px;
  background-color: white;
  border-radius: 50%;
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.2);
  transform-origin: center center;
}

/* 机器人眼睛瞳孔 */
.robot-eye::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) translate(var(--pupil-offset-x, 0), var(--pupil-offset-y, 0));
  width: 11px;
  height: 11px;
  background-color: #333;
  border-radius: 50%;
  animation: robot-pupil-move 3s infinite;
  transition: transform 0.3s ease;
}

/* 第一个机器人眼睛容器 */
.robot-1 .robot-eyes {
  padding: 30px 15px 0;
}

/* 第一个机器人眼睛 */
.robot-1 .robot-eye {
  width: 30px;
  height: 30px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.2);
}

/* 第一个机器人眼睛瞳孔 */
.robot-1 .robot-eye::before {
  width: 15px;
  height: 15px;
}

/* 眼睛被遮挡状态 */
.robot-eye.eye-hidden {
  background-color: white;
  position: relative;
}

.robot-eye.eye-hidden::before {
  display: none;
}

.robot-eye.eye-hidden::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 5px;
  right: 5px;
  height: 3px;
  background-color: #333;
  border-radius: 3px;
  transform: translateY(-50%);
}

/* 机器人嘴巴 */
.robot-mouth {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 4px;
  background-color: #333;
  border-radius: 4px;
  animation: robot-mouth-move 4s infinite;
}

/* 第一个机器人嘴巴 */
.robot-1 .robot-mouth {
  bottom: 20px;
  width: 40px;
  height: 5px;
  border-radius: 5px;
}

/* 机器人身体 */
.robot-body {
  width: 75px;
  height: 90px;
  background-color: #4A90E2;
  border-radius: 15px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 机器人身体上的图案 */
.robot-body::before {
  content: '';
  position: absolute;
  top: 15px;
  left: 50%;
  transform: translateX(-50%);
  width: 45px;
  height: 22px;
  background-color: #357ABD;
  border-radius: 11px;
}

/* 第一个机器人身体 */
.robot-1 .robot-body {
  width: 100px;
  height: 120px;
  border-radius: 20px;
}

/* 第一个机器人身体上的图案 */
.robot-1 .robot-body::before {
  top: 20px;
  width: 60px;
  height: 30px;
  border-radius: 15px;
}

/* 机器人手臂容器 */
.robot-arms {
  position: absolute;
  top: 15px;
  left: -22px;
  right: -22px;
  display: flex;
  justify-content: space-between;
}

/* 机器人手臂 */
.robot-arm {
  width: 15px;
  height: 45px;
  background-color: #4A90E2;
  border-radius: 7px;
  animation: robot-arm-move 3s infinite alternate;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 左侧手臂 */
.robot-arm.left-arm {
  transform-origin: top center;
  animation-delay: 0s;
}

/* 右侧手臂 */
.robot-arm.right-arm {
  transform-origin: top center;
  animation-delay: 1.5s;
}

/* 机器人腿部容器 */
.robot-legs {
  position: absolute;
  bottom: -30px;
  left: 15px;
  right: 15px;
  display: flex;
  justify-content: space-between;
}

/* 机器人腿部 */
.robot-leg {
  width: 15px;
  height: 30px;
  background-color: #4A90E2;
  border-radius: 7px;
  animation: robot-leg-move 2s infinite alternate;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 左侧腿部 */
.robot-leg.left-leg {
  animation-delay: 0s;
}

/* 右侧腿部 */
.robot-leg.right-leg {
  animation-delay: 1s;
}

/* 第一个机器人手臂容器 */
.robot-1 .robot-arms {
  top: 20px;
  left: -30px;
  right: -30px;
}

/* 第一个机器人手臂 */
.robot-1 .robot-arm {
  width: 20px;
  height: 60px;
  border-radius: 10px;
}

/* 第一个机器人腿部容器 */
.robot-1 .robot-legs {
  bottom: -40px;
  left: 20px;
  right: 20px;
}

/* 第一个机器人腿部 */
.robot-1 .robot-leg {
  width: 20px;
  height: 40px;
  border-radius: 10px;
}

/* 机器人弹跳动画 */
@keyframes robot-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* 机器人头部转动动画 */
@keyframes robot-head-turn {
  0% {
    transform: rotate(-10deg);
  }
  100% {
    transform: rotate(10deg);
  }
}

/* 机器人瞳孔移动动画 */
@keyframes robot-pupil-move {
  0%, 100% {
    transform: translate(-50%, -50%) translateX(-2px);
  }
  50% {
    transform: translate(-50%, -50%) translateX(2px);
  }
}

/* 机器人嘴巴移动动画 */
@keyframes robot-mouth-move {
  0%, 90%, 100% {
    width: 40px;
  }
  95% {
    width: 50px;
  }
}

/* 机器人手臂移动动画 */
@keyframes robot-arm-move {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(30deg);
  }
}

/* 机器人腿部移动动画 */
@keyframes robot-leg-move {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-5px);
  }
}
</style>
