function loginUser() {
    let u = document.getElementById("login-username").value.trim();
    let p = document.getElementById("login-password").value.trim();
    let err = document.getElementById("login-error");

    if (u === "" || p === "") {
        err.textContent = "Enter username and password.";
        return;
    }

    err.textContent = "";

    fetch("LoginRequestServlet", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `username=${encodeURIComponent(u)}&password=${encodeURIComponent(p)}`
    })
    .then(async response => {
        if (response.status === 202) {
            let data = await response.json();
            localStorage.setItem("userId", data.userId);

            window.location.href = "waitlist.html";
        }
        else if (response.status === 400) {
            err.textContent = "Invalid username.";
        }
        else if (response.status === 404) {
            err.textContent = "Incorrect username or password.";
        }
        else {
            err.textContent = "Unexpected server error.";
        }
    })
    .catch(() => {
        err.textContent = "Network error.";
    });
}