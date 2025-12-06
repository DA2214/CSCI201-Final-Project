const form = document.getElementById("reservationForm");
const machineSelect = document.getElementById("machine");
const durationSelect = document.getElementById("duration");
const message = document.getElementById("message");
const historyBody = document.getElementById("reservationHistory");
const startBtn = document.getElementById("startBtn");
const cancelBtn = document.getElementById("cancelBtn");
const reserveBtn = document.getElementById("reserveBtn");

const state = {
  machines: [],
  history: []
};

document.addEventListener("DOMContentLoaded", () => {
  loadMachines();
  loadReservations();
});

form.addEventListener("submit", (e) => {
  e.preventDefault();
  const duration = parseInt(durationSelect.value, 10);
  sendReservationAction("reserve", { duration });
});

startBtn.addEventListener("click", () => sendReservationAction("start"));
cancelBtn.addEventListener("click", () => sendReservationAction("cancel"));

/**
 * Load active reservations from backend and populate history table
 */
async function loadReservations() {
  const userId = resolveUserId();
  if (!userId) {
    renderHistory();
    return;
  }

  try {
    const resp = await fetch(`reservation?userId=${userId}`);
    if (!resp.ok) {
      throw new Error("Failed to load reservations.");
    }
    const reservations = await resp.json();
    
    // Convert backend format to frontend format
    state.history = Array.isArray(reservations) ? reservations.map(r => ({
      machineId: r.machineId,
      machineName: r.machineName || `Machine ${r.machineId}`,
      duration: r.intendedDuration,
      status: r.workoutStartTime ? "In Progress" : "Reserved",
      className: r.workoutStartTime ? "in-progress" : "reserved"
    })) : [];
    
    renderHistory();
  } catch (err) {
    console.error("Error loading reservations:", err);
    renderHistory(); // Render empty state on error
  }
}

/**
 * Load machines from backend and populate dropdown
 */
async function loadMachines() {
  machineSelect.innerHTML = `<option value=\"\">Loading machines...</option>`;
  machineSelect.disabled = true;
  try {
    const resp = await fetch("machines");
    if (!resp.ok) {
      throw new Error("Failed to load machines.");
    }
    const machines = await resp.json();
    state.machines = Array.isArray(machines) ? machines : [];
    renderMachineOptions();
  } catch (err) {
    showMessage(err.message || "Unable to fetch machines.", "#b00020");
    machineSelect.innerHTML = `<option value=\"\">Unable to load machines</option>`;
  } finally {
    machineSelect.disabled = false;
  }
}

function renderMachineOptions() {
  machineSelect.innerHTML = "";
  const placeholder = document.createElement("option");
  placeholder.value = "";
  placeholder.textContent = state.machines.length ? "-- Select --" : "No machines available";
  machineSelect.appendChild(placeholder);

  state.machines.forEach((m) => {
    const opt = document.createElement("option");
    opt.value = m.machineId;
    opt.textContent = `${m.name} (${formatMachineStatus(m.status)})`;
    machineSelect.appendChild(opt);
  });
}

/**
 * Main handler for reservation actions
 */
