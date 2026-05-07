let urlPrefix = "http://localhost:8000";
let serverUrl = urlPrefix + "/chat/chat_stream"
let authUrl = urlPrefix + "/generate_temp_token"

function getRoleList(avatar_mode) {
    if (avatar_mode === "public") {
        return JSON.parse(localStorage.getItem('public_roles_list')) || [];
    }
    else {
        return JSON.parse(localStorage.getItem('roles_list')) || [];
    }
}

// 存储临时token及其获取时间
let tempTokenCache = {
    model: null,
    token: null,
    timestamp: null
};

async function getTempToken(model_name, voice_id) {
    const unionid = localStorage.getItem('unionid');
    if (!unionid)
    {
        XSAlert('用户未登录');
        return;
    }
    // 检查缓存中是否有有效的token（40秒内）
    const now = Date.now();
    if (tempTokenCache.token && tempTokenCache.timestamp &&
        (now - tempTokenCache.timestamp) < 40000 && tempTokenCache.model == model_name) {
        return tempTokenCache.token;
    }
    try {
        const response = await fetch(authUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ unionid, model_name, voice_id }),
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        // 更新缓存
        tempTokenCache = {
            model: model_name,
            token: data.token,
            timestamp: now
        };

        return data.token;
    } catch (error) {
        console.warn('获取临时token失败，继续执行文字对话:', error);
        // 不显示错误提示，避免影响用户体验
        // 返回一个默认值，允许文字对话继续
        return "default_token";
    }
}

function checkUnionid() {
    const unionid = localStorage.getItem('unionid');
    if (!unionid)
    {
        alert('用户未登录');
        return false;
    }
    return true;
}

function getRoleByID(avatar_mode, selectedRoleID) {
    let rolesList = [];
    if (avatar_mode === "public") {
        rolesList = JSON.parse(localStorage.getItem('public_roles_list')) || [];
    }
    else {
        rolesList = JSON.parse(localStorage.getItem('roles_list')) || [];
    }

    const selectedRole = rolesList.find(role => role.avatar_id === selectedRoleID);
    if (!selectedRole) {
        XSAlert('角色列表中未找到角色');
        return;
    }
    return selectedRole;
}

function getVoiceIDByID(voice_id) {
    const tencentTTS = parseInt(localStorage.getItem('tencentTTS')) || 0;
    if (tencentTTS === 0 && (voice_id.slice(0, 4) === "5010" || voice_id.slice(0, 4) === "6010"))
    {
        XSAlert("角色使用了腾讯云语音，但您的服务器未配置腾讯云服务，暂时改为阿里云临时音色");
        voice_id = "longwan";
    }
    else if (tencentTTS > 0 && (voice_id.slice(0, 4) === "long" || voice_id.slice(0, 4) === "loon")) {
        XSAlert("角色使用了阿里云语音，但您的服务器设置了优先使用腾讯云服务，暂时改为腾讯云临时音色");
        voice_id = "501004";
    }

    let tts_model = "tencent";
    if (voice_id.slice(0, 4) === "5010" || voice_id.slice(0, 4) === "6010") {
        tts_model = "tencent";
    } else {
        tts_model = "ali";
    }
    return [tts_model, voice_id];
}

async function handleResponseStream(responseBody, signal) {
    const reader = responseBody.getReader();
    const decoder = new TextDecoder();
    let sseDataBuffer = "";  // SSE网络传输数据缓存区，用于存储不完整的 JSON 块
    let fullResponse = "";  // 收集完整回复
    try {
        while (true) {
            if (signal.aborted) {
                reader.cancel();
                break;
            }
            const { done, value } = await reader.read();
            if (done) {
                return;
            }
            const chunk = decoder.decode(value, { stream: true });
            sseDataBuffer += chunk; // 将新数据追加到缓存区

            // 根据换行符拆分缓存区中的数据
            const chunks = sseDataBuffer.split("\n");
            for (let i = 0; i < chunks.length - 1; i++) {
                try {
                    const data = JSON.parse(chunks[i]);
                    console.log("Received data:", data);
                    
                    // 如果有文本内容，收集完整回复
                    if (data.text && !data.endpoint) {
                        fullResponse = data.text;
                        console.log("完整回复:", fullResponse);
                        
                        // 一次性显示完整消息
                        if (window.addMessage) {
                            window.addMessage(fullResponse, false, true);
                        }
                        
                        // 只有当 cosyvoice 存在时才发送语音
                        if (window.cosyvoice) {
                            try {
                                console.log('发送文本到语音合成:', fullResponse);
                                window.cosyvoice.sendText(fullResponse);
                            } catch (voiceError) {
                                console.warn('语音合成失败，继续显示文字:', voiceError);
                            }
                        }
                    }
                    
                    // 结束信号
                    if (data.endpoint) {
                        console.log('Stream completed');
                        if (window.cosyvoice) {
                            try {
                                await window.cosyvoice.stop();
                            } catch (stopError) {
                                console.warn('停止语音服务失败:', stopError);
                            }
                        }
                        // 重置完整回复
                        fullResponse = "";
                    }
                } catch (error) {
                    console.error("Error parsing chunk:", error);
                }
            }
            // 将最后一个不完整的块保留在缓存区中
            sseDataBuffer = chunks[chunks.length - 1];
        }
    } catch (error) {
        console.error('流处理异常:', error);
    }
}

async function sendChatRequest(requestBody, signal) {
    try {
        const response = await fetch(serverUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody),
            signal: signal
        });

        if (!response.ok) {
            if (response.status === 500) {
                // 服务器内部错误，可能是API密钥未配置
                console.error('服务器内部错误，可能是API密钥未配置');
                // 显示友好的错误消息
                if (window.addMessage) {
                    window.addMessage('抱歉，数字人服务暂时不可用，请检查API密钥配置', false, true);
                }
                return;
            }
            throw new Error(`HTTP错误 ${response.status}`);
        }
        if (response.body) {
            await handleResponseStream(response.body, signal);
        } else {
            console.error('响应体为空');
        }
    } catch (error) {
        console.error('发送聊天请求失败:', error);
        // 显示友好的错误消息
        if (window.addMessage) {
            window.addMessage('抱歉，数字人服务暂时不可用，请稍后重试', false, true);
        }
    }
}
