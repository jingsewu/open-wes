import React, { useState } from "react"
import axios from "axios"
import "./Chatbot.css"
import { SendOutlined } from "@ant-design/icons"
import assistant from "@/icon/ai-assistant.png"
import user from "@/icon/ai-user.png"
import { Input, Button } from "antd"

const Chatbot = () => {
    const [messages, setMessages] = useState<any[]>([])
    const [input, setInput] = useState("")
    const [loading, setLoading] = useState(false)

    const handleSend = async () => {
        if (input.trim() === "") return

        const newMessage = { text: input, sender: "user" }
        setMessages([...messages, newMessage])
        setInput("")
        setLoading(true)

        try {
            const response = await axios.get("/chatAi/ai/chat?message=" + input)
            const aiMessage = { text: response.data, sender: "ai" }
            setMessages([...messages, newMessage, aiMessage])
        } catch (error) {
            console.error("Error fetching AI response", error)
        } finally {
            setLoading(false)
        }
    }

    const handleInputChange = (e: any) => {
        setInput(e.target.value)
    }

    return (
        <>
            <div className="chatbox">
                {messages.map((message, index) => (
                    <div
                        key={index}
                        className={`message-container ${message.sender}`}
                    >
                        <img
                            src={message.sender === "user" ? user : assistant}
                            alt={`${message.sender} avatar`}
                            className="avatar"
                        />
                        <div
                            className={`message ${message.sender}`}
                            style={{ whiteSpace: "pre-wrap" }}
                        >
                            {message.text.replace(/\\n/g, "<br/>")}
                        </div>
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
                    onPressEnter={handleSend}
                    placeholder="Type your message..."
                    size="large"
                />
                <Button
                    icon={<SendOutlined />}
                    size="large"
                    type="primary"
                    onClick={handleSend}
                    style={{ width: 80, marginLeft: 10 }}
                ></Button>
            </div>
        </>
    )
}

export default Chatbot
