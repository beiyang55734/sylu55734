<template>
  <div class="footer">
    <div class="copyright">
      <p>© 2025 基础脚手架 版权所有 | 京ICP备12345678号</p>
    </div>
  </div>
</template>


<style>
/* 页脚整体样式 */
.footer {
  background-color: #333;          /* 页脚背景色：深灰色 */
  color: #fff;                     /* 页脚默认文字色：白色 */
  padding: 0px 0 20px;            /* 内边距：上下40px、左右0，顶部留白多于底部 */
  margin-top: 20px;                /* 与上方内容间距：20px，避免紧贴内容 */
}

/* 链接鼠标悬停样式 */
.footer-links a:hover {
  color: #fff;                     /* 悬停时文字变白色，增强交互反馈 */
}

/* 版权信息区域样式 */
.copyright {
  text-align: center;              /* 文字居中对齐 */
  padding-top: 30px;               /* 顶部内边距：30px，与上方内容拉开距离 */
  margin-top: 30px;                /* 上方外边距：30px，配合内边距增加留白 */
  border-top: 1px solid #555;      /* 顶部边框：1px实线，颜色#555（区分区域） */
  font-size: 12px;                 /* 版权文字大小：12px（次要信息视觉层级） */
  color: #999;                     /* 版权文字色：深灰色（比链接色更暗） */
}
</style>
