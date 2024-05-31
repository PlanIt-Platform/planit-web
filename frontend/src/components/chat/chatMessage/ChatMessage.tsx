import React from 'react';

export default function ChatMessage({ message }) {
    return (
        <div>
            <div className="chat-message">
                <p>{message.name}: {message.text}</p>
            </div>
        </div>
    );
}