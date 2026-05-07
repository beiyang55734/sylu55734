package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("choose")
public class Choose {

    @TableId(type=IdType.AUTO)
    private Integer id;
    private String name;
    private Integer studentid;
    private Integer courseid;
    @TableField(exist = false)
    private String studentName;


}