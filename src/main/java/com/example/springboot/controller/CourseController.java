package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Course;
import com.example.springboot.entity.User;
import com.example.springboot.service.ICourseService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
* API接口
*/
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    /**
     * 新增
     */
    @PostMapping
    public Result save(@RequestBody Course course) {
        User currentUser = TokenUtils.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Result.error("无权限操作");
        }
        courseService.save(course);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result update(@RequestBody Course course) {
        User currentUser = TokenUtils.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Result.error("无权限操作");
        }
        courseService.updateById(course);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        User currentUser = TokenUtils.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Result.error("无权限操作");
        }
        courseService.removeById(id);
        return Result.success();
    }

    /**
     * 查询全部数据
     */
    @GetMapping
    public Result findAll() {
        return Result.success(courseService.list());
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(courseService.getById(id));
    }

    /**
     * 分页查询数据
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<Course>().orderByDesc(Course::getId);
        queryWrapper.like(StrUtil.isNotBlank(name), Course::getName, name);
        Page<Course> page = courseService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }

}
