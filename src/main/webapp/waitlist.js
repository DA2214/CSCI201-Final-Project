/* guest mode*/
console.log("waitlist.js loaded.");
const params = new URLSearchParams(window.location.search);
const isGuest = params.get("guest") === "1";
console.log("Guest mode:", isGuest);

/* backend variables */
let machines = [];
let myWaitlistMachineIds = [];   
const userID = localStorage.getItem("userID");

/* utility funcs*/
function computeEstimatedWait(count) {
    if (!count || count <= 0) return "–";
    const minutes = count * 30;
    if (minutes < 60) return `${minutes} min`;
    return `${Math.floor(minutes / 60)} hr ${minutes % 60} min`;
}

function isOnMyWaitlist(machineId) {
    return myWaitlistMachineIds.includes(machineId);
}

/* calls to backend*/
async function loadMachines() {
    try {
        const response = await fetch("/201Final/machines");
        machines = await response.json();
        console.log("Loaded machines:", machines);

        renderSummary();
        renderTable();
        renderMyWaitlists();
    } catch (err) {
        console.error("ERROR loading machines:", err);
    }
}

async function joinWaitlist(machineId) {
    if (isGuest) return alert("Please log in to join waitlists.");
    if (!userID) return alert("Missing userID — please log in again.");

    const body = { userID, machineID: machineId };

    const res = await fetch("/201Final/WaitlistServlet/join", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const data = await res.json();
    alert(data.message);
    loadMachines();
}

async function leaveWaitlist(machineId) {
    if (!userID) return;

    const body = { userID, machineID: machineId };

    const res = await fetch("/201Final/WaitlistServlet/decline", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const data = await res.json();
    alert(data.message);
    loadMachines();
}

/* rendering - summary chips*/
function renderSummary() {
    document.getElementById("summaryTotal").textContent = machines.length;
    document.getElementById("summaryFull").textContent =
        machines.filter(m => m.status !== "AVAILABLE").length;

    if (!isGuest) {
        document.getElementById("summaryMyWaitlists").textContent =
            myWaitlistMachineIds.length;
    }
}

/* rebdering - waitlist*/
function renderMyWaitlists() {
    if (isGuest) return;

    const listEl = document.getElementById("myWaitlistsList");
    listEl.innerHTML = "";

    if (myWaitlistMachineIds.length === 0) {
        listEl.innerHTML = "<li>You are not on any waitlists.</li>";
        return;
    }

    myWaitlistMachineIds.forEach(id => {
        const machine = machines.find(m => m.machineId === id);
        if (!machine) return;

        const li = document.createElement("li");
        li.innerHTML = `
            <span>${machine.name} — <span class="status-waitlist">Waitlist</span></span>
        `;
        const btn = document.createElement("button");
        btn.textContent = "Leave Waitlist";
        btn.classList.add("btn-join", "btn-join-secondary");
        btn.onclick = () => leaveWaitlist(id);
        li.appendChild(btn);
        listEl.appendChild(li);
    });
}

/* rendering - machines table*/
function renderTable() {
    const tbody = document.getElementById("machineTableBody");
    const filterType = document.getElementById("filterType").value;

    tbody.innerHTML = "";

    let rows = machines.filter(
        m => filterType === "all" || m.type === filterType
    );

    rows.sort((a, b) => a.name.localeCompare(b.name));

    rows.forEach(machine => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${machine.name}</td>
            <td>${machine.type}</td>
        `;

        // Status Handling 
        const statusCell = document.createElement("td");
        const span = document.createElement("span");
        span.classList.add("status-badge");

        if (machine.status === "AVAILABLE") {
            span.classList.add("status-available");
            span.textContent = "Available";
        } else {
            span.classList.add("status-waitlist");
            span.textContent = "Waitlist Only";
        }
        statusCell.appendChild(span);
        tr.appendChild(statusCell);

        // Waitlist Count
        const count = machine.waitlistCount ?? 0;
        tr.innerHTML += `
            <td>${count}</td>
            <td>${computeEstimatedWait(count)}</td>
        `;

        // Action Column
        const actionTd = document.createElement("td");
        actionTd.classList.add("action-col");

        const btn = document.createElement("button");
        btn.classList.add("btn-join");

        if (isGuest) {
            btn.textContent = "Login to join";
            btn.disabled = true;
        } else if (machine.status === "AVAILABLE") {
            btn.textContent = "Book Now";
            btn.classList.add("btn-join-primary");
            btn.onclick = () =>
                (window.location.href = `reservation.html?machine=${machine.machineId}`);
        } else {
            btn.textContent = "Join Waitlist";
            btn.classList.add("btn-join-primary");
            btn.onclick = () => joinWaitlist(machine.machineId);
        }

        actionTd.appendChild(btn);
        tr.appendChild(actionTd);
        tbody.appendChild(tr);
    });
}

/* Init */
document.addEventListener("DOMContentLoaded", () => {
    if (isGuest) {
        document.body.classList.add("guest");

        // navbar for guest mode
        const nav = document.querySelector(".nav-links");
        if (nav) {
            nav.innerHTML = `
                <a href="waitlist.html?guest=1" class="active">Availability</a>
                <a href="login.html">Login</a>
            `;
        }
    }

    document.getElementById("filterType").addEventListener("change", renderTable);

    loadMachines();
});
