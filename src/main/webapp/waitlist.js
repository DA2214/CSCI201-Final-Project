/* guest mode*/
console.log("waitlist.js loaded.");
const params = new URLSearchParams(window.location.search);
const isGuest = params.get("guest") === "1";
console.log("Guest mode:", isGuest);

/* backend variables */
let machines = [];
let myWaitlistMachineIds = [];
let myWaitlists = []; // Store full waitlist data including machine names
// Get userID dynamically instead of as const to handle login after page load
function getUserID() {
    return localStorage.getItem("userId") || sessionStorage.getItem("userId");
}

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

        renderSummary();
        renderTable();
    } catch (err) {
        console.error("ERROR loading machines:", err);
    }
}

async function loadMyWaitlists() {
    const userID = getUserID();
    if (isGuest || !userID) {
        myWaitlistMachineIds = [];
        myWaitlists = [];
        renderMyWaitlists();
        return;
    }

    try {
        const response = await fetch(`/201Final/WaitlistServlet?userID=${userID}`);
        if (!response.ok) {
            throw new Error("Failed to load waitlists");
        }
        const waitlists = await response.json();
        
        if (waitlists && waitlists.success === false) {
            myWaitlistMachineIds = [];
            renderMyWaitlists();
            return;
        }
        
        myWaitlists = Array.isArray(waitlists) ? waitlists : [];
        myWaitlistMachineIds = myWaitlists.map(w => w.machineId).filter(id => id != null);
        
        renderMyWaitlists();
        renderSummary();
    } catch (err) {
        console.error("ERROR loading waitlists:", err);
        myWaitlistMachineIds = [];
        renderMyWaitlists();
    }
}

async function joinWaitlist(machineId) {
    if (isGuest) return alert("Please log in to join waitlists.");
    const userID = getUserID();
    if (!userID) return alert("Missing userID — please log in again.");

    const body = { userID, machineID: machineId };

    const res = await fetch("/201Final/WaitlistServlet/join", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const data = await res.json();
    alert(data.message);
    await loadMachines();
    await loadMyWaitlists();
}

async function leaveWaitlist(machineId) {
    const userID = getUserID();
    if (!userID) return;

    const body = { userID, machineID: machineId };

    const res = await fetch("/201Final/WaitlistServlet/decline", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const data = await res.json();
    alert(data.message);
    await loadMachines(); // Wait for machines to load first
    await loadMyWaitlists(); // Then refresh my waitlists after leaving
}

/* rendering - summary chips*/
function renderSummary() {
    document.getElementById("summaryTotal").textContent = machines.length;
    document.getElementById("summaryFull").textContent =
        machines.filter(m => m.status !== "AVAILABLE").length;

    if (!isGuest) {
        const summaryEl = document.getElementById("summaryMyWaitlists");
        if (summaryEl) {
            summaryEl.textContent = myWaitlistMachineIds.length;
        }
    }
}

/* rebdering - waitlist*/
function renderMyWaitlists() {
    if (isGuest) return;

    const listEl = document.getElementById("myWaitlistsList");
    if (!listEl) return;
    listEl.innerHTML = "";

    if (myWaitlistMachineIds.length === 0) {
        listEl.innerHTML = "<li>You are not on any waitlists.</li>";
        return;
    }
    myWaitlistMachineIds.forEach(id => {
        // Find waitlist entry with machine name from backend
        const waitlistEntry = myWaitlists.find(w => w.machineId === id);
        let machineName = null;
        
        if (waitlistEntry && waitlistEntry.machineName) {
            // Use machine name from backend response
            machineName = waitlistEntry.machineName;
        } else {
            // Fallback: try to find in machines array
            const machineIdNum = typeof id === 'string' ? parseInt(id, 10) : id;
            const machine = machines.find(m => Number(m.machineId) === machineIdNum);
            machineName = machine ? machine.name : null;
        }
        
        const li = document.createElement("li");
        const displayName = machineName || `Machine ${id}`;
        li.innerHTML = `
            <span>${displayName} — <span class="status-waitlist">Waitlist</span></span>
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

    // Load machines first, then waitlists (so waitlists can find machine names)
    loadMachines().then(() => loadMyWaitlists());
});
