// --- Guest mode detection --------------------------------------
console.log("waitlist.js loaded, search =", window.location.search);
const params = new URLSearchParams(window.location.search);
const isGuest = params.get("guest") === "1";
console.log("isGuest =", isGuest);

// --- test data ----------------------------------

// average reservation duration in minutes (for estimated wait)
const AVERAGE_SLOT_MINUTES = 30;

// Example machine data (type added)
const machines = [
    { id: 1, name: "Treadmill 1",   type: "cardio",   status: "Available", waitlistCount: 0 },
    { id: 2, name: "Treadmill 2",   type: "cardio",   status: "Full",      waitlistCount: 2 },
    { id: 3, name: "Bike 1",        type: "cardio",   status: "Full",      waitlistCount: 1 },
    { id: 4, name: "Elliptical 1",  type: "cardio",   status: "Available", waitlistCount: 0 },
    { id: 5, name: "Bench Press 1", type: "strength", status: "Full",      waitlistCount: 3 },
    { id: 6, name: "Lat Pulldown",  type: "strength", status: "Full",      waitlistCount: 5 },
    { id: 7, name: "Leg Press",     type: "strength", status: "Available", waitlistCount: 0 }
];

// demo - track which machine IDs this user is waitlisted on 
let myWaitlistMachineIds = [];

// --- Helpers ------------------------------------------------------

function computeEstimatedWait(count) {
    if (count <= 0) return "–";
    const minutes = count * AVERAGE_SLOT_MINUTES;
    if (minutes < 60) return `${minutes} min`;
    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins === 0 ? `${hrs} hr` : `${hrs} hr ${mins} min`;
}

function isOnMyWaitlist(machineId) {
    return myWaitlistMachineIds.includes(machineId);
}

// --- Rendering ----------------------------------------------------

function renderSummary() {
    const total = machines.length;
    const full = machines.filter(m => m.status === "Full").length;

    document.getElementById("summaryTotal").textContent = total;
    document.getElementById("summaryFull").textContent = full;

    // ⭐ NEW (guest mode): only show personal count if NOT guest
    const myEl = document.getElementById("summaryMyWaitlists");
    if (!isGuest && myEl) {
        const myCount = myWaitlistMachineIds.length;
        myEl.textContent = myCount;
    }
}

function renderMyWaitlists() {
    // ⭐ NEW (guest mode): guests shouldn't see personal list
    if (isGuest) return;

    const listEl = document.getElementById("myWaitlistsList");
    if (!listEl) return;

    listEl.innerHTML = "";

    if (myWaitlistMachineIds.length === 0) {
        const empty = document.createElement("li");
        empty.textContent = "You are not on any waitlists yet.";
        listEl.appendChild(empty);
        return;
    }

    myWaitlistMachineIds.forEach(id => {
        const machine = machines.find(m => m.id === id);
        if (!machine) return;

        const li = document.createElement("li");
        li.innerHTML = `
            <span>${machine.name} – <span class="status-waitlist">Waitlist</span></span>
        `;

        const leaveBtn = document.createElement("button");
        leaveBtn.textContent = "Leave Waitlist";
        leaveBtn.classList.add("btn-join", "btn-join-secondary");
        leaveBtn.onclick = () => leaveWaitlist(machine.id);

        li.appendChild(leaveBtn);
        listEl.appendChild(li);
    });
}

