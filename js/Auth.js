const organizationForm = document.forms["organizationForm"];

if (organizationForm) {

    organizationForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const orgName = organizationForm["organizationName"].value.trim();
        const orgType = organizationForm["organizationType"].value;
        const orgEmail = organizationForm["organizationEmail"].value.trim();
        const phone = organizationForm["organizationPhone"].value.trim();

        const adminName = organizationForm["adminName"].value.trim();
        const adminEmail = organizationForm["adminEmail"].value.trim();

        const password = organizationForm["adminPassword"].value;
        const confirmPassword = organizationForm["confirmPassword"].value;

        const error = document.getElementById("formError");

        error.style.color = "#C43E3E";
        error.textContent = "";

        // 1. Required fields
        if (!orgName || !orgType || !orgEmail ||
            !adminName || !adminEmail || !password || !confirmPassword) {

            error.textContent = "Please fill in all required fields.";
            return;
        }

        // 2. Organization name validation
        if (orgName.length < 2 || orgName.length > 150) {
            error.textContent = "Organization name must contain 2 to 150 characters.";
            return;
        }

        // 3. Admin name validation
        if (!/^[a-zA-Z\s.'-]{2,100}$/.test(adminName)) {
            error.textContent = "Please enter a valid administrator name.";
            return;
        }

        // 4. Email validation
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailPattern.test(orgEmail)) {
            error.textContent = "Please enter a valid organization email.";
            return;
        }

        if (!emailPattern.test(adminEmail)) {
            error.textContent = "Please enter a valid administrator email.";
            return;
        }

        // 5. Phone validation (optional field)
        if (phone !== "" && !/^[0-9]{10,15}$/.test(phone)) {
            error.textContent = "Phone number must contain 10 to 15 digits.";
            return;
        }

        // 6. Password minimum length
        if (password.length < 8) {
            error.textContent = "Password must contain at least 8 characters.";
            return;
        }

        // 7. Password strength
        const strongPassword =
            /[A-Z]/.test(password) &&
            /[a-z]/.test(password) &&
            /[0-9]/.test(password) &&
            /[^A-Za-z0-9]/.test(password);

        if (!strongPassword) {
            error.textContent =
                "Password must include uppercase, lowercase, number and special character.";
            return;
        }

        // 8. Confirm password
        if (password !== confirmPassword) {
            error.textContent = "Passwords do not match.";
            return;
        }

        // All frontend validations passed
        error.style.color = "#159B91";
        error.textContent = "Please wait...";

        registerOrganization();

    });

}


/* =====================================
   MEMBER REGISTRATION
===================================== */

const memberForm = document.forms["memberForm"];

if (memberForm) {

    memberForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const orgCode = memberForm["organizationCode"].value.trim();
        const fullName = memberForm["fullName"].value.trim();
        const email = memberForm["email"].value.trim();

        const password = memberForm["password"].value;
        const confirmPassword = memberForm["confirmPassword"].value;

        const error = document.getElementById("memberError");

        error.style.color = "#C43E3E";
        error.textContent = "";

        // 1. Required fields
        if (!orgCode || !fullName || !email ||
            !password || !confirmPassword) {

            error.textContent = "Please fill in all required fields.";
            return;
        }

        // 2. Organization code format
        if (!/^FB[A-Z0-9]{6}$/.test(orgCode.toUpperCase())) {
            error.textContent = "Please enter a valid organization code.";
            return;
        }

        // 3. Name validation
        if (!/^[a-zA-Z\s.'-]{2,100}$/.test(fullName)) {
            error.textContent = "Please enter a valid full name.";
            return;
        }

        // 4. Email validation
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailPattern.test(email)) {
            error.textContent = "Please enter a valid email address.";
            return;
        }

        // 5. Password length
        if (password.length < 8) {
            error.textContent = "Password must contain at least 8 characters.";
            return;
        }

        // 6. Password strength
        const strongPassword =
            /[A-Z]/.test(password) &&
            /[a-z]/.test(password) &&
            /[0-9]/.test(password) &&
            /[^A-Za-z0-9]/.test(password);

        if (!strongPassword) {
            error.textContent =
                "Password must include uppercase, lowercase, number and special character.";
            return;
        }

        // 7. Confirm password
        if (password !== confirmPassword) {
            error.textContent = "Passwords do not match.";
            return;
        }

        error.style.color = "#159B91";
        error.textContent = "Please wait...";

        registerMember();

        // Backend connection will be added next

    });

}


/* =====================================
   ORGANIZATION BACKEND CONNECTION
===================================== */

