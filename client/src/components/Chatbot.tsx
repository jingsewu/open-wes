import React, {useState} from "react";
import "./Chatbot.css";
import {SendOutlined} from "@ant-design/icons";
import assistant from "@/icon/ai-assistant.png";
import user from "@/icon/ai-user.png";
import {Button, Input} from "antd";
import request from "@/utils/requestInterceptor";
import {toast} from "amis";
import {useTranslation} from "react-i18next";

const Chatbot = () => {
    const [messages, setMessages] = useState<any[]>([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const {t} = useTranslation();

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

    const topics: any[string] = t('ai.chat.topics', {returnObjects: true});

    return (
        <>
            <div className="chatbox">
                {/* Welcome message */}
                <div className="message-container welcome-message">
                    <img src={assistant} alt="avatar" className="avatar"/>
                    <div className="message" style={{whiteSpace: 'pre-wrap'}}>
                        <div>{t('ai.chat.welcome')}</div>
                        <p>{t('ai.chat.intro')}</p>

                        <div>{t('ai.chat.whatICanDo')}</div>
                        <ul>
                            {topics.map((topic: string, index: string) => (
                                <li
                                    key={index}
                                    className="clickable-topic"
                                    onClick={() => handleTopicClick(topic)}
                                >
                                    {topic}
                                </li>
                            ))}
                        </ul>

                        <div>{t('ai.chat.quickStart')}</div>
                        <p>{t('ai.chat.quickStartContent')}</p>

                        <div>{t('ai.chat.enhanceExperience')}</div>
                        <p>{t('ai.chat.enhanceContent')}</p>

                        <p>{t('ai.chat.closing')}</p>
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
