package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("grade")
public class Grade {

    @TableId(type=IdType.AUTO)
    private Integer id;
    private String name;
    private String studentname;
    private Integer studentid;
    private Integer courseid;
    private Double score;
    private String comment;
    private String feedback;



}