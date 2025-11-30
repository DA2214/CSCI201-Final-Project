// Simulated "logged in" user data for frontend-only demo.
// In the real app, this would be fetched from the server using userID.

const mockUsageData = [
  // usageID, userID, machineID, duration (minutes), date
  { usageID: 1, userID: 101, machineID: "TREAD_1", machineName: "Treadmill 1", duration: 30, date: "2025-11-24" },
  { usageID: 2, userID: 101, machineID: "TREAD_2", machineName: "Treadmill 2", duration: 45, date: "2025-11-22" },
  { usageID: 3, userID: 101, machineID: "BIKE_1",  machineName: "Bike 1",      duration: 20, date: "2025-11-20" },
  { usageID: 4, userID: 101, machineID: "BENCH_1", machineName: "Bench Press 1", duration: 40, date: "2025-11-18" },
  { usageID: 5, userID: 101, machineID: "TREAD_1", machineName: "Treadmill 1", duration: 60, date: "2025-11-10" }
];

const mockReservationData = [
  // reservationId, userID, machineID, machineName, startTime, endTime, status, date
  {
    reservationId: 11,
    userID: 101,
    machineID: "TREAD_1",
    machineName: "Treadmill 1",
    startTime: "08:00",
    endTime: "09:00",
    status: "Completed",
    date: "2025-11-24"
  },
  {
    reservationId: 12,
    userID: 101,
    machineID: "BIKE_1",
    machineName: "Bike 1",
    startTime: "18:00",
    endTime: "19:00",
    status: "Cancelled",
    date: "2025-11-21"
  },
  {
    reservationId: 13,
    userID: 101,
    machineID: "BENCH_1",
    machineName: "Bench Press 1",
    startTime: "16:00",
    endTime: "17:00",
    status: "Completed",
    date: "2025-11-18"
  }
];

// Utility: parse YYYY-MM-DD into Date at local midnight
function parseDate(dateStr) {
  const [y, m, d] = dateStr.split("-").map(Number);
  return new Date(y, m - 1, d);
}

// Return records in last N days; if "all", no filtering.
function filterByRange(data, days) {
  if (days === "all") return data.slice();

  const now = new Date();
  const cutoff = new Date(now);
  cutoff.setDate(now.getDate() - Number(days));
  return data.filter((item) => parseDate(item.date) >= cutoff);
}

// Compute total minutes for last 7 days / 30 days
function computeTotals(usageData) {
  const now = new Date();
  const weekCutoff = new Date(now);
  weekCutoff.setDate(now.getDate() - 7);

  const monthCutoff = new Date(now);
  monthCutoff.setDate(now.getDate() - 30);

  let weekly = 0;
  let monthly = 0;

  usageData.forEach((u) => {
    const d = parseDate(u.date);
    if (d >= weekCutoff) {
      weekly += u.duration;
    }
    if (d >= monthCutoff) {
      monthly += u.duration;
    }
  });

  return { weekly, monthly };
}

// Compute most used machine name based on total duration
function computeTopMachine(usageData) {
  if (!usageData.length) return "–";
  const totalsByMachine = {};
  usageData.forEach((u) => {
    const key = u.machineName;
    totalsByMachine[key] = (totalsByMachine[key] || 0) + u.duration;
  });
  let bestName = "–";
  let bestMinutes = 0;
  for (const [name, minutes] of Object.entries(totalsByMachine)) {
    if (minutes > bestMinutes) {
      bestMinutes = minutes;
      bestName = name;
    }
  }
  return bestName;
}

// Render functions
function renderSummary(usageData) {
  const { weekly, monthly } = computeTotals(usageData);
  document.getElementById("weeklyMinutes").textContent = `${weekly} min`;
  document.getElementById("monthlyMinutes").textContent = `${monthly} min`;
  document.getElementById("topMachine").textContent = computeTopMachine(usageData);
}

function renderUsageTable(usageData) {
  const tbody = document.querySelector("#usageTable tbody");
  const emptyMsg = document.getElementById("usageEmpty");
  tbody.innerHTML = "";

  if (usageData.length === 0) {
    emptyMsg.style.display = "block";
    return;
  }
  emptyMsg.style.display = "none";

  // Sort newest first
  const sorted = usageData.slice().sort((a, b) => parseDate(b.date) - parseDate(a.date));

  sorted.forEach((u) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${u.date}</td>
      <td>${u.machineName}</td>
      <td>${u.duration} min</td>
    `;
    tbody.appendChild(tr);
  });
}

function renderReservationTable(reservationData) {
  const tbody = document.querySelector("#reservationTable tbody");
  const emptyMsg = document.getElementById("reservationEmpty");
  tbody.innerHTML = "";

  if (reservationData.length === 0) {
    emptyMsg.style.display = "block";
    return;
  }
  emptyMsg.style.display = "none";

  const sorted = reservationData
    .slice()
    .sort((a, b) => parseDate(b.date) - parseDate(a.date));

  sorted.forEach((r) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${r.date}</td>
      <td>${r.machineName}</td>
      <td>${r.startTime}</td>
      <td>${r.endTime}</td>
      <td>${r.status}</td>
    `;
    tbody.appendChild(tr);
  });
}

// Apply filter based on dropdown
function updateView() {
  const rangeSelect = document.getElementById("timeRange");
  const days = rangeSelect.value;

  const filteredUsage = filterByRange(mockUsageData, days);
  const filteredReservations = filterByRange(mockReservationData, days);

  renderSummary(mockUsageData); // summary is always based on real recent usage
  renderUsageTable(filteredUsage);
  renderReservationTable(filteredReservations);
}

// Handle data erase request (frontend-only)
function handleEraseRequest() {
  const msg = document.getElementById("eraseMessage");
  msg.textContent =
    "Data erasure request submitted. In the real system, this would notify the server to remove your Usage and Reservation records.";
}

// Handle logout button (frontend-only)
function handleLogout() {
  // In real app: clear auth token / session and redirect
  window.location.href = "login.html";
}

// Init
document.addEventListener("DOMContentLoaded", () => {
  document
    .getElementById("timeRange")
    .addEventListener("change", updateView);

  document
    .getElementById("eraseDataBtn")
    .addEventListener("click", handleEraseRequest);

  document
    .getElementById("logoutBtn")
    .addEventListener("click", handleLogout);

  updateView();
});
