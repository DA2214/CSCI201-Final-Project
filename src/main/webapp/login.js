function loginUser() {
    let u = document.getElementById("login-username").value.trim();
    let p = document.getElementById("login-password").value.trim();
    let err = document.getElementById("login-error");

    if (u === "" || p === "") {
        err.textContent = "Enter username and password.";
        return;
    }

    err.textContent = "";
    alert("Login request sent.");
}
