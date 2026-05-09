package com.example.springboot.service.impl;

import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IAiPptService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiPptServiceImpl implements IAiPptService {

    private static final String API_BASE_URL = "https://open.docmee.cn/api";
    private static final String PYTHON_BIN = "d:/dh_live_env/Scripts/python.exe";

    @Override
    public Map<String, Object> createApiToken(String apiKey, String uid, Integer limit, Integer timeOfHours) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ServiceException("API Key不能为空");
        }

        StringBuilder script = new StringBuilder();
        script.append("import requests\n");
        script.append("url = '").append(API_BASE_URL).append("/user/createApiToken'\n");
        script.append("headers = {'Api-Key': '").append(apiKey).append("', 'Content-Type': 'application/json'}\n");
        script.append("data = {");
        if (uid != null && !uid.isEmpty()) {
            script.append("'uid': '").append(uid).append("', ");
        }
        if (limit != null) {
            script.append("'limit': ").append(limit).append(", ");
        }
        if (timeOfHours != null) {
            script.append("'timeOfHours': ").append(timeOfHours).append(", ");
        }
        script.append("}\n");
        script.append("r = requests.post(url, headers=headers, json=data)\n");
        script.append("print(r.text)\n");

        try {
            String result = executePython(script.toString());
            return parseJsonResponse(result, "创建Token");
        } catch (Exception e) {
            throw new ServiceException("创建Token失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> directGeneratePptx(String token, String title) {
        if (token == null || token.isEmpty()) {
            throw new ServiceException("Token不能为空");
        }

        StringBuilder script = new StringBuilder();
        script.append("import requests\n");
        script.append("url = '").append(API_BASE_URL).append("/ppt/directGeneratePptx'\n");
        script.append("headers = {'token': '").append(token).append("', 'Content-Type': 'application/json'}\n");
        script.append("data = {'title': '").append(title.replace("'", "\\'")).append("'}\n");
        script.append("r = requests.post(url, headers=headers, json=data)\n");
        script.append("print(r.text)\n");

        try {
            String result = executePython(script.toString());
            return parseJsonResponse(result, "生成PPT");
        } catch (Exception e) {
            throw new ServiceException("生成PPT失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> generateOutline(String token, String prompt) {
        if (token == null || token.isEmpty()) {
            throw new ServiceException("Token不能为空");
        }

        StringBuilder script = new StringBuilder();
        script.append("import requests\n");
        script.append("url = '").append(API_BASE_URL).append("/ppt/generateOutline'\n");
        script.append("headers = {'token': '").append(token).append("', 'Content-Type': 'application/json'}\n");
        script.append("data = {'prompt': '").append(prompt.replace("'", "\\'")).append("'}\n");
        script.append("r = requests.post(url, headers=headers, json=data)\n");
        script.append("print(r.text)\n");

        try {
            String result = executePython(script.toString());
            return parseJsonResponse(result, "生成大纲");
        } catch (Exception e) {
            throw new ServiceException("生成大纲失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> generateContent(String token, String outline, Boolean asyncGenPptx) {
        if (token == null || token.isEmpty()) {
            throw new ServiceException("Token不能为空");
        }

        StringBuilder script = new StringBuilder();
        script.append("import requests\n");
        script.append("url = '").append(API_BASE_URL).append("/ppt/generateContent'\n");
        script.append("headers = {'token': '").append(token).append("', 'Content-Type': 'application/json'}\n");
        script.append("data = {'outline': '").append(outline.replace("'", "\\'")).append("'");
        if (asyncGenPptx != null) {
            script.append(", 'asyncGenPptx': ").append(asyncGenPptx);
        }
        script.append("}\n");
        script.append("r = requests.post(url, headers=headers, json=data)\n");
        script.append("print(r.text)\n");

        try {
            String result = executePython(script.toString());
            return parseJsonResponse(result, "生成内容");
        } catch (Exception e) {
            throw new ServiceException("生成内容失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> asyncPptInfo(String token, String pptId) {
        if (token == null || token.isEmpty()) {
            throw new ServiceException("Token不能为空");
        }

        StringBuilder script = new StringBuilder();
        script.append("import requests\n");
        script.append("url = '").append(API_BASE_URL).append("/ppt/asyncPptInfo?pptId=").append(pptId).append("'\n");
        script.append("headers = {'token': '").append(token).append("'}\n");
        script.append("r = requests.get(url, headers=headers)\n");
        script.append("print(r.text)\n");

        try {
            String result = executePython(script.toString());
            return parseJsonResponse(result, "查询PPT信息");
        } catch (Exception e) {
            throw new ServiceException("查询PPT信息失败: " + e.getMessage());
        }
    }

    private String executePython(String script) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(PYTHON_BIN, "-c", script);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new ServiceException("Python脚本执行失败，退出码: " + exitCode);
        }

        return output.toString();
    }

    private Map<String, Object> parseJsonResponse(String jsonStr, String operation) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> response = mapper.readValue(jsonStr, Map.class);

            if (response == null) {
                throw new ServiceException(operation + "失败: 响应为空");
            }

            String code = String.valueOf(response.get("code"));
            if (!"0".equals(code)) {
                String message = (String) response.get("message");
                throw new ServiceException(operation + "失败: " + message);
            }

            Object data = response.get("data");
            if (data == null) {
                return new HashMap<>();
            }

            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }

            return new HashMap<>();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析响应失败: " + e.getMessage());
        }
    }
}