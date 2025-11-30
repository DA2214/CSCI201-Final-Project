// ======================
// Fetch data from server
// ======================

// You must set this from login/session
const LOGGED_IN_USER_ID = 101;

async function fetchUserHistory() {
  try {
    const response = await fetch(`AnalyticsServlet?userID=${LOGGED_IN_USER_ID}`);

    if (!response.ok) {
      throw new Error("Server returned " + response.status);
    }

    const data = await response.json();

	console.log(data);
	
    // Expecting: { machineUsage: [...], reservations: [...] }
    return {
      usage: data.machineUsage || [],
      reservations: data.reservations || []
    };

  } catch (err) {
    console.error("Error fetching user history:", err);
    return { usage: [], reservations: [] };
  }
}

// ======================
// Date helpers
// ======================

function parseDate(dateStr) {
  const [y, m, d] = dateStr.split("-").map(Number);
  return new Date(y, m - 1, d);
}

function filterByRange(data, days) {
  if (days === "all") return data.slice();

  const now = new Date();
  const cutoff = new Date(now);
  cutoff.setDate(now.getDate() - Number(days));

  return data.filter((item) => parseDate(item.date) >= cutoff);
}

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
    if (d >= weekCutoff) weekly += u.duration;
    if (d >= monthCutoff) monthly += u.duration;
  });

  return { weekly, monthly };
}

function computeTopMachine(usageData) {
  if (!usageData.length) return "–";
  const totals = {};

  usageData.forEach((u) => {
    totals[u.machineName] = (totals[u.machineName] || 0) + u.duration;
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

// ======================
// Rendering functions
// ======================

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

  const sorted = usageData
    .slice()
    .sort((a, b) => parseDate(b.date) - parseDate(a.date));

  sorted.forEach((u) => {
	console.log(u);
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
	console.log(r);
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
// Update UI based on dropdown
// ======================

let serverUsage = [];
let serverReservations = [];

function updateView() {
  const days = document.getElementById("timeRange").value;

  console.log(days);
  console.log(serverUsage);
  const filteredUsage = filterByRange(serverUsage, days);
  console.log(serverReservations);
  const filteredReservations = filterByRange(serverReservations, days);

  console.log
  renderSummary(serverUsage); // summary always uses full usage
  renderUsageTable(filteredUsage);
  renderReservationTable(filteredReservations);
}

// ======================
// Buttons & Initialization
// ======================

function handleLogout() {
  window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", async () => {
	console.log("Doing JS");
  document.getElementById("timeRange").addEventListener("change", updateView);
  document.getElementById("logoutBtn").addEventListener("click", handleLogout);

  // 🌟 Fetch real data from your servlet
  const data = await fetchUserHistory();
  serverUsage = data.usage;
  serverReservations = data.reservations;
  updateView();
});
