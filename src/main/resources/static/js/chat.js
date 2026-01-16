document.addEventListener("DOMContentLoaded", function() {
    const sendButton = document.getElementById("send-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");

    sendButton.addEventListener("click", function() {
        const prompt = chatInput.value;
        if (!prompt) return;
        chatInput.value = "";

        // add a user message to chat
        const userDiv = document.createElement("div");
        userDiv.className = "message user";
        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
        messagesContainer.appendChild(userDiv);

        const pathParts = window.location.pathname.split("/");
        const chatId = pathParts[pathParts.length - 1];
        const url = `/chat-stream/${chatId}?prompt=${encodeURIComponent(prompt)}`;

        const eventSource = new EventSource(url);
        let fullText = "";

        // answer AI
        const aiDiv = document.createElement("div");
        aiDiv.className = "message mentor";
        // image Ai
        aiDiv.innerHTML = `<img src="/images/mentor.png" alt="Mentor">`;
        // element answer
        const aiBubble = document.createElement("div");
        aiBubble.className = "bubble";
        aiDiv.appendChild(aiBubble);
        messagesContainer.appendChild(aiDiv);

        eventSource.onmessage = function(event) {
            const data = JSON.parse(event.data);
            let token = data.text;
            console.log(token);
            fullText += token;
            // Markdown
            aiBubble.innerHTML = marked.parse(fullText);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        };

        eventSource.onerror = function(e) {
            console.error("Error SSE:", e);
            eventSource.close();
        };
    });

    chatInput.addEventListener("keydown", function(e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendButton.click();
        }
    });

    scrollMessagesToBottom();
});

function scrollMessagesToBottom() {
    const messages = document.getElementById("messages");
    if (!messages) return;
    messages.scrollTo({
        top: messages.scrollHeight,
        behavior: "smooth",
    });
}

document.addEventListener("DOMContentLoaded", () => {
    scrollMessagesToBottom();
    requestAnimationFrame(scrollMessagesToBottom);
    setTimeout(scrollMessagesToBottom, 50);
});




