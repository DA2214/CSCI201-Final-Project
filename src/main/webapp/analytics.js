// ======================
// Fetch data from server
// ======================

const LOGGED_IN_USER_ID = localStorage.getItem("userId");

async function fetchUserHistory() {
  try {
    const response = await fetch(`AnalyticsServlet?userID=${LOGGED_IN_USER_ID}`);

    if (!response.ok) {
      throw new Error("Server returned " + response.status);
    }

    const data = await response.json();
    console.log("Fetched data:", data);

    // Only use reservations now
    return data.reservations || [];

  } catch (err) {
    console.error("Error fetching user history:", err);
    return [];
  }
}

// ======================
// Date & Time Helpers
// ======================

function parseDate(dateStr) {
  const [y, m, d] = dateStr.split("-").map(Number);
  return new Date(y, m - 1, d);
}

function timeToMinutes(t) {
  const [h, m] = t.split(":").map(Number);
  return h * 60 + m;
}

function filterByRange(data, days) {
  if (days === "all") return data.slice();

  const now = new Date();
  const cutoff = new Date(now);
  cutoff.setDate(now.getDate() - Number(days));

  return data.filter((item) => parseDate(item.date) >= cutoff);
}

// ======================
// Summary Calculations (Reservations Only)
// ======================

function computeTotals(reservations) {
  const now = new Date();
  const weekCutoff = new Date(now);
  weekCutoff.setDate(now.getDate() - 7);

  const monthCutoff = new Date(now);
  monthCutoff.setDate(now.getDate() - 30);

  let weekly = 0;
  let monthly = 0;

  reservations.forEach((r) => {
    const d = parseDate(r.date);
    if (isNaN(d)) return;

    const duration = timeToMinutes(r.endTime) - timeToMinutes(r.startTime);

    if (d >= weekCutoff) weekly += duration;
    if (d >= monthCutoff) monthly += duration;
  });

  return { weekly, monthly };
}

function computeTopMachine(reservations) {
  if (!reservations.length) return "–";

  const totals = {};

  reservations.forEach((r) => {
    const duration = timeToMinutes(r.endTime) - timeToMinutes(r.startTime);
    totals[r.machineName] = (totals[r.machineName] || 0) + duration;
  });

  let bestName = "–";
  let bestMinutes = 0;

  for (const [name, minutes] of Object.entries(totals)) {
    if (minutes > bestMinutes) {
      bestMinutes = minutes;
      bestName = name;
    }
  }

  return bestName;
}

function renderSummary(reservations) {
  const { weekly, monthly } = computeTotals(reservations);

  document.getElementById("weeklyMinutes").textContent = `${weekly} min`;
  document.getElementById("monthlyMinutes").textContent = `${monthly} min`;
  document.getElementById("topMachine").textContent = computeTopMachine(reservations);
}

// ======================
// Table Rendering (Reservations Only)
// ======================

function renderReservationTable(reservations) {
  const tbody = document.querySelector("#reservationTable tbody");
  const emptyMsg = document.getElementById("reservationEmpty");

  tbody.innerHTML = "";

  if (!reservations.length) {
    emptyMsg.style.display = "block";
    return;
  }

  emptyMsg.style.display = "none";

  const sorted = reservations
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

// ======================
// Update UI on dropdown
// ======================

let serverReservations = [];

function updateView() {
  const days = document.getElementById("timeRange").value;
  const filtered = filterByRange(serverReservations, days);

  // Summary uses ALL reservations
  renderSummary(serverReservations);

  // Table uses filtered range
  renderReservationTable(filtered);
}

// ======================
// Buttons & Initialization
// ======================

document.addEventListener("DOMContentLoaded", async () => {
  console.log("Dashboard JS Loaded!");

  document.getElementById("timeRange").addEventListener("change", updateView);

  // Fetch data & render UI
  serverReservations = await fetchUserHistory();
  updateView();
});