async function registerOrganization() {

    const form = document.forms["organizationForm"];
    const error = document.getElementById("formError");
    const submitButton = form.querySelector('button[type="submit"]');

    submitButton.disabled = true;

    try {

        const formData = new URLSearchParams();

        formData.append("organizationName", form["organizationName"].value.trim());
        formData.append("organizationType", form["organizationType"].value);
        formData.append("organizationEmail", form["organizationEmail"].value.trim());
        formData.append("organizationPhone", form["organizationPhone"].value.trim());
        formData.append("adminName", form["adminName"].value.trim());
        formData.append("adminEmail", form["adminEmail"].value.trim());
        formData.append("adminPassword", form["adminPassword"].value);
        formData.append("confirmPassword", form["confirmPassword"].value);

        const response = await fetch("http://localhost:8080/FindBack/OrganizationRegisterServlet", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData
        });

        const result = await response.json();

        if (result.success) {

            sessionStorage.setItem("fbWelcomeTitle", "Your organization is ready!");
            sessionStorage.setItem("fbWelcomeMessage", "Welcome to FindBack. Your workspace has been created successfully. Share the organization code with your members so they can join.");
            sessionStorage.setItem("fbWelcomeNext", "Login.html");
            sessionStorage.setItem("fbOrganizationCode", result.organizationCode || "");
            window.location.href = "Welcome.html";
            return;

        } else {

            error.style.color = "#C43E3E";
            error.textContent = result.message;

        }

    } catch (e) {

        error.style.color = "#C43E3E";
        error.textContent =
            "Unable to connect to the server. Please try again.";

    } finally {

        submitButton.disabled = false;

    }

}



async function registerMember() {

    const form = document.forms["memberForm"];
    const error = document.getElementById("memberError");
    const button = form.querySelector('button[type="submit"]');

    button.disabled = true;

    try {

        const formData = new URLSearchParams();

        formData.append("organizationCode", form["organizationCode"].value.trim());
        formData.append("fullName", form["fullName"].value.trim());
        formData.append("email", form["email"].value.trim());
        formData.append("password", form["password"].value);
        formData.append("confirmPassword", form["confirmPassword"].value);

        const response = await fetch(
            "http://localhost:8080/FindBack/MemberRegisterServlet",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData
            }
        );

        const result = await response.json();

        if (result.success) {
            sessionStorage.setItem("fbWelcomeTitle", "Welcome to FindBack!");
            sessionStorage.setItem("fbWelcomeMessage", "Your member account has been created successfully. Sign in to explore your organization's Lost & Found workspace.");
            sessionStorage.setItem("fbWelcomeNext", "Login.html");
            window.location.href = "Welcome.html";
            return;
        }
        error.style.color = "#C43E3E";
        error.textContent = result.message;

    } catch (e) {

        error.style.color = "#C43E3E";
        error.textContent = "Unable to connect to the server. Please try again.";

    } finally {

        button.disabled = false;

    }
}


/* =====================================
   LOGIN FORM
===================================== */

const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const orgCode = loginForm["organizationCode"].value.trim();
        const email = loginForm["email"].value.trim();
        const password = loginForm["password"].value;

        const error = document.getElementById("loginError");

        error.style.color = "#C43E3E";
        error.textContent = "";

        if (!orgCode || !email || !password) {
            error.textContent = "Please enter all login details.";
            return;
        }

        if (!/^FB[A-Z0-9]{6}$/i.test(orgCode)) {
            error.textContent = "Please enter a valid organization code.";
            return;
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            error.textContent = "Please enter a valid email address.";
            return;
        }

        error.style.color = "#159B91";
        error.textContent = "Signing in...";

        loginUser();

    });

}


/* =====================================
   LOGIN BACKEND CONNECTION
===================================== */

async function loginUser() {

    const form = document.getElementById("loginForm");
    const error = document.getElementById("loginError");
    const button = form.querySelector('button[type="submit"]');

    button.disabled = true;

    try {

        const formData = new URLSearchParams();

        formData.append("organizationCode", form["organizationCode"].value.trim());
        formData.append("email", form["email"].value.trim());
        formData.append("password", form["password"].value);

        const response = await fetch(
            "http://localhost:8080/FindBack/LoginServlet",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData
            }
        );

        const result = await response.json();

        if (result.success) {

            // Store the session token for subsequent API requests
            sessionStorage.setItem("findbackToken", result.token);
            sessionStorage.setItem("findbackRole", result.role);
            sessionStorage.setItem("findbackName", result.fullName);

            sessionStorage.setItem("fbWelcomeTitle", "Welcome back!");
            sessionStorage.setItem("fbWelcomeMessage", "You're signed in successfully. Your FindBack workspace is ready.");
            sessionStorage.setItem("fbWelcomeNext", result.dashboard);
            window.location.href = "Welcome.html";

        } else {

            error.style.color = "#C43E3E";
            error.textContent = result.message;

        }

    } catch (e) {

        console.error("Login error:", e);

        error.style.color = "#C43E3E";
        error.textContent =
            "Unable to connect to the server. Please try again.";

    } finally {

        button.disabled = false;

    }
}
const toggleLoginPassword = document.getElementById("toggleLoginPassword");

if (toggleLoginPassword) {

    toggleLoginPassword.addEventListener("click", function() {

        const passwordField = document.getElementById("loginPassword");

        if (passwordField.type === "password") {
            passwordField.type = "text";
            toggleLoginPassword.textContent = "Hide";
        } else {
            passwordField.type = "password";
            toggleLoginPassword.textContent = "Show";
        }

    });

}
