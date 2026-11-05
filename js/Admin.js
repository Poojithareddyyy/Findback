
// ADMIN MANAGEMENT PAGE - TAB SWITCHING

const managementTabs = document.querySelectorAll(".management-tab");

if (managementTabs.length > 0) {

    const managementSections = document.querySelectorAll(
        ".management-section"
    );

    managementTabs.forEach(function(tab) {

        tab.addEventListener("click", function() {

            const tabName = tab.getAttribute("data-section");

            // Remove active class from all tabs
            managementTabs.forEach(function(button) {
                button.classList.remove("active");
            });

            // Hide all sections
            managementSections.forEach(function(section) {
                section.classList.remove("active");
            });

            // Activate selected tab
            tab.classList.add("active");

            const sectionName = tab.getAttribute("data-section");

            const selectedSection = document.getElementById(
                sectionName + "Section"
            );

            if (selectedSection) {
                selectedSection.classList.add("active");
            }

            if (tabName === "claims") loadPendingClaims();
            if (tabName === "returns") loadCompletedReturns();
            if (tabName === "analytics") loadAdminAnalytics();
            if (tabName === "items") loadAdminItems();

        });

    });

}
/* =====================================
   LOAD ADMIN ITEM REPORTS
===================================== */

async function loadAdminItems() {

    const table = document.getElementById("adminItemsTable");

    if (!table) return;

    try {

        const response = await fetch(
            "http://localhost:8080/FindBack/AdminItemsServlet",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " +
                        sessionStorage.getItem("findbackToken")
                }
            }
        );

        const result = await response.json();

        if (!result.success) {
            table.innerHTML =
                `<tr><td colspan="6">${result.message}</td></tr>`;
            return;
        }

        if (result.items.length === 0) {
            table.innerHTML =
                `<tr><td colspan="6">No item reports found.</td></tr>`;
            return;
        }

        table.innerHTML = "";

        result.items.forEach(item => {

            const row = document.createElement("tr");

            const values = [
                item.itemName,
                item.itemType,
                "User ID: " + item.reportedBy,
                item.itemDate,
                item.status.replaceAll("_", " ")
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

        const actionCell = document.createElement("td");

        if (item.itemType === "FOUND" &&
            item.status === "FOUND_REPORTED") {

        const verifyButton = document.createElement("button");

            verifyButton.textContent = "Verify";
            verifyButton.className = "management-action";

            verifyButton.addEventListener("click", function() {
            verifyItem(item.itemId, verifyButton);
        });

        actionCell.appendChild(verifyButton);

        } else {
            actionCell.textContent = "—";
        }

        row.appendChild(actionCell);

            table.appendChild(row);

        });

    } catch (error) {

        console.error("Admin items error:", error);

        table.innerHTML =
            `<tr><td colspan="6">Unable to load item reports.</td></tr>`;
    }
}

loadAdminItems();

async function verifyItem(itemId, button) {

    if (!confirm("Are you sure you want to verify this found item?")) {
        return;
    }

    button.disabled = true;
    button.textContent = "Verifying...";

    try {

        const formData = new URLSearchParams();
        formData.append("itemId", itemId);

        const response = await fetch(
            "http://localhost:8080/FindBack/VerifyItemServlet",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Authorization": "Bearer " +
                        sessionStorage.getItem("findbackToken")
                },
                body: formData
            }
        );

        const result = await response.json();

        alert(result.message);

        if (result.success) {
            loadAdminItems();
        } else {
            button.disabled = false;
            button.textContent = "Verify";
        }

    } catch (error) {

        console.error(error);
        alert("Unable to verify item.");

        button.disabled = false;
        button.textContent = "Verify";
    }
}

