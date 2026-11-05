/* =====================================
   REPORT LOST / FOUND ITEM
===================================== */


async function submitItemReport(form, itemType, messageId) {

    const message = document.getElementById(messageId);
    const button = form.querySelector('button[type="submit"]');

    // Save values before resetting the form
    const itemName = form["itemName"].value.trim();
    const category = form["category"].value;
    const description = form["description"].value.trim();
    const location = form["location"].value.trim();
    const itemDate = form["itemDate"].value;

    button.disabled = true;
    message.style.color = "#159B91";
    message.textContent = "Submitting your report...";

    try {

        const formData = new URLSearchParams();

        formData.append("itemName", itemName);
        formData.append("category", category);
        formData.append("description", description);
        formData.append("location", location);
        formData.append("itemDate", itemDate);
        formData.append("itemType", itemType);

        const token = sessionStorage.getItem("findbackToken");

        const response = await fetch(
            "http://localhost:8080/FindBack/ItemServlet",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Authorization": "Bearer " + token
                },
                body: formData
            }
        );

        const result = await response.json();

        message.style.color = result.success ? "#159B91" : "#C43E3E";
        message.textContent = result.message;

        if (result.success) {

            form.reset();

            // Find matching items only for LOST reports
            if (itemType === "LOST") {
                await findMatchingItems(
                    itemName,
                    category,
                    location,
                    itemDate
                );
            }
        }

    } catch (error) {

        console.error(error);
        message.style.color = "#C43E3E";
        message.textContent = "Unable to connect to the server.";

    } finally {

        button.disabled = false;

    }
}


// SMART ITEM MATCHING

async function findMatchingItems(itemName, category, location, itemDate) {

    const results = document.getElementById("matchingResults");
    const list = document.getElementById("matchingItemsList");

    if (!results || !list) return;

    results.style.display = "block";
    list.textContent = "Searching for possible matches...";

    try {

        const params = new URLSearchParams({
            itemName: itemName,
            category: category,
            location: location,
            itemDate: itemDate
        });

        const response = await fetch(
            "http://localhost:8080/FindBack/MatchingServlet?" + params,
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
            list.textContent = result.message;
            return;
        }

        list.innerHTML = "";

        if (!result.items || result.items.length === 0) {
            list.textContent =
                "No matching found items are available right now.";
            return;
        }

        result.items.forEach(item => {

            const card = document.createElement("div");
            card.className = "item-result-card";

            const heading = document.createElement("h3");
            heading.textContent = item.itemName;

            const categoryText = document.createElement("p");
            categoryText.textContent = "Category: " + item.category;

            const locationText = document.createElement("p");
            locationText.textContent = "Location: " + item.location;

            const dateText = document.createElement("p");
            dateText.textContent = "Date: " + item.itemDate;

            const score = document.createElement("p");
            score.textContent = "Match Score: " + item.matchScore + "%";

            card.append(
                heading,
                categoryText,
                locationText,
                dateText,
                score
            );

            list.appendChild(card);
        });

    } catch (error) {

        console.error("Matching error:", error);

        list.textContent =
            "Your report was saved, but matching results could not be loaded.";

    }
}



/* LOST ITEM FORM */

const lostForm = document.forms["lostItemForm"];

if (lostForm) {
    lostForm.addEventListener("submit", function(event) {
        event.preventDefault();
        submitItemReport(lostForm, "LOST", "lostError");
    });
}


/* FOUND ITEM FORM */

const foundForm = document.forms["foundItemForm"];

if (foundForm) {
    foundForm.addEventListener("submit", function(event) {
        event.preventDefault();
        submitItemReport(foundForm, "FOUND", "foundError");
    });
}


/* SEARCH ITEMS */

const searchItemForm = document.forms["searchItemForm"];

if (searchItemForm) {

    const searchResults = document.getElementById("searchResults");

    const resultCount = document.getElementById("resultCount");

    const noResults = document.getElementById("noResults");

    const searchCards = document.querySelectorAll(".item-result-card");


    searchItemForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const name = searchItemForm["itemName"].value.trim().toLowerCase();

        const category = searchItemForm["category"].value;

        const location = searchItemForm["location"].value.trim().toLowerCase();

        let count = 0;

        searchCards.forEach(function(card) {

            const itemName = card.dataset.name.toLowerCase();

            const itemCategory = card.dataset.category;

            const itemLocation = card.dataset.location.toLowerCase();

            const nameMatch = itemName.includes(name);

            const categoryMatch =
                category === "" || itemCategory === category;

            const locationMatch =
                location === "" || itemLocation.includes(location);

            if (nameMatch && categoryMatch && locationMatch) {

                card.style.display = "block";
                count++;

            } else {

                card.style.display = "none";

            }

        });


        resultCount.textContent = "Showing " + count + " matching item(s)";

        if (count === 0) {

            searchResults.style.display = "none";
            noResults.style.display = "block";

        } else {

            searchResults.style.display = "grid";
            noResults.style.display = "none";

        }

    });


    // CLEAR FILTERS

    searchItemForm.addEventListener("reset", function() {

        setTimeout(function() {

            searchCards.forEach(function(card) {
                card.style.display = "block";
            });

            searchResults.style.display = "grid";
            noResults.style.display = "none";

            resultCount.textContent = "Showing demo results";

        }, 0);

    });


    // VIEW ITEM DETAILS

    const viewButtons = document.querySelectorAll(".view-item-btn");

    const modal = document.getElementById("itemModal");

    const closeModal = document.getElementById("closeModal");

    let selectedItem = "";


    viewButtons.forEach(function(button) {

        button.addEventListener("click", function() {

            selectedItem = this.dataset.item;

            const card = this.closest(".item-result-card");

            document.getElementById("modalItemName").textContent =
                selectedItem;

            document.getElementById("modalItemDescription").textContent =
                card.querySelector(".item-description").textContent;

            modal.classList.add("show");

        });

    });


    closeModal.addEventListener("click", function() {
        modal.classList.remove("show");
    });


    modal.addEventListener("click", function(event) {

        if (event.target === modal) {
            modal.classList.remove("show");
        }

    });


    // CLAIM BUTTON - TEMPORARY

    const claimButton = document.getElementById("claimItemBtn");

if (claimButton) {
    claimButton.addEventListener("click", async function () {

        if (!selectedSearchItem) {
            alert("Please select an item first.");
            return;
        }

        const details = document.getElementById("claimDetails").value.trim();

        if (!details) {
            alert("Please provide claim details.");
            return;
        }

        claimButton.disabled = true;
        claimButton.textContent = "Submitting...";

        try {
            const response = await fetch(
                "http://localhost:8080/FindBack/ClaimServlet",
                {
                    method: "POST",
                    headers: {
                        "Authorization": "Bearer " + sessionStorage.getItem("findbackToken"),
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: new URLSearchParams({
                        itemId: selectedSearchItem.itemId,
                        claimDetails: details
                    })
                }
            );

            const result = await response.json();

            alert(result.message);

            if (result.success) {
                document.getElementById("itemModal").classList.remove("show");
                document.getElementById("claimDetails").value = "";
                loadFoundItems();
            }

        } catch (error) {
            console.error("Claim error:", error);
            alert("Unable to submit your claim. Please try again.");
        } finally {
            claimButton.disabled = false;
            claimButton.textContent = "Continue to Claim →";
        }
    });
}

}


// MY ACTIVITY PAGE - FILTERS

const activityFilters = document.querySelectorAll(".activity-filter");

if (activityFilters.length > 0) {

    const activityRows = document.querySelectorAll(
        "#activityTableBody tr"
    );

    const emptyMessage = document.getElementById(
        "activityEmptyMessage"
    );

    activityFilters.forEach(function(button) {

        button.addEventListener("click", function() {

            // Remove active class from all buttons
            activityFilters.forEach(function(btn) {
                btn.classList.remove("active");
            });

            // Highlight selected button
            button.classList.add("active");

            const selectedFilter = button.getAttribute("data-filter");
            let visibleCount = 0;

            activityRows.forEach(function(row) {

                const activityType = row.getAttribute("data-type");

                if (selectedFilter === "all" ||
                    selectedFilter === activityType) {

                    row.style.display = "";
                    visibleCount++;

                } else {

                    row.style.display = "none";

                }

            });

            // Show message if no records are available
            if (visibleCount === 0) {
                emptyMessage.style.display = "block";
            } else {
                emptyMessage.style.display = "none";
            }

        });

    });

}

/* =====================================
   LOAD REAL VERIFIED FOUND ITEMS
===================================== */

let availableFoundItems = [];
let selectedSearchItem = null;

async function loadFoundItems() {

    const container = document.getElementById("searchResults");
    const count = document.getElementById("resultCount");

    if (!container) return;

    container.innerHTML = "<p>Loading available items...</p>";

    try {

        const response = await fetch(
            "http://localhost:8080/FindBack/SearchItemsServlet",
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
            container.textContent = result.message;
            return;
        }

        availableFoundItems = result.items;

        if (count) {
            count.textContent =
                "Showing " + availableFoundItems.length + " verified items";
        }

        displayFoundItems(availableFoundItems);

    } catch (error) {

        console.error(error);
        container.textContent = "Unable to load found items.";

    }
}


/* DISPLAY ITEM CARDS */

function displayFoundItems(items) {

    const container = document.getElementById("searchResults");
    const noResults = document.getElementById("noResults");

    container.innerHTML = "";

    if (items.length === 0) {
        noResults.style.display = "block";
        return;
    }

    noResults.style.display = "none";

    items.forEach(item => {

        const card = document.createElement("article");
        card.className = "item-result-card";

        const visual = document.createElement("div");
        visual.className = "item-card-visual bag-visual";

        const icon = document.createElement("span");
        icon.className = "item-visual-icon";
        icon.textContent = "▤";

        const badge = document.createElement("span");
        badge.className = "available-badge";
        badge.textContent = "Available";

        visual.append(icon, badge);

        const content = document.createElement("div");
        content.className = "item-card-content";

        const category = document.createElement("div");
        category.className = "item-card-category";
        category.textContent = item.category;

        const heading = document.createElement("h3");
        heading.textContent = item.itemName;

        const description = document.createElement("p");
        description.className = "item-description";
        description.textContent = item.description || "No description provided.";

        const meta = document.createElement("div");
        meta.className = "item-card-meta";

        const location = document.createElement("span");
        location.textContent = "⌖ " + item.location;

        const verified = document.createElement("span");
        verified.textContent = "✓ Verified";

        meta.append(location, verified);

        const button = document.createElement("button");
        button.className = "view-item-btn";
        button.type = "button";
        button.textContent = "View Details →";

        button.addEventListener("click", function() {
            openFoundItemDetails(item);
        });

        content.append(category, heading, description, meta, button);
        card.append(visual, content);

        container.appendChild(card);

    });
}


/* SEARCH FILTERS */

const searchForm = document.forms["searchItemForm"];

if (searchForm) {

    searchForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const name = searchForm["itemName"].value.trim().toLowerCase();
        const category = searchForm["category"].value;
        const location = searchForm["location"].value.trim().toLowerCase();

        const filtered = availableFoundItems.filter(item => {

            return item.itemName.toLowerCase().includes(name) &&
                   (!category || item.category === category) &&
                   item.location.toLowerCase().includes(location);

        });

        document.getElementById("resultCount").textContent =
            "Showing " + filtered.length + " matching items";

        displayFoundItems(filtered);

    });

    searchForm.addEventListener("reset", function() {

        setTimeout(function() {
            document.getElementById("resultCount").textContent =
                "Showing " + availableFoundItems.length + " verified items";

            displayFoundItems(availableFoundItems);
        }, 0);

    });
}


/* ITEM DETAILS MODAL */

function openFoundItemDetails(item) {

    selectedSearchItem = item;

    document.getElementById("modalItemName").textContent =
        item.itemName;

    document.getElementById("modalItemDescription").textContent =
        item.description || "No description available.";

    document.getElementById("itemModal").classList.add("show");
}

const closeModalButton = document.getElementById("closeModal");

if (closeModalButton) {
    closeModalButton.addEventListener("click", function() {
        document.getElementById("itemModal").classList.remove("show");
    });
}

const claimButton = document.getElementById("claimItemBtn");

if (claimButton) {
    claimButton.addEventListener("click", function() {
        alert("Claim submission will be connected in the next step.");
    });
}


/* LOAD ITEMS WHEN PAGE OPENS */

if (document.getElementById("searchResults")) {
    loadFoundItems();
}


let myActivityItems = [];
let myActivityClaims = [];

async function loadMyActivity() {

    const tbody = document.getElementById("activityTableBody");
    if (!tbody) return;

    tbody.innerHTML =
        "<tr><td colspan='4'>Loading your activity...</td></tr>";

    try {
        const response = await fetch(
            "http://localhost:8080/FindBack/MyActivityServlet",
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
                "<tr><td colspan='4'>Unable to load activity.</td></tr>";
            return;
        }

        myActivityItems = result.items || [];
        myActivityClaims = result.claims || [];

        displayMyActivity("ALL");
        updateActivityStats();

    } catch (error) {
        console.error("My Activity error:", error);
        tbody.innerHTML =
            "<tr><td colspan='4'>Server connection failed.</td></tr>";
    }
}

function displayMyActivity(filter) {

    const tbody = document.getElementById("activityTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    let activities = [];

    myActivityItems.forEach(item => {
        activities.push({
            id: item.itemId,
            name: item.itemName,
            type: item.itemType,
            status: item.status,
            date: item.itemDate
        });
    });

    myActivityClaims.forEach(claim => {
        activities.push({
            id: claim.claimId,
            name: "Claim for Item #" + claim.itemId,
            type: "CLAIM",
            status: claim.status,
            date: "-"
        });
    });

    if (filter !== "ALL") {
        activities = activities.filter(item => item.type === filter);
    }

    if (activities.length === 0) {
        tbody.innerHTML =
            "<tr><td colspan='4' class='activity-empty'>No activities found.</td></tr>";
        return;
    }

    activities.forEach(activity => {

        const row = document.createElement("tr");

        [
            activity.name,
            activity.type,
            activity.status,
            activity.date
        ].forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });
}

function updateActivityStats() {

    const stats = document.querySelectorAll(".activity-stat-card h2");

    if (stats.length >= 4) {
        stats[0].textContent = myActivityItems.length;
        stats[1].textContent =
            myActivityItems.filter(item => item.itemType === "LOST").length;
        stats[2].textContent =
            myActivityItems.filter(item => item.itemType === "FOUND").length;
        stats[3].textContent = myActivityClaims.length;
    }
}

// Connect existing activity filter buttons.
document.querySelectorAll(".activity-filter").forEach(button => {

    button.addEventListener("click", function () {

        document.querySelectorAll(".activity-filter")
            .forEach(btn => btn.classList.remove("active"));

        this.classList.add("active");

        const filter = this.textContent.trim().toUpperCase();

        displayMyActivity(
            filter.includes("LOST") ? "LOST" :
            filter.includes("FOUND") ? "FOUND" :
            filter.includes("CLAIM") ? "CLAIM" : "ALL"
        );
    });
});

if (document.getElementById("activityTableBody")) {
    loadMyActivity();
}