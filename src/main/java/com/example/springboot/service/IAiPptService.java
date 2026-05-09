package com.example.springboot.service;

import java.util.Map;

public interface IAiPptService {

    /**
     * 创建API Token
     */
    Map<String, Object> createApiToken(String apiKey, String uid, Integer limit, Integer timeOfHours);

    /**
     * 直接生成PPT
     */
    Map<String, Object> directGeneratePptx(String token, String prompt);

    /**
     * 生成PPT大纲
     */
    Map<String, Object> generateOutline(String token, String prompt);

    /**
     * 生成PPT内容
     */
    Map<String, Object> generateContent(String token, String outline, Boolean asyncGenPptx);

    /**
     * 查询异步生成PPT信息
     */
    Map<String, Object> asyncPptInfo(String token, String pptId);
}
