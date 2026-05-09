export const CULTURE_TOPIC_LIBRARY = [
  {
    key: 'classics',
    label: '诗词经典',
    description: '聚焦诗词、国学与经典阅读',
    color: '#8a5a44',
    keywords: ['诗', '词', '诗词', '古诗', '宋词', '唐诗', '国学', '经典', '文学', '散文']
  },
  {
    key: 'heritage',
    label: '非遗技艺',
    description: '聚焦传统工艺、戏曲与民俗实践',
    color: '#2a6f6b',
    keywords: ['非遗', '技艺', '工艺', '民俗', '刺绣', '剪纸', '戏曲', '陶艺', '皮影', '泥塑']
  },
  {
    key: 'relic',
    label: '文物考古',
    description: '聚焦文物、博物馆与考古发现',
    color: '#c27b34',
    keywords: ['文物', '博物馆', '考古', '遗址', '青铜', '壁画', '石窟', '敦煌', '器物']
  },
  {
    key: 'history',
    label: '历史人物',
    description: '聚焦历史人物、时代故事与文明脉络',
    color: '#7b5ea7',
    keywords: ['历史', '人物', '朝代', '文明', '名人', '故事', '史记', '战争', '王朝']
  },
  {
    key: 'art',
    label: '艺术美育',
    description: '聚焦书画、音乐、舞蹈与审美表达',
    color: '#456990',
    keywords: ['艺术', '书法', '绘画', '音乐', '舞蹈', '美术', '美育', '审美', '设计']
  },
  {
    key: 'regional',
    label: '地方文化',
    description: '聚焦地域风貌、城市记忆与民族文化',
    color: '#5d7d2b',
    keywords: ['地方', '地域', '民族', '丝路', '岭南', '江南', '中原', '边疆', '城市']
  }
]

export const DEFAULT_CULTURE_TOPIC = {
  key: 'general',
  label: '综合文化',
  description: '适合开展跨学科学习与文化通识阅读',
  color: '#5f6c7b'
}

function normalizeText(value) {
  return String(value || '').toLowerCase()
}

function uniqueNumbers(list) {
  const numbers = (list || []).map(item => Number(item)).filter(item => !Number.isNaN(item))
  return Array.from(new Set(numbers))
}

export function inferCourseTopics(course) {
  const text = normalizeText([
    course && course.name,
    course && course.content,
    course && course.teacher,
    course && course.address
  ].filter(Boolean).join(' '))

  const matched = CULTURE_TOPIC_LIBRARY
    .map(topic => {
      const score = topic.keywords.reduce((total, keyword) => {
        return total + (text.includes(normalizeText(keyword)) ? 1 : 0)
      }, 0)
      return {
        key: topic.key,
        label: topic.label,
        description: topic.description,
        color: topic.color,
        score
      }
    })
    .filter(topic => topic.score > 0)
    .sort((a, b) => b.score - a.score)

  if (!matched.length) {
    return [DEFAULT_CULTURE_TOPIC]
  }
  return matched.slice(0, 3)
}

export function groupCoursesByTopic(courses) {
  const topicMap = new Map()
  ;(courses || []).forEach(course => {
    inferCourseTopics(course).forEach(topic => {
      if (!topicMap.has(topic.key)) {
        topicMap.set(topic.key, {
          key: topic.key,
          label: topic.label,
          description: topic.description,
          color: topic.color,
          courses: []
        })
      }
      topicMap.get(topic.key).courses.push(course)
    })
  })
  return Array.from(topicMap.values()).sort((a, b) => b.courses.length - a.courses.length)
}

export function buildFavoriteStorageKey(user) {
  const userId = user && user.id ? user.id : 'guest'
  return `culture-course-favorites:${userId}`
}

export function loadFavoriteCourseIds(user) {
  if (typeof window === 'undefined') {
    return []
  }
  try {
    return uniqueNumbers(JSON.parse(localStorage.getItem(buildFavoriteStorageKey(user)) || '[]'))
  } catch (error) {
    return []
  }
}

export function saveFavoriteCourseIds(user, courseIds) {
  if (typeof window === 'undefined') {
    return []
  }
  const nextIds = uniqueNumbers(courseIds)
  localStorage.setItem(buildFavoriteStorageKey(user), JSON.stringify(nextIds))
  return nextIds
}

export function toggleFavoriteCourse(user, courseId) {
  const nextIds = new Set(loadFavoriteCourseIds(user))
  const id = Number(courseId)
  if (nextIds.has(id)) {
    nextIds.delete(id)
  } else {
    nextIds.add(id)
  }
  return saveFavoriteCourseIds(user, Array.from(nextIds))
}

export function buildRecommendedCourses(courses, favoriteIds, enrolledIds) {
  const favoriteSet = new Set(uniqueNumbers(favoriteIds))
  const enrolledSet = new Set(uniqueNumbers(enrolledIds))
  const sourceCourses = (courses || []).filter(course => favoriteSet.has(course.id) || enrolledSet.has(course.id))
  const topicScoreMap = new Map()

  sourceCourses.forEach(course => {
    inferCourseTopics(course).forEach(topic => {
      topicScoreMap.set(topic.key, (topicScoreMap.get(topic.key) || 0) + 2)
    })
  })

  if (!topicScoreMap.size) {
    groupCoursesByTopic(courses).slice(0, 2).forEach(topic => topicScoreMap.set(topic.key, 1))
  }

  return (courses || [])
    .map(course => {
      const tags = inferCourseTopics(course)
      const tagScore = tags.reduce((total, tag) => total + (topicScoreMap.get(tag.key) || 0), 0)
      const contentScore = Math.min(String(course.content || '').length / 80, 2)
      const favoriteBoost = favoriteSet.has(course.id) ? 1.5 : 0
      const enrolledPenalty = enrolledSet.has(course.id) ? -100 : 0
      return {
        course,
        score: tagScore + contentScore + favoriteBoost + enrolledPenalty
      }
    })
    .sort((a, b) => b.score - a.score)
    .map(item => item.course)
}

export function buildLearningRoute(course) {
  const tags = inferCourseTopics(course).map(item => item.key)
  if (tags.includes('relic')) {
    return '推荐先看数字人讲解，再结合课程资料做文物导览练习。'
  }
  if (tags.includes('heritage')) {
    return '推荐先学工艺背景，再配合 AI 出题完成非遗知识巩固。'
  }
  if (tags.includes('classics')) {
    return '推荐先读课程导读，再用 AI PPT 梳理诗词脉络与人物关系。'
  }
  if (tags.includes('art')) {
    return '推荐先做审美观察，再把课程内容整理成讲解稿或演示视频。'
  }
  return '推荐先完成课程导读，再结合数字人讲解和测评形成学习闭环。'
}

export function summarizeCourse(course, maxLength) {
  const value = String((course && course.content) || '').replace(/\s+/g, ' ').trim()
  if (!value) {
    return '暂无课程简介，建议补充课程背景、核心内容和学习目标。'
  }
  if (value.length <= maxLength) {
    return value
  }
  return `${value.slice(0, maxLength)}...`
}
