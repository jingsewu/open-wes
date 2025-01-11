import React, {useState} from "react";
import "./Chatbot.css";
import {SendOutlined} from "@ant-design/icons";
import assistant from "@/icon/ai-assistant.png";
import user from "@/icon/ai-user.png";
import {Button, Input} from "antd";
import request from "@/utils/requestInterceptor";
import {toast} from "amis";

const Chatbot = () => {
    const [messages, setMessages] = useState<any[]>([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSend = async (messageText: string) => {
        if (messageText.trim() === "") return;

        const newMessage = {text: messageText, sender: "user"};
        setMessages([...messages, newMessage]);
        setInput("");
        setLoading(true);

        try {
            const response: any = await request({
                method: "get",
                url: `/ai/ai/chat?message=${encodeURIComponent(messageText)}`
            });

            if (response.data != null && response.status === 200) {
                const aiMessage = {text: response.data, sender: "ai"};
                setMessages((prevMessages) => [...prevMessages, aiMessage]);
            } else {
                toast["error"]("chat error", "Message");
            }
        } catch (error) {
            console.error("Error fetching AI response", error);
        } finally {
            setLoading(false);
        }
    };

    const handleTopicClick = (topic: string) => {
        setInput(topic);
        handleSend(topic); // Automatically send the topic as a message
    };

    const handleInputChange = (e: any) => {
        setInput(e.target.value);
    };

    return (
        <>
            <div className="chatbox">
                {/* Welcome message */}
                <div className="message-container welcome-message">
                    <img src={assistant} alt="avatar" className="avatar"/>
                    <div className="message" style={{whiteSpace: "pre-wrap"}}>
                        <div>📦 <strong>欢迎来到OWES智能助手</strong> 📦</div>
                        <p>您好！欢迎使用OWES智能问答助手，我是专门为您在开放仓库执行系统中的疑问提供解答的AI助手。</p>

                        <div>🔧 <strong>我能帮您做什么</strong> 🔧</div>
                        <ul>
                            <li className="clickable-topic"
                                onClick={() => handleTopicClick("解释OWES中的各项功能和设置")}>
                                解释OWES中的各项功能和设置
                            </li>
                            <li className="clickable-topic"
                                onClick={() => handleTopicClick("指导您如何优化仓库流程和效率")}>
                                指导您如何优化仓库流程和效率
                            </li>
                            <li className="clickable-topic"
                                onClick={() => handleTopicClick("提供故障排除建议和技术支持")}>
                                提供故障排除建议和技术支持
                            </li>
                            <li className="clickable-topic"
                                onClick={() => handleTopicClick("分享最佳实践案例和行业趋势")}>
                                分享最佳实践案例和行业趋势
                            </li>
                            <li className="clickable-topic"
                                onClick={() => handleTopicClick("回答关于库存管理、订单处理等具体问题")}>
                                回答关于库存管理、订单处理等具体问题
                            </li>
                        </ul>

                        <div>🔍 <strong>快速开始</strong> 🔍</div>
                        <p>只需输入您的问题或选择一个话题，我将立即为您提供详细的信息和解决方案。无论是遇到技术难题还是想要提高操作效率，我都准备好协助您。</p>

                        <div>🌟 <strong>提升您的OWES体验</strong> 🌟</div>
                        <p>通过我，您可以获得即时的帮助和支持，确保您的仓库运作顺畅无阻。让我们一起致力于改善您的仓库管理体验！</p>

                        <p>请问您现在有什么需要帮助的地方吗？或者您想先了解一下哪些方面？</p>
                    </div>
                </div>

                {/* Messages */}
                {messages.map((message, index) => (
                    <div key={index} className={`message-container ${message.sender}`}>
                        <img
                            src={message.sender === "user" ? user : assistant}
                            alt={`${message.sender} avatar`}
                            className="avatar"
                        />
                        <div
                            className={`message ${message.sender}`}
                            style={{whiteSpace: "pre-wrap"}}
                            dangerouslySetInnerHTML={{__html: message.text.replace(/\\n/g, "<br/>")}} // Ensure to sanitize input to prevent XSS
                        />
                    </div>
                ))}
                {loading && (
                    <div className="message-container ai">
                        <img
                            src={assistant}
                            alt="AI avatar"
                            className="avatar"
                        />
                        <div className="message ai">...</div>
                    </div>
                )}
            </div>
            <div className="input-container">
                <Input
                    value={input}
                    onChange={handleInputChange}
                    onPressEnter={(e: any) => handleSend(e.target.value)}
                    placeholder="Type your message..."
                    size="large"
                />
                <Button
                    icon={<SendOutlined/>}
                    size="large"
                    type="primary"
                    onClick={() => handleSend(input)}
                    style={{width: 80, marginLeft: 10}}
                ></Button>
            </div>
        </>
    );
};

export default Chatbot;
