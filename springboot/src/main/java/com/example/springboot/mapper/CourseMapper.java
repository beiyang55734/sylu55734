package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Course;

import java.util.List;

/**
 * Course 数据访问层（DAO）接口
 * 作用：定义与 Course 实体对应的数据库操作方法，负责与数据库交互
 *
 * 继承 MyBatis-Plus 的 BaseMapper 接口，可直接获得以下核心能力（无需手动写 SQL）：
 * 1. 基础 CRUD：insert(新增)、deleteById(按主键删)、updateById(按主键改)、selectById(按主键查)、selectList(条件查列表) 等
 * 2. 条件构造：配合 QueryWrapper/LambdaQueryWrapper 实现复杂条件查询（如模糊查询、范围查询、排序等）
 * 3. 分页支持：配合 MyBatis-Plus 分页插件，实现物理分页查询
 *
 * 注意：
 * 若需扩展自定义 SQL 操作（如多表联查、复杂统计），可在此接口中定义方法，
 * 并在对应的 CourseMapper.xml 文件中编写 SQL 实现
 */
public interface CourseMapper extends BaseMapper<Course> {

}