async function loadPendingClaims() {

    const tbody = document.getElementById("claimsTableBody");
    if (!tbody) return;

    tbody.innerHTML = "<tr><td colspan='6'>Loading claims...</td></tr>";

    try {
        const response = await fetch(
            "http://localhost:8080/FindBack/AdminClaimsServlet",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + sessionStorage.getItem("findbackToken")
                }
            }
        );

        const result = await response.json();

        if (!result.success) {
            tbody.innerHTML =
                "<tr><td colspan='6'>Unable to load claims.</td></tr>";
            return;
        }

        tbody.innerHTML = "";

        if (result.claims.length === 0) {
            tbody.innerHTML =
                "<tr><td colspan='6'>No active claims found.</td></tr>";
            return;
        }

        result.claims.forEach(claim => {

            const row = document.createElement("tr");

            const values = [
                claim.claimId,
                claim.itemId,
                claim.userId,
                claim.claimDetails,
                claim.status.replaceAll("_", " ")
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            const actionCell = document.createElement("td");

            if (claim.status === "PENDING") {
                const approveBtn = document.createElement("button");
                approveBtn.textContent = "Approve";
                approveBtn.type = "button";
                approveBtn.className = "management-action";
                approveBtn.addEventListener("click", () => updateClaimStatus(claim.claimId, "APPROVED"));

                const rejectBtn = document.createElement("button");
                rejectBtn.textContent = "Reject";
                rejectBtn.type = "button";
                rejectBtn.className = "management-action management-action-danger";
                rejectBtn.addEventListener("click", () => updateClaimStatus(claim.claimId, "REJECTED"));
                actionCell.append(approveBtn, rejectBtn);
            } else if (claim.status === "APPROVED") {
                const handoverBtn = document.createElement("button");
                handoverBtn.textContent = "Confirm Handover";
                handoverBtn.type = "button";
                handoverBtn.className = "management-action";
                handoverBtn.addEventListener("click", () => confirmPhysicalHandover(claim.claimId, handoverBtn));
                actionCell.appendChild(handoverBtn);
            } else {
                actionCell.textContent = "—";
            }
            row.appendChild(actionCell);

            tbody.appendChild(row);
        });

    } catch (error) {
        console.error("Claims loading error:", error);
        tbody.innerHTML =
            "<tr><td colspan='6'>Server connection failed.</td></tr>";
    }
}


async function updateClaimStatus(claimId, status) {

    if (!confirm("Are you sure you want to " + status.toLowerCase() + " this claim?")) {
        return;
    }

    try {
        const response = await fetch(
            "http://localhost:8080/FindBack/UpdateClaimServlet",
            {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + sessionStorage.getItem("findbackToken"),
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    claimId: claimId,
                    status: status
                })
            }
        );

        const result = await response.json();

        alert(result.message);

        if (result.success) {
            loadPendingClaims();
        }

    } catch (error) {
        console.error("Claim update error:", error);
        alert("Unable to update claim.");
    }
}


// Load claims when the admin management page opens.
if (document.getElementById("claimsTableBody")) {
    loadPendingClaims();
}

async function confirmPhysicalHandover(claimId, button) {
    if (!confirm("Confirm that the approved item has been physically handed over to the member?")) return;
    button.disabled = true;
    button.textContent = "Processing...";
    try {
        const response = await fetch("http://localhost:8080/FindBack/HandoverServlet", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + sessionStorage.getItem("findbackToken"),
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({ claimId: claimId })
        });
        const result = await response.json();
        alert(result.message || (result.success ? "Handover recorded." : "Unable to record handover."));
        if (result.success) {
            loadPendingClaims();
            loadCompletedReturns();
        } else {
            button.disabled = false;
            button.textContent = "Confirm Handover";
        }
    } catch (error) {
        console.error("Handover error:", error);
        alert("Unable to connect to the server.");
        button.disabled = false;
        button.textContent = "Confirm Handover";
    }
}

async function loadCompletedReturns() {

    const tbody = document.getElementById("returnsTableBody");

    if (!tbody) return;

    tbody.innerHTML = "<tr><td colspan='6'>Loading...</td></tr>";

    try {
        const response = await fetch(
            "http://localhost:8080/FindBack/AdminReturnsServlet",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " +
                        sessionStorage.getItem("findbackToken")
                }
            }
        );

        const result = await response.json();

        if (!result.success) {
            tbody.innerHTML =
                "<tr><td colspan='6'>Unable to load returns.</td></tr>";
            return;
        }

        tbody.innerHTML = "";

        if (result.returns.length === 0) {
            tbody.innerHTML =
                "<tr><td colspan='6'>No completed returns yet.</td></tr>";
            return;
        }

        result.returns.forEach(item => {

            const row = document.createElement("tr");

            [
                item.returnId,
                item.claimId,
                item.itemId,
                item.userId,
                item.verifiedBy,
                item.returnDate
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            tbody.appendChild(row);
        });

    } catch (error) {
        console.error("Returns loading error:", error);

        tbody.innerHTML =
            "<tr><td colspan='6'>Server connection failed.</td></tr>";
    }
}


