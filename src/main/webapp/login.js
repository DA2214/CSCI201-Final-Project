async function loginUser() {
    let u = document.getElementById("login-username").value.trim();
    let p = document.getElementById("login-password").value.trim();
    let err = document.getElementById("login-error");

    if (u === "" || p === "") {
        err.textContent = "Enter username and password.";
        return;
    }

    err.textContent = "";

    try {
        let resp = await fetch("LoginRequestServlet", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(u)}&password=${encodeURIComponent(p)}`
        });

        if (resp.status === 202) {
            let data = await resp.json();
            localStorage.setItem("userId", data.userId);
			setTimeout(() => {
				window.location.href = "waitlist.html";
			}, 150);  // <-- give session cookie time to register
        } 
        else if (resp.status === 404) {
            err.textContent = "Incorrect username or password.";
        } 
        else if (resp.status === 400) {
            err.textContent = "Invalid username.";
        } 
        else {
            err.textContent = "Login failed.";
        }
    } 
    catch (e) {
        err.textContent = "Connection error.";
    }
}
