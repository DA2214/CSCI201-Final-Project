async function fetchNotifications() {
    try {
        const res = await fetch("/notifications");
        const notifications = await res.json();

        const countElem = document.getElementById("notification-count");
        const listElem = document.getElementById("notification-list");

        listElem.innerHTML = "";

        let unread = 0;

        notifications.forEach(n => {
            if (n.readStatus === 0) unread++;

            const item = document.createElement("div");
            item.className = "notification-item";

            item.innerHTML = `
                <p>${n.message}</p>
                <small>${n.createdAt}</small>
                <button class="notification-mark-btn" onclick="markRead(${n.notifyID})">
                    Mark as read
                </button>
            `;

            listElem.appendChild(item);
        });

        // update bell counter
        countElem.textContent = unread > 9 ? "9+" : unread;

    } catch (err) {
        console.error("Notification fetch failed:", err);
    }
}

async function markRead(id) {
    await fetch("/markNotificationRead", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "notifyID=" + id
    });

    fetchNotifications(); // refresh after marking read
}

// refresh every 5 seconds
setInterval(fetchNotifications, 5000);

// initial load
window.onload = fetchNotifications;