async function loadAdminDashboard() {

    try {
        const response = await fetch(
            "http://localhost:8080/FindBack/AdminDashboardServlet",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + sessionStorage.getItem("findbackToken")
                }
            }
        );

        const result = await response.json();

        if (!result.success) {
            alert(result.message || "Unable to load dashboard.");
            return;
        }

        document.getElementById("totalItemsCount").textContent = result.totalItems;
        document.getElementById("lostItemsCount").textContent = result.lostItems;
        document.getElementById("foundReportedCount").textContent = result.foundReported;
        document.getElementById("availableItemsCount").textContent = result.availableItems;
        document.getElementById("pendingClaimsCount").textContent = result.pendingClaims;
        document.getElementById("returnedItemsCount").textContent = result.returnedItems;

    } catch (error) {
        console.error("Dashboard loading error:", error);
        alert("Unable to connect to the dashboard.");
    }
}


async function loadAdminAnalytics() {

    const token = sessionStorage.getItem("findbackToken");

    try {

        const response = await fetch(
            "http://localhost:8080/FindBack/AdminDashboardServlet",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        const result = await response.json();

        if (!result.success) {
            console.log("Unable to load analytics");
            return;
        }

        document.getElementById("analyticsTotalReports").textContent =
            result.totalReports;

        document.getElementById("analyticsFoundItems").textContent =
            result.foundItems;

        document.getElementById("analyticsTotalClaims").textContent =
            result.totalClaims;

        document.getElementById("analyticsReturnedItems").textContent =
            result.returnedItems;

    } catch (error) {
        console.error("Analytics loading error:", error);
    }
}

async function loadAdminRecentActivity() {

    const token = sessionStorage.getItem("findbackToken");

    try {

        // Load actual item reports
        const itemResponse = await fetch(
            "http://localhost:8080/FindBack/AdminItemsServlet",
            {
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        const itemResult = await itemResponse.json();

        const itemContainer =
            document.getElementById("recentItemsList");

        if (itemResult.success && itemResult.items.length > 0) {

            itemContainer.innerHTML = itemResult.items
                .slice(0, 3)
                .map(item => `
                    <div class="admin-recent-row">
                        <div>
                            <strong>${escapeDashboardText(item.itemName)}</strong>
                            <span>${escapeDashboardText(item.itemType)} Report · User ID: ${item.reportedBy}</span>
                        </div>
                        <span class="admin-mini-status">
                            ${escapeDashboardText(item.status)}
                        </span>
                    </div>
                `).join("");

        } else {
            itemContainer.innerHTML =
                "<p>No item reports yet.</p>";
        }

        // Load actual claims
        const claimResponse = await fetch(
            "http://localhost:8080/FindBack/AdminClaimsServlet",
            {
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        const claimResult = await claimResponse.json();

        const claimContainer =
            document.getElementById("recentClaimsList");

        if (claimResult.success && claimResult.claims.length > 0) {

            claimContainer.innerHTML = claimResult.claims
                .slice(0, 3)
                .map(claim => `
                    <div class="admin-recent-row">
                        <div>
                            <strong>Claim #${claim.claimId}</strong>
                            <span>Item #${claim.itemId} · User ID: ${claim.userId}</span>
                        </div>
                        <span class="admin-mini-status">
                            ${escapeDashboardText(claim.status)}
                        </span>
                    </div>
                `).join("");

        } else {
            claimContainer.innerHTML =
                "<p>No claim requests yet.</p>";
        }

    } catch (error) {
        console.error("Recent activity error:", error);
        document.getElementById("recentItemsList").textContent =
            "Unable to load item reports.";
        document.getElementById("recentClaimsList").textContent =
            "Unable to load claims.";
    }
}

function escapeDashboardText(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}