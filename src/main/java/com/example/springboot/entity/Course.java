package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("course")
public class Course {

    @TableId(type=IdType.AUTO)
    private Integer id;
    private String name;
    private String content;
    private String teacher;
    private String times;
    private String intervals;
    private String address;
    private String img;

}