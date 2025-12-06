function toggleDropdown() {
    const box = document.getElementById("notification-dropdown");
    box.style.display = (box.style.display === "none") ? "block" : "none";
}

async function fetchNotifications() {
    try {
        const res = await fetch("notifications"); // calls NotificationServlet
        const notifications = await res.json();

        const countElem = document.getElementById("notification-count");
        const listElem = document.getElementById("notification-list");

        if (!countElem || !listElem) return;

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

        countElem.textContent = unread > 9 ? "9+" : unread;

    } catch (err) {
        console.error("Error fetching notifications:", err);
    }
}

async function markRead(id) {
    await fetch("markNotificationRead", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "notifyID=" + encodeURIComponent(id)
    });

    fetchNotifications();
}

setInterval(fetchNotifications, 5000); // auto-refresh every 5 sec
window.addEventListener("load", fetchNotifications);
