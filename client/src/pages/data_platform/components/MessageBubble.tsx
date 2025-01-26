import React from 'react';
import { Message } from '../types';
import { Bot, User } from 'lucide-react';

interface MessageBubbleProps {
    message: Message;
}

export function MessageBubble({ message }: MessageBubbleProps) {
    const isUser = message.role === 'user';

    return (
        <div className={`flex gap-3 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
            <div className={`flex h-8 w-8 shrink-0 select-none items-center justify-center rounded-full ${
                isUser ? 'bg-blue-600' : 'bg-gray-200'
            }`}>
                {isUser ? (
                    <User className="h-5 w-5 text-white" />
                ) : (
                    <Bot className="h-5 w-5 text-gray-600" />
                )}
            </div>
            <div
                className={`relative flex max-w-xl rounded-lg px-4 py-2 ${
                    isUser
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-200 text-gray-700'
                }`}
            >
                <p className="whitespace-pre-wrap">{message.content}</p>
            </div>
        </div>
    );
}
