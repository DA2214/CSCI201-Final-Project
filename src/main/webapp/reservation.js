const form = document.getElementById("reservationForm");
const machineSelect = document.getElementById("machine");
const durationSelect = document.getElementById("duration");
const message = document.getElementById("message");

// Simulated reserved machines
const reservedMachines = ["Bench Press 1", "Bike 1"];

form.addEventListener("submit", (e) => {
  e.preventDefault();
  const machine = machineSelect.value;
  const duration = durationSelect.value;

  if (!machine) {
    showMessage("⚠️ Please select a machine.", "red");
    return;
  }

  if (reservedMachines.includes(machine)) {
    showMessage(`<strong>${machine}</strong> is full. You’ve been added to the waitlist.`, "orange");
  } else {
    showMessage(`<strong>${machine}</strong> reserved successfully for ${duration} minutes!`, "green");
    reservedMachines.push(machine);
    addToHistory(machine, duration, "Confirmed");
  }

  form.reset();
});

function showMessage(text, color) {
  message.innerHTML = text;
  message.style.color = color;
  setTimeout(() => (message.textContent = ""), 5000);
}

function addToHistory(machine, duration, status) {
  const table = document.getElementById("reservationHistory");
  const row = document.createElement("tr");
  row.innerHTML = `
    <td>${machine}</td>
    <td>${duration} minutes</td>
    <td class="${status === "Confirmed" ? "confirmed" : "waitlisted"}">${status}</td>
  `;
  table.appendChild(row);
}
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    // In a real app you'd clear auth/session here
    window.location.href = "login.html";
  });
}

