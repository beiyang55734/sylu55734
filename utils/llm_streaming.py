import asyncio
import datetime
import json
import logging
import time
from openai import OpenAI
from queue import Queue
from threading import Thread

from utils.dashscope import DASHSCOPE_API_KEY, DASHSCOPE_LLM_URL
from utils.session_manager import user_locks, get_or_create_session

_client = None
def get_client():
    global _client
    if _client is None:
        if not DASHSCOPE_API_KEY:
            raise Exception("DASHSCOPE_API_KEY 未配置，请设置阿里百炼平台秘钥")
        _client = OpenAI(
            api_key=DASHSCOPE_API_KEY,
            base_url=DASHSCOPE_LLM_URL
        )
    return _client


# 包装原始同步函数
def run_llm_thread(messages, response_queue):
    full_response = ""  # 用于收集完整回复
    print("run_llm_tts", messages)
    try:
        # 流式获取LLM响应，但完整收集后再发送
        client = get_client()
        stream = client.chat.completions.create(
            model="qwen-plus",
            messages=messages,
            max_tokens=500,  # 增加最大token数
            stream=True,
            stream_options={"include_usage": True}
        )
        for chunk in stream:
            if chunk.choices and chunk.choices[0].delta.content:
                content = chunk.choices[0].delta.content
                full_response += content  # 收集完整回复
                # 打印流式回复的时间戳
                current_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                print(f"Content chunk received at: {current_time}")
        
        # 完整收集后，一次性发送完整回复
        print(f"完整回复已收集: {full_response}")
        response_queue.put(("text", full_response))
        # 结束流式合成
        response_queue.put(("end", full_response))

    except Exception as e:
        logging.error(f"Processing error: {str(e)}")
        response_queue.put(("error", str(e)))


async def gen_stream(unionid, avatar_id, messages):
    # system_prompt = "" if system_prompt is None else system_prompt
    user_prompt = messages[-1]["content"]

    # 创建跨线程通信队列
    response_queue = Queue()
    # 在独立线程中运行同步处理
    Thread(target=run_llm_thread, args=(messages, response_queue)).start()
    
    full_response_text = ""
    while True:
        try:
            item_type, data = response_queue.get(timeout=30)

            if item_type == "text":
                full_response_text = data
                # 一次性发送完整文本，不设置endpoint=True，让前端知道这是完整消息
                yield json.dumps({
                    "text": data,
                    "endpoint": False
                }) + "\n"

            elif item_type == "end":
                # 发送结束信号
                yield json.dumps({
                    "text": "",
                    "endpoint": True,
                }) + "\n"

                # 保存对话历史
                async with user_locks[unionid]:  # 获取用户级锁
                    session = get_or_create_session(unionid, avatar_id, None)
                    session.add_messages([
                        {"role": "user", "content": user_prompt},
                        {"role": "assistant", "content": data}  # data包含完整回复
                    ])

                break

            elif item_type == "error":
                yield json.dumps({
                    "error": data,
                    "endpoint": True
                }) + "\n"
                break

        except Exception as e:
            logging.error(f"Queue timeout: {str(e)}")
            break