function renderTable() {
    const tbody = document.getElementById("machineTableBody");
    const filterType = document.getElementById("filterType").value;

    tbody.innerHTML = "";

    const rowsToShow = machines.filter(m =>
        filterType === "all" ? true : m.type === filterType
    );

    // sorting available machines
    rowsToShow.sort((a, b) => {
        if (a.status === b.status) return a.name.localeCompare(b.name);
        return a.status === "Available" ? -1 : 1;
    });

    rowsToShow.forEach(machine => {
        const tr = document.createElement("tr");

        // Machine name
        const nameTd = document.createElement("td");
        nameTd.textContent = machine.name;
        tr.appendChild(nameTd);

        // Type
        const typeTd = document.createElement("td");
        typeTd.textContent = machine.type === "cardio" ? "Cardio" : "Strength";
        tr.appendChild(typeTd);

        // Status
        const statusTd = document.createElement("td");
        const statusSpan = document.createElement("span");
        statusSpan.classList.add("status-badge");
        if (machine.status === "Available") {
            statusSpan.classList.add("status-available");
            statusSpan.textContent = "Available";
        } else {
            statusSpan.classList.add("status-waitlist");
            statusSpan.textContent = "Waitlist Only";
        }
        statusTd.appendChild(statusSpan);
        tr.appendChild(statusTd);

        // Waitlist count
        const countTd = document.createElement("td");
        countTd.textContent = machine.waitlistCount;
        tr.appendChild(countTd);

        // Estimated wait
        const waitTd = document.createElement("td");
        waitTd.textContent = computeEstimatedWait(machine.waitlistCount);
        tr.appendChild(waitTd);

        // ⭐ Action column
        const actionTd = document.createElement("td");
        actionTd.classList.add("action-col"); // ⭐ for CSS to hide in guest mode

        const btn = document.createElement("button");
        btn.classList.add("btn-join");

        if (isGuest) {
            // ⭐ NEW (guest mode): no real actions
            // if you are hiding the whole column with CSS, user won't see this anyway
            btn.textContent = "Log in to join";
            btn.classList.add("btn-join-disabled");
            btn.disabled = true;
        } else {
            // existing logged-in behavior
            if (machine.status === "Available") {
                btn.textContent = "Book Now";
                btn.classList.add("btn-join-primary");
                btn.onclick = () => {
                    // maybe pass machine ID as a query param ???
                    window.location.href = "reservation.html";
                };
            } else {
                if (isOnMyWaitlist(machine.id)) {
                    btn.textContent = "On Waitlist";
                    btn.classList.add("btn-join-disabled");
                } else {
                    btn.textContent = "Join Waitlist";
                    btn.classList.add("btn-join-primary");
                    btn.onclick = () => joinWaitlist(machine.id);
                }
            }
        }

        actionTd.appendChild(btn);
        tr.appendChild(actionTd);

        tbody.appendChild(tr);
    });
}

// --- Actions ------------------------------------------------------

function joinWaitlist(machineId) {
    // guests shouldn't be able to call this (extra guard)
    if (isGuest) return;

    const machine = machines.find(m => m.id === machineId);
    if (!machine) return;

    if (!isOnMyWaitlist(machineId)) {
        myWaitlistMachineIds.push(machineId);
        machine.waitlistCount += 1;
        alert(`You have been added to the waitlist for ${machine.name}.`);
    }

    renderSummary();
    renderTable();
    renderMyWaitlists();

    // backend: servlet call:
    /*
    fetch('JoinWaitlistServlet', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ machineId })
    });
    */
}

function leaveWaitlist(machineId) {
    // guests shouldn't be here either, but just in case:
    if (isGuest) return;

    const machine = machines.find(m => m.id === machineId);
    if (!machine) return;

    myWaitlistMachineIds = myWaitlistMachineIds.filter(id => id !== machineId);
    if (machine.waitlistCount > 0) {
        machine.waitlistCount -= 1;
    }

    renderSummary();
    renderTable();
    renderMyWaitlists();
}

// --- Init ---------------------------------------------------------

// ⭐ NEW: move init logic into a function, called after we toggle guest class
function initWaitlistPage() {
    const filterSelect = document.getElementById("filterType");
    if (filterSelect) {
        filterSelect.addEventListener("change", () => {
            renderTable();
        });
    }

    renderSummary();
    renderTable();
    renderMyWaitlists();
}

document.addEventListener("DOMContentLoaded", () => {
    if (isGuest) {
        document.body.classList.add("guest");

        // keep guest flag when clicking Availability in the navbar
        const availabilityLink = document.querySelector('.nav-links a[href="waitlist.html"]');
        if (availabilityLink) {
            availabilityLink.href = "waitlist.html?guest=1";
        }
    }

    initWaitlistPage();
});

