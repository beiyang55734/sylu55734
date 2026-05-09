package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Grade;
import com.example.springboot.entity.User;
import com.example.springboot.service.IGradeService;
import com.example.springboot.service.ICourseService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
* API接口
*/
@RestController
@RequestMapping("/grade")
public class GradeController {

    @Autowired
    private IGradeService gradeService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IUserService userService;

    /**
     * 新增
     */
    @PostMapping
    public Result save(@RequestBody Grade grade) {
        User user = TokenUtils.getCurrentUser();
        // 只有教师和管理员可以新增成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            return Result.error("无权限执行此操作");
        }
        
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Grade::getCourseid, grade.getCourseid());
        queryWrapper.eq(Grade::getStudentid, grade.getStudentid());
        Grade existingGrade = gradeService.getOne(queryWrapper);
        if (existingGrade != null) {
            return Result.error("该学生已经打过分，请勿重复打分");
        }
        gradeService.save(grade);
        return Result.success();
    }



    /**
     * 修改
     */
    @PutMapping
    public Result update(@RequestBody Grade grade) {
        User user = TokenUtils.getCurrentUser();
        // 只有教师和管理员可以修改成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            return Result.error("无权限执行此操作");
        }
        gradeService.updateById(grade);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        User user = TokenUtils.getCurrentUser();
        // 只有教师和管理员可以删除成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            return Result.error("无权限执行此操作");
        }
        gradeService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        User user = TokenUtils.getCurrentUser();
        // 只有教师和管理员可以批量删除成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            return Result.error("无权限执行此操作");
        }
        gradeService.removeByIds(ids);
        return Result.success();
    }

    /**
     * 查询全部数据
     */
    @GetMapping
    public Result findAll() {
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<Grade>();
        User user = TokenUtils.getCurrentUser();
        // 学生只能查看自己的成绩，教师和管理员可以查看所有成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            queryWrapper.eq(Grade::getStudentid, user.getId());
        }
        return Result.success(gradeService.list(queryWrapper));
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        Grade grade = gradeService.getById(id);
        User user = TokenUtils.getCurrentUser();
        // 学生只能查看自己的成绩，教师和管理员可以查看所有成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER") && !grade.getStudentid().equals(user.getId())){
            return Result.error("无权限查看该成绩");
        }
        return Result.success(grade);
    }

    /**
     * 分页查询数据
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<Grade>().orderByDesc(Grade::getId);
        queryWrapper.like(StrUtil.isNotBlank(name), Grade::getName, name);
        User user = TokenUtils.getCurrentUser();
        // 学生只能查看自己的成绩，教师和管理员可以查看所有成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            queryWrapper.eq(Grade::getStudentid, user.getId());
        }

        Page<Grade> page = gradeService.page(new Page<>(pageNum, pageSize), queryWrapper);

        return Result.success(page);
    }

    /**
     * 导入成绩
     */
    @PostMapping("/import")
    public Result importGrades(@RequestParam("file") MultipartFile file) {
        User user = TokenUtils.getCurrentUser();
        // 只有教师和管理员可以导入成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            return Result.error("无权限执行此操作");
        }
        
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            List<Grade> grades = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Grade grade = new Grade();
                    grade.setName(getCellValue(row.getCell(0)));
                    grade.setStudentname(getCellValue(row.getCell(1)));
                    grade.setStudentid(Integer.parseInt(getCellValue(row.getCell(2))));
                    grade.setCourseid(Integer.parseInt(getCellValue(row.getCell(3))));
                    grade.setScore(Double.parseDouble(getCellValue(row.getCell(4))));
                    grade.setComment(getCellValue(row.getCell(5)));
                    grade.setFeedback(getCellValue(row.getCell(6)));
                    grades.add(grade);
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行数据格式错误: " + e.getMessage());
                }
            }
            
            workbook.close();
            
            if (!grades.isEmpty()) {
                List<Grade> savedGrades = new ArrayList<>();
                for (Grade grade : grades) {
                    LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(Grade::getCourseid, grade.getCourseid());
                    queryWrapper.eq(Grade::getStudentid, grade.getStudentid());
                    Grade existingGrade = gradeService.getOne(queryWrapper);
                    if (existingGrade == null) {
                        savedGrades.add(grade);
                    } else {
                        errors.add("学生ID: " + grade.getStudentid() + " 课程ID: " + grade.getCourseid() + " 已存在成绩");
                    }
                }
                
                if (!savedGrades.isEmpty()) {
                    gradeService.saveBatch(savedGrades);
                }
            }
            
            if (!errors.isEmpty()) {
                return Result.error("部分数据导入失败: " + String.join("; ", errors));
            }
            
            return Result.success("导入成功，共导入" + grades.size() + "条数据");
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出成绩
     */
    @GetMapping("/export")
    public void exportGrades(HttpServletResponse response) {
        User user = TokenUtils.getCurrentUser();
        // 只有教师和管理员可以导出成绩
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            try {
                response.setStatus(403);
                response.getWriter().write("无权限执行此操作");
                response.getWriter().flush();
                response.getWriter().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        try {
            List<Grade> grades = gradeService.list();
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("成绩表");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"课程名称", "学生姓名", "学生ID", "课程ID", "分数", "评语", "学生反馈"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
                cell.setCellStyle(style);
            }
            
            // 填充数据
            for (int i = 0; i < grades.size(); i++) {
                Grade grade = grades.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(grade.getName());
                row.createCell(1).setCellValue(grade.getStudentname());
                row.createCell(2).setCellValue(grade.getStudentid());
                row.createCell(3).setCellValue(grade.getCourseid());
                row.createCell(4).setCellValue(grade.getScore());
                row.createCell(5).setCellValue(grade.getComment());
                row.createCell(6).setCellValue(grade.getFeedback());
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("成绩表.xlsx", "UTF-8"));
            
            // 输出文件
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 成绩统计分析
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam(required = false) Integer courseId) {
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<Grade>();
        User user = TokenUtils.getCurrentUser();
        // 学生只能查看自己的成绩统计，教师和管理员可以查看所有成绩统计
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            queryWrapper.eq(Grade::getStudentid, user.getId());
        }
        
        if (courseId != null) {
            queryWrapper.eq(Grade::getCourseid, courseId);
        }

        List<Grade> grades = gradeService.list(queryWrapper);
        
        if (grades.isEmpty()) {
            return Result.success(Collections.emptyMap());
        }
        
        // 计算平均分
        double averageScore = grades.stream().mapToDouble(Grade::getScore).average().orElse(0);
        
        // 计算及格率
        long passCount = grades.stream().filter(grade -> grade.getScore() >= 60).count();
        double passRate = (double) passCount / grades.size() * 100;
        
        // 按课程统计
        Map<String, Map<String, Object>> courseStatistics = grades.stream()
            .collect(Collectors.groupingBy(Grade::getName, Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("count", list.size());
                    stats.put("average", list.stream().mapToDouble(Grade::getScore).average().orElse(0));
                    stats.put("min", list.stream().mapToDouble(Grade::getScore).min().orElse(0));
                    stats.put("max", list.stream().mapToDouble(Grade::getScore).max().orElse(0));
                    stats.put("passRate", (double) list.stream().filter(g -> g.getScore() >= 60).count() / list.size() * 100);
                    return stats;
                }
            )));
        
        // 分数分布
        Map<String, Integer> scoreDistribution = new HashMap<>();
        scoreDistribution.put("优秀(90-100)", (int) grades.stream().filter(g -> g.getScore() >= 90).count());
        scoreDistribution.put("良好(80-89)", (int) grades.stream().filter(g -> g.getScore() >= 80 && g.getScore() < 90).count());
        scoreDistribution.put("中等(70-79)", (int) grades.stream().filter(g -> g.getScore() >= 70 && g.getScore() < 80).count());
        scoreDistribution.put("及格(60-69)", (int) grades.stream().filter(g -> g.getScore() >= 60 && g.getScore() < 70).count());
        scoreDistribution.put("不及格(0-59)", (int) grades.stream().filter(g -> g.getScore() < 60).count());
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", grades.size());
        result.put("averageScore", averageScore);
        result.put("passRate", passRate);
        result.put("courseStatistics", courseStatistics);
        result.put("scoreDistribution", scoreDistribution);
        
        return Result.success(result);
    }

    /**
     * 学生成绩排名
     */
    @GetMapping("/ranking")
    public Result getRanking(@RequestParam(required = false) Integer courseId) {
        LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<Grade>();
        User user = TokenUtils.getCurrentUser();
        // 学生只能查看自己的成绩排名，教师和管理员可以查看所有成绩排名
        if(!user.getRole().equals("ADMIN") && !user.getRole().equals("TEACHER")){
            queryWrapper.eq(Grade::getStudentid, user.getId());
        }
        
        if (courseId != null) {
            queryWrapper.eq(Grade::getCourseid, courseId);
        }
        queryWrapper.orderByDesc(Grade::getScore);
        
        List<Grade> grades = gradeService.list(queryWrapper);
        
        // 添加排名
        for (int i = 0; i < grades.size(); i++) {
            Grade grade = grades.get(i);
            Map<String, Object> rankingInfo = new HashMap<>();
            rankingInfo.put("rank", i + 1);
            rankingInfo.put("grade", grade);
            grades.set(i, grade);
        }
        
        return Result.success(grades);
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

}