async function sendReservationAction(action, { duration } = {}) {
  const machineId = parseInt(machineSelect.value, 10);
  if (!machineId) {
    showMessage("Select a machine first.", "#b00020");
    return;
  }

  const userId = resolveUserId();
  if (!userId) {
    showMessage("Missing user ID. Add ?userId=123 to the URL or set sessionStorage/localStorage 'userId'.", "#b00020");
    return;
  }

  const payload = {
    action,
    userId: String(userId),
    machineId: String(machineId)
  };
  if (action === "reserve") {
    payload.duration = duration || durationSelect.value;
  }

  toggleButtons(true);
  try {
    const resp = await fetch("reservation", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await resp.json().catch(() => ({}));
    if (!resp.ok || data.success === false) {
      throw new Error(data.message || "Request failed.");
    }

    handleActionSuccess(action, data, { machineId, duration: payload.duration });
    loadMachines();
    loadReservations(); // Refresh reservations table after action
  } catch (err) {
    showMessage(err.message || "Something went wrong.", "#b00020");
  } finally {
    toggleButtons(false);
  }
}

function handleActionSuccess(action, data, meta) {
  const machineName = getMachineName(meta.machineId);
  let statusLabel = "";
  let className = "";
  let note = data.message || "";

  switch (action) {
    case "reserve":
      statusLabel = "Reserved";
      className = "reserved";
      if (!note) {
        note = "Machine reserved. Start within 5 minutes.";
      }
      updateHistoryEntry(meta.machineId, {
        machineName,
        duration: meta.duration || data.workoutDurationMinutes,
        status: statusLabel,
        className
      });
      break;
    case "start":
      statusLabel = "In Progress";
      className = "in-progress";
      if (!note) {
        note = "Workout started.";
      }
      updateHistoryEntry(meta.machineId, {
        machineName,
        status: statusLabel,
        className
      });
      break;
    case "cancel":
      statusLabel = "Cancelled";
      className = "cancelled";
      if (!note) {
        note = "Reservation cancelled.";
      }
      updateHistoryEntry(meta.machineId, {
        machineName,
        status: statusLabel,
        className
      });
      break;
    default:
      break;
  }

  showMessage(note, "#0a7a2d");
}

function updateHistoryEntry(machineId, payload) {
  const existingIndex = state.history.findIndex((item) => item.machineId === machineId);
  if (existingIndex >= 0) {
    state.history[existingIndex] = { ...state.history[existingIndex], ...payload };
  } else {
    state.history.unshift({ machineId, ...payload });
  }
  if (state.history.length > 8) {
    state.history.pop();
  }
  renderHistory();
}

function renderHistory() {
  historyBody.innerHTML = "";
  if (state.history.length === 0) {
    const emptyRow = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = 3;
    cell.className = "table-empty";
    cell.textContent = "No reservations yet.";
    emptyRow.appendChild(cell);
    historyBody.appendChild(emptyRow);
    return;
  }

  state.history.forEach((entry) => {
    const row = document.createElement("tr");
    const machineTd = document.createElement("td");
    machineTd.textContent = entry.machineName || `Machine ${entry.machineId}`;
    row.appendChild(machineTd);

    const durationTd = document.createElement("td");
    durationTd.textContent = entry.duration ? `${entry.duration} minutes` : "–";
    row.appendChild(durationTd);

    const statusTd = document.createElement("td");
    statusTd.className = entry.className || "";
    statusTd.textContent = entry.status || "";
    row.appendChild(statusTd);

    historyBody.appendChild(row);
  });
}

function getMachineName(machineId) {
  const machine = state.machines.find((m) => Number(m.machineId) === Number(machineId));
  return machine ? machine.name : `Machine ${machineId}`;
}

function formatMachineStatus(status) {
  switch ((status || "").toUpperCase()) {
    case "AVAILABLE":
      return "Available";
    case "RESERVED":
      return "Reserved";
    case "IN_USE":
      return "In Use";
    default:
      return "Unknown";
  }
}

function toggleButtons(disabled) {
  [reserveBtn, startBtn, cancelBtn].forEach((btn) => {
    if (btn) btn.disabled = disabled;
  });
}

function resolveUserId() {
  const params = new URLSearchParams(window.location.search);
  const fromQuery = params.get("userId");
  if (fromQuery && !Number.isNaN(Number(fromQuery))) {
    return Number(fromQuery);
  }
  const stored = sessionStorage.getItem("userId") || localStorage.getItem("userId");
  if (stored && !Number.isNaN(Number(stored))) {
    return Number(stored);
  }
  return null;
}

function showMessage(text, color) {
  message.innerHTML = text;
  message.style.color = color || "inherit";
  setTimeout(() => (message.textContent = ""), 5000);
}

const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    window.location.href = "login.html";
  });
}
