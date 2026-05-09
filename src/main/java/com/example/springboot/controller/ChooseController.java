package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Choose;
import com.example.springboot.entity.Course;
import com.example.springboot.entity.User;
import com.example.springboot.service.IChooseService;
import com.example.springboot.service.ICourseService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/choose")
public class ChooseController {

    @Autowired
    private IChooseService chooseService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IUserService userService;

    @PostMapping
    public Result save(@RequestBody Choose choose) {
        User currentUser = TokenUtils.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            choose.setStudentid(currentUser.getId());
        }
        if (choose.getCourseid() == null) {
            return Result.error("课程不能为空");
        }

        Course course = courseService.getById(choose.getCourseid());
        if (course == null) {
            return Result.error("课程不存在");
        }

        choose.setName(course.getName());

        LambdaQueryWrapper<Choose> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Choose::getCourseid, choose.getCourseid());
        queryWrapper.eq(Choose::getStudentid, choose.getStudentid());
        if (chooseService.getOne(queryWrapper) != null) {
            return Result.error("你已经选过这门课程了");
        }

        chooseService.save(choose);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Choose choose) {
        if (choose.getCourseid() != null) {
            Course course = courseService.getById(choose.getCourseid());
            if (course != null) {
                choose.setName(course.getName());
            }
        }
        chooseService.updateById(choose);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        chooseService.removeById(id);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(chooseService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(chooseService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        LambdaQueryWrapper<Choose> queryWrapper = new LambdaQueryWrapper<Choose>().orderByDesc(Choose::getId);
        queryWrapper.like(StrUtil.isNotBlank(name), Choose::getName, name);

        User currentUser = TokenUtils.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            queryWrapper.eq(Choose::getStudentid, currentUser.getId());
        }

        Page<Choose> page = chooseService.page(new Page<>(pageNum, pageSize), queryWrapper);
        page.getRecords().forEach(choose -> {
            User user = userService.getById(choose.getStudentid());
            choose.setStudentName(user != null ? user.getName() : "未知");
        });

        return Result.success(page);
    }
}
