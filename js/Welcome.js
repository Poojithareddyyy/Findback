document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("welcomeTitle").textContent = sessionStorage.getItem("fbWelcomeTitle") || "You're all set!";
    document.getElementById("welcomeMessage").textContent = sessionStorage.getItem("fbWelcomeMessage") || "Welcome to FindBack. Let's make lost things found again.";
    document.getElementById("welcomeContinue").href = sessionStorage.getItem("fbWelcomeNext") || "Login.html";
    const code = sessionStorage.getItem("fbOrganizationCode");
    if (code) { const box = document.getElementById("welcomeCode"); box.textContent = "Organization code: " + code; box.style.display = "block"; }
    ["fbWelcomeTitle", "fbWelcomeMessage", "fbWelcomeNext", "fbOrganizationCode"].forEach(k => sessionStorage.removeItem(k));
});