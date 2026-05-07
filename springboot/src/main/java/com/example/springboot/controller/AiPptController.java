package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.IAiPptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/ai-ppt")
public class AiPptController {

    @Autowired
    private IAiPptService aiPptService;

    /**
     * 创建API Token
     */
    @PostMapping("/create-token")
    public Result createToken(@RequestBody Map<String, Object> params) {
        String apiKey = (String) params.get("apiKey");
        String uid = (String) params.get("uid");
        Integer limit = (Integer) params.get("limit");
        Integer timeOfHours = (Integer) params.get("timeOfHours");
        
        Map<String, Object> result = aiPptService.createApiToken(apiKey, uid, limit, timeOfHours);
        return Result.success(result);
    }

    /**
     * 直接生成PPT
     */
    @PostMapping("/direct-generate")
    public Result directGeneratePptx(@RequestBody Map<String, Object> params) {
        String token = (String) params.get("token");
        String prompt = (String) params.get("prompt");
        
        Map<String, Object> result = aiPptService.directGeneratePptx(token, prompt);
        return Result.success(result);
    }

    /**
     * 生成PPT大纲
     */
    @PostMapping("/generate-outline")
    public Result generateOutline(@RequestBody Map<String, Object> params) {
        String token = (String) params.get("token");
        String prompt = (String) params.get("prompt");
        
        Map<String, Object> result = aiPptService.generateOutline(token, prompt);
        return Result.success(result);
    }

    /**
     * 生成PPT内容
     */
    @PostMapping("/generate-content")
    public Result generateContent(@RequestBody Map<String, Object> params) {
        String token = (String) params.get("token");
        String outline = (String) params.get("outline");
        Boolean asyncGenPptx = (Boolean) params.get("asyncGenPptx");
        
        Map<String, Object> result = aiPptService.generateContent(token, outline, asyncGenPptx);
        return Result.success(result);
    }

    /**
     * 查询异步生成PPT信息
     */
    @GetMapping("/async-ppt-info")
    public Result asyncPptInfo(@RequestParam String token, @RequestParam String pptId) {
        Map<String, Object> result = aiPptService.asyncPptInfo(token, pptId);
        return Result.success(result);
    }
}
