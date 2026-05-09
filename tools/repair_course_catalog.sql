SET NAMES utf8mb4;

INSERT INTO `course` (`id`, `name`, `content`, `teacher`, `times`, `intervals`, `address`, `img`)
VALUES
  (1, '敦煌壁画与丝路文明', '围绕敦煌石窟、壁画艺术与丝绸之路文明交流展开学习，适合做文化讲解、数字人导览和课程展示。', '李文博', '周一 08:00-09:40', '16 学时', '人文楼 201', NULL),
  (2, '唐诗宋词中的家国情怀', '从经典诗词切入人物、时代和家国表达，适合结合 AI PPT、知识测评和朗诵展示进行拓展。', '周清妍', '周二 10:00-11:40', '16 学时', '文史楼 305', NULL),
  (3, '非遗技艺与现代设计', '结合剪纸、刺绣、皮影等非遗技艺，探索传统文化在现代设计中的转化与表达。', '陈知远', '周三 14:00-15:40', '20 学时', '创意工坊 A102', NULL),
  (4, '中国传统节日与礼俗文化', '系统学习春节、清明、端午、中秋等传统节日及背后的礼俗、信仰和文化精神。', '许安宁', '周四 10:00-11:40', '16 学时', '综合楼 406', NULL),
  (5, '青铜器与先秦文明', '通过青铜器铭文、器型和礼制理解先秦文明脉络，适合做博物馆导览式学习展示。', '顾承泽', '周五 08:30-10:10', '18 学时', '博雅楼 118', NULL),
  (6, '书法美育与汉字文化', '从汉字演变、笔法结构与审美表达入手，提升学生对传统文字文化的理解与实践能力。', '沈书涵', '周五 14:00-15:40', '18 学时', '书法教室 2', NULL),
  (7, '戏曲艺术与中国审美', '围绕京剧、昆曲等传统戏曲讲解角色、唱腔、身段和舞台审美，适合做数字人讲解场景。', '陆景行', '周六 09:00-10:40', '16 学时', '艺术中心 108', NULL),
  (8, '博物馆里的文物叙事', '学习如何围绕文物建立知识叙事、讲解脚本和展示逻辑，适合和 AI 出题、视频讲解联动。', '林若溪', '周六 14:00-15:40', '16 学时', '实践中心 301', NULL)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `content` = VALUES(`content`),
  `teacher` = VALUES(`teacher`),
  `times` = VALUES(`times`),
  `intervals` = VALUES(`intervals`),
  `address` = VALUES(`address`),
  `img` = VALUES(`img`);

UPDATE `choose` c
JOIN `course` course ON c.`courseid` = course.`id`
SET c.`name` = course.`name`;

UPDATE `grade` g
JOIN `course` course ON g.`courseid` = course.`id`
SET g.`name` = course.`name`;

UPDATE `choose`
SET `name` = '待确认课程'
WHERE `courseid` IS NULL
  AND (`name` IS NULL OR REPLACE(REPLACE(TRIM(`name`), '?', ''), '？', '') = '');

UPDATE `grade`
SET `name` = '待确认课程'
WHERE `courseid` IS NULL
  AND (`name` IS NULL OR REPLACE(REPLACE(TRIM(`name`), '?', ''), '？', '') = '');
