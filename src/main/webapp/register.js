function registerUser() {
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
    alert("Registration request sent.");
}
