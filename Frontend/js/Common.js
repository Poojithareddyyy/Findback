// Get the logged-in user's token
function getAuthToken() {
    return sessionStorage.getItem("findbackToken");
}


// Check whether the user is logged in
async function protectPage(requiredRole) {

    const token = getAuthToken();

    // If token is missing, redirect to login
    if (!token) {
        window.location.href = "Login.html";
        return;
    }

    try {

        const response = await fetch(
            "http://localhost:8080/FindBack/SessionServlet",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        const result = await response.json();

        // Invalid or expired session
        if (!result.success) {
            sessionStorage.clear();
            window.location.href = "Login.html";
            return;
        }

        // Refresh the logged-in user's name from the backend
        // Get the actual name from the validated session
if (result.fullName) {

    sessionStorage.setItem("findbackName", result.fullName);

    const name = result.fullName;

    const currentName = document.getElementById("currentUserName");
    const welcomeName = document.getElementById("welcomeName");
    const avatar = document.querySelector(".profile-avatar");

    if (currentName) currentName.textContent = name;
    if (welcomeName) welcomeName.textContent = name;
    if (avatar) avatar.textContent = name.charAt(0).toUpperCase();
}

        // Check user role
        if (result.role !== requiredRole) {

            if (result.role === "ORG_ADMIN") {
                window.location.href = "AdminDashboard.html";
            } else {
                window.location.href = "UserDashboard.html";
            }

            return;
        }

    } catch (error) {

    console.error("Session validation error:", error);

    alert("Unable to verify your session. Please login again.");

    sessionStorage.removeItem("findbackToken");
    sessionStorage.removeItem("findbackRole");

    window.location.href = "Login.html";
    }
}


// Logout function
async function logoutUser() {

    const token = getAuthToken();

    try {

        if (token) {

            await fetch(
                "http://localhost:8080/FindBack/LogoutServlet",
                {
                    method: "POST",
                    headers: {
                        "Authorization": "Bearer " + token
                    }
                }
            );
        }

    } catch (error) {

        console.error("Logout error:", error);

    } finally {

        sessionStorage.removeItem("findbackToken");
        sessionStorage.removeItem("findbackRole");

        window.location.href = "Login.html";
    }
}