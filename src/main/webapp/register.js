async function registerUser() {
    let u = document.getElementById("reg-username").value.trim();
    let e = document.getElementById("reg-email").value.trim();
    let p1 = document.getElementById("reg-password").value.trim();
    let p2 = document.getElementById("reg-confirm").value.trim();
    let err = document.getElementById("register-error");

    if (u === "" || e === "" || p1 === "" || p2 === "") {
        err.textContent = "Fill all fields.";
        return;
    }

    if (p1 !== p2) {
        err.textContent = "Passwords do not match.";
        return;
    }

    err.textContent = "";

    try {
        // Check username exists
        let respUser = await fetch("UsernameExistsServlet?username=" + encodeURIComponent(u));
        if (respUser.status === 200) {
            err.textContent = "Username already exists.";
            return;
        }

        // Check email exists
        let respEmail = await fetch("EmailExistsServlet?email=" + encodeURIComponent(e));
        if (respEmail.status === 200) {
            err.textContent = "Email already exists.";
            return;
        }

        // Register user
        let resp = await fetch("AccountRegistrationServlet", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(u)}&email=${encodeURIComponent(e)}&password=${encodeURIComponent(p1)}`
        });

        if (resp.status === 202) {
            let data = await resp.json();
            localStorage.setItem("userId", data.userId);
            window.location.href = "waitlist.html";
        } 
        else if (resp.status === 406) {
            err.textContent = "Username or email already in use.";
        } 
        else if (resp.status === 400) {
            err.textContent = "Invalid username or email.";
        } 
        else {
            err.textContent = "Registration failed.";
        }
    }
    catch (e) {
        err.textContent = "Connection error.";
    }
}

