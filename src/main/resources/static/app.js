"use strict";

/* ============================================================
   CampusFind front end - plain JavaScript talking to the
   Spring Boot REST API. The logged-in user's id is sent in the
   X-User-Id header on every request.
   ============================================================ */

const API = location.protocol === "file:" ? "http://localhost:8080/api" : "/api";
const state = { user: null, categories: [], view: null, filters: { lost: {}, found: {} } };

const $ = (sel, root = document) => root.querySelector(sel);
const content = () => $("#content");

/* ---------- helpers ---------- */
function esc(v) {
    return String(v ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function today() {
    return new Date().toLocaleDateString("en-CA"); // yyyy-mm-dd
}

let toastTimer;
function toast(message, type = "ok") {
    const t = $("#toast");
    t.textContent = message;
    t.className = "toast show " + (type === "error" ? "error" : "");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { t.className = "toast"; }, 4200);
}

async function api(path, { method = "GET", body } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (state.user) headers["X-User-Id"] = state.user.id;

    const res = await fetch(API + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
    });

    let data = null;
    try { data = await res.json(); } catch (e) { /* empty body */ }

    if (!res.ok) {
        if (res.status === 401 && state.user) { logout(); }
        let msg = (data && data.message) || ("Request failed (" + res.status + ")");
        if (data && data.fieldErrors) {
            msg += ": " + Object.entries(data.fieldErrors).map(([k, v]) => v).join("; ");
        }
        throw new Error(msg);
    }
    return data;
}

function tag(status) {
    return `<span class="tag tag-${esc(status.toLowerCase())}">${esc(status)}</span>`;
}

function catOptions(selected) {
    return state.categories
        .map(c => `<option value="${c.id}" ${String(selected) === String(c.id) ? "selected" : ""}>${esc(c.name)}</option>`)
        .join("");
}

function formToObject(form) {
    const obj = Object.fromEntries(new FormData(form));
    if (obj.categoryId) obj.categoryId = Number(obj.categoryId);
    return obj;
}

function toQuery(filters) {
    const p = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => { if (v) p.set(k, v); });
    const s = p.toString();
    return s ? "?" + s : "";
}

function isStaffOrAdmin() {
    return state.user.role === "STAFF" || state.user.role === "ADMIN";
}

/* ---------- auth screen ---------- */
document.querySelectorAll(".tab").forEach(btn => {
    btn.addEventListener("click", () => switchTab(btn.dataset.tab));
});

function switchTab(name) {
    document.querySelectorAll(".tab").forEach(b => b.classList.toggle("active", b.dataset.tab === name));
    $("#loginForm").hidden = name !== "login";
    $("#registerForm").hidden = name !== "register";
}

$("#roleSelect").addEventListener("change", e => {
    $("#adminCodeRow").hidden = e.target.value !== "ADMIN";
});

$("#loginForm").addEventListener("submit", async e => {
    e.preventDefault();
    try {
        const user = await api("/auth/login", { method: "POST", body: formToObject(e.target) });
        await enter(user);
    } catch (err) {
        toast(err.message, "error");
    }
});

$("#registerForm").addEventListener("submit", async e => {
    e.preventDefault();
    const form = e.target;
    try {
        const body = formToObject(form);
        await api("/auth/register", { method: "POST", body });
        toast("Account created. You can log in now.");
        $("#loginForm").email.value = body.email;
        form.reset();
        $("#adminCodeRow").hidden = true;
        switchTab("login");
    } catch (err) {
        toast(err.message, "error");
    }
});

$("#logoutBtn").addEventListener("click", logout);

/* ---------- session ---------- */
async function enter(user) {
    state.user = user;
    localStorage.setItem("cf_user", JSON.stringify(user));
    state.categories = await api("/categories");
    $("#whoami").textContent = `${user.name} (${user.role.toLowerCase()})`;
    $("#authView").hidden = true;
    $("#appView").hidden = false;
    go(user.role === "ADMIN" ? "dashboard" : "lost");
}

function logout() {
    state.user = null;
    localStorage.removeItem("cf_user");
    $("#appView").hidden = true;
    $("#authView").hidden = false;
}

/* ---------- navigation ---------- */
function tabsFor(role) {
    const tabs = [];
    if (role === "ADMIN") tabs.push(["dashboard", "Dashboard"]);
    tabs.push(["report", "Report lost item"]);
    if (role !== "STUDENT") tabs.push(["log", "Log found item"]);
    tabs.push(["lost", role === "STUDENT" ? "My lost reports" : "Lost reports"]);
    tabs.push(["found", "Found items"]);
    tabs.push(["matches", "Matches"]);
    return tabs;
}

function renderNav() {
    $("#nav").innerHTML = tabsFor(state.user.role)
        .map(([id, label]) => `<button data-view="${id}" class="${state.view === id ? "active" : ""}">${label}</button>`)
        .join("");
}

$("#nav").addEventListener("click", e => {
    const b = e.target.closest("button[data-view]");
    if (b) go(b.dataset.view);
});

const VIEWS = {
    dashboard: viewDashboard,
    report: viewReport,
    log: viewLog,
    lost: viewLost,
    found: viewFound,
    matches: viewMatches
};

async function go(view) {
    state.view = view;
    renderNav();
    content().innerHTML = `<p class="muted">Loading...</p>`;
    try {
        await VIEWS[view]();
    } catch (err) {
        content().innerHTML = `<div class="notice error">${esc(err.message)}</div>`;
    }
}

/* ---------- views ---------- */
async function viewReport() {
    content().innerHTML = `
        <h2>Report a lost item</h2>
        <form id="lostForm" class="panel form">
            <label>Category
                <select name="categoryId" required>
                    <option value="" disabled selected>Choose a category</option>${catOptions()}
                </select>
            </label>
            <label>Description
                <textarea name="description" rows="3" maxlength="500" required
                    placeholder="Black Casio calculator with a blue sticker on the back"></textarea>
            </label>
            <label>Where did you lose it?
                <input name="location" maxlength="255" required placeholder="Library, second floor">
            </label>
            <label>Date lost
                <input type="date" name="dateLost" max="${today()}" value="${today()}" required>
            </label>
            <button class="btn primary">Submit lost report</button>
        </form>`;
    $("#lostForm").addEventListener("submit", async e => {
        e.preventDefault();
        try {
            await api("/lost", { method: "POST", body: formToObject(e.target) });
            toast("Lost report submitted");
            go("lost");
        } catch (err) { toast(err.message, "error"); }
    });
}

async function viewLog() {
    content().innerHTML = `
        <h2>Log a found item</h2>
        <form id="foundForm" class="panel form">
            <label>Category
                <select name="categoryId" required>
                    <option value="" disabled selected>Choose a category</option>${catOptions()}
                </select>
            </label>
            <label>Description
                <textarea name="description" rows="3" maxlength="500" required
                    placeholder="Black calculator, blue sticker, name scratched off"></textarea>
            </label>
            <label>Where was it found?
                <input name="location" maxlength="255" required placeholder="Library reading room">
            </label>
            <label>Date found
                <input type="date" name="dateFound" max="${today()}" value="${today()}" required>
            </label>
            <button class="btn primary">Save found item</button>
        </form>`;
    $("#foundForm").addEventListener("submit", async e => {
        e.preventDefault();
        try {
            await api("/found", { method: "POST", body: formToObject(e.target) });
            toast("Found item logged as AVAILABLE");
            go("found");
        } catch (err) { toast(err.message, "error"); }
    });
}

function filterBar(kind, statuses) {
    const f = state.filters[kind];
    return `
        <div class="filters">
            <label>Category
                <select id="f-cat"><option value="">All</option>${catOptions(f.categoryId)}</select>
            </label>
            <label>Status
                <select id="f-status"><option value="">All</option>
                    ${statuses.map(s => `<option ${f.status === s ? "selected" : ""}>${s}</option>`).join("")}
                </select>
            </label>
            <label>Date
                <input type="date" id="f-date" value="${esc(f.date || "")}">
            </label>
            <button class="btn" id="f-apply">Apply filters</button>
            <button class="btn ghost" id="f-clear">Clear</button>
        </div>`;
}

function bindFilters(kind, reload) {
    $("#f-apply").onclick = () => {
        state.filters[kind] = { categoryId: $("#f-cat").value, status: $("#f-status").value, date: $("#f-date").value };
        reload();
    };
    $("#f-clear").onclick = () => { state.filters[kind] = {}; reload(); };
}

async function viewLost() {
    const list = await api("/lost" + toQuery(state.filters.lost));
    const canDelete = l => l.status === "OPEN" && (state.user.role === "ADMIN" || l.reportedBy.id === state.user.id);

    const rows = list.map(l => `
        <tr>
            <td>#${l.id}</td>
            <td>${esc(l.category.name)}</td>
            <td>${esc(l.description)}</td>
            <td>${esc(l.location)}</td>
            <td>${esc(l.dateLost)}</td>
            <td>${tag(l.status)}</td>
            <td>${esc(l.reportedBy.name)}</td>
            <td>${canDelete(l) ? `<button class="btn small danger" data-act="del-lost" data-id="${l.id}">Delete</button>` : ""}</td>
        </tr>`).join("");

    content().innerHTML = `
        <h2>${state.user.role === "STUDENT" ? "My lost reports" : "Lost reports"}</h2>
        ${filterBar("lost", ["OPEN", "MATCHED", "RETURNED"])}
        ${list.length ? `<div class="table-wrap"><table class="data">
            <thead><tr><th>ID</th><th>Category</th><th>Description</th><th>Location</th><th>Date lost</th><th>Status</th><th>Reported by</th><th></th></tr></thead>
            <tbody>${rows}</tbody></table></div>`
            : `<div class="empty">No lost reports found. Use "Report lost item" to add one.</div>`}`;
    bindFilters("lost", viewLost);
}

async function viewFound() {
    const list = await api("/found" + toQuery(state.filters.found));
    const manage = isStaffOrAdmin();
    const canDelete = f => f.status === "AVAILABLE" &&
        (state.user.role === "ADMIN" || (state.user.role === "STAFF" && f.reportedBy.id === state.user.id));

    const rows = list.map(f => `
        <tr>
            <td>#${f.id}</td>
            <td>${esc(f.category.name)}</td>
            <td>${esc(f.description)}</td>
            <td>${esc(f.location)}</td>
            <td>${esc(f.dateFound)}</td>
            <td>${tag(f.status)}</td>
            <td>${esc(f.reportedBy.name)}</td>
            ${manage ? `<td><div class="row-actions">
                <select id="st-${f.id}" aria-label="New status for item ${f.id}">
                    ${["AVAILABLE", "CLAIMED", "RETURNED"].map(s => `<option ${s === f.status ? "selected" : ""}>${s}</option>`).join("")}
                </select>
                <button class="btn small" data-act="set-status" data-id="${f.id}">Update</button>
                ${canDelete(f) ? `<button class="btn small danger" data-act="del-found" data-id="${f.id}">Delete</button>` : ""}
            </div></td>` : ""}
        </tr>`).join("");

    content().innerHTML = `
        <h2>Found items</h2>
        ${filterBar("found", ["AVAILABLE", "CLAIMED", "RETURNED"])}
        ${list.length ? `<div class="table-wrap"><table class="data">
            <thead><tr><th>ID</th><th>Category</th><th>Description</th><th>Location</th><th>Date found</th><th>Status</th><th>Logged by</th>${manage ? "<th>Change status</th>" : ""}</tr></thead>
            <tbody>${rows}</tbody></table></div>`
            : `<div class="empty">No found items match these filters.</div>`}`;
    bindFilters("found", viewFound);
}

async function viewMatches() {
    const list = await api("/matches");
    const manage = isStaffOrAdmin();

    const tickets = list.map(m => `
        <article class="ticket">
            <div class="ticket-main">
                <div class="stub">
                    <h4>Lost report #${m.lostReport.id} by ${esc(m.lostReport.reportedBy.name)}</h4>
                    <div class="what">${esc(m.lostReport.description)}</div>
                    <div class="meta">${esc(m.lostReport.category.name)}, ${esc(m.lostReport.location)}, ${esc(m.lostReport.dateLost)}</div>
                </div>
                <div class="perf" aria-hidden="true"></div>
                <div class="stub">
                    <h4>Found item #${m.foundItem.id} logged by ${esc(m.foundItem.reportedBy.name)}</h4>
                    <div class="what">${esc(m.foundItem.description)}</div>
                    <div class="meta">${esc(m.foundItem.category.name)}, ${esc(m.foundItem.location)}, ${esc(m.foundItem.dateFound)}</div>
                </div>
            </div>
            <div class="ticket-foot">
                <div class="chips">
                    ${m.sharedKeywords.length ? "Shared words:" : ""}
                    ${m.sharedKeywords.map(w => `<span class="chip">${esc(w)}</span>`).join("")}
                    ${m.sharedLocationWords.length ? "Same place:" : ""}
                    ${m.sharedLocationWords.map(w => `<span class="chip">${esc(w)}</span>`).join("")}
                </div>
                ${manage ? `<button class="btn primary small" data-act="confirm"
                    data-lost="${m.lostReport.id}" data-found="${m.foundItem.id}">Confirm this match</button>` : ""}
            </div>
        </article>`).join("");

    content().innerHTML = `
        <h2>Possible matches</h2>
        <p class="muted">Open lost reports paired with available found items in the same category
        that share a keyword or a place name. Best matches first.</p>
        ${list.length ? `<div class="tickets">${tickets}</div>`
            : `<div class="empty">No matches yet. When a found item shares a category and a keyword with an open lost report, it shows up here.</div>`}`;
}

async function viewDashboard() {
    const [d, users] = await Promise.all([api("/admin/dashboard"), api("/admin/users")]);
    const userRows = users.map(u => `<tr><td>#${u.id}</td><td>${esc(u.name)}</td><td>${esc(u.email)}</td><td>${esc(u.role)}</td></tr>`).join("");

    content().innerHTML = `
        <h2>Dashboard</h2>
        <div class="big-stats">
            <div class="big-stat pending"><div class="n">${d.pendingItems}</div><div class="l">Pending</div><div class="d">Lost reports still waiting for a match</div></div>
            <div class="big-stat"><div class="n">${d.matchedItems}</div><div class="l">Matched</div><div class="d">Lost reports paired with a found item</div></div>
            <div class="big-stat returned"><div class="n">${d.returnedItems}</div><div class="l">Returned</div><div class="d">Found items handed back to their owner</div></div>
        </div>
        <div class="small-stats">
            <span><strong>${d.totalUsers}</strong>users</span>
            <span><strong>${d.totalLostReports}</strong>lost reports</span>
            <span><strong>${d.totalFoundItems}</strong>found items</span>
            <span><strong>${d.foundAvailable}</strong>available</span>
            <span><strong>${d.foundClaimed}</strong>claimed</span>
        </div>
        <div class="two-col">
            <div>
                <h3>Add a category</h3>
                <form id="catForm" class="panel form">
                    <label>Category name<input name="name" maxlength="100" required placeholder="Umbrella"></label>
                    <button class="btn primary">Add category</button>
                </form>
            </div>
            <div>
                <h3>Registered users</h3>
                <div class="table-wrap"><table class="data">
                    <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th></tr></thead>
                    <tbody>${userRows}</tbody></table></div>
            </div>
        </div>`;

    $("#catForm").addEventListener("submit", async e => {
        e.preventDefault();
        try {
            await api("/categories", { method: "POST", body: formToObject(e.target) });
            state.categories = await api("/categories");
            toast("Category added");
            go("dashboard");
        } catch (err) { toast(err.message, "error"); }
    });
}

/* ---------- row actions (one listener for the whole page) ---------- */
content().addEventListener("click", async e => {
    const btn = e.target.closest("[data-act]");
    if (!btn) return;
    const { act, id, lost, found } = btn.dataset;

    try {
        if (act === "del-lost") {
            if (!confirm("Delete this lost report?")) return;
            await api(`/lost/${id}`, { method: "DELETE" });
            toast("Lost report deleted");
            go("lost");
        } else if (act === "del-found") {
            if (!confirm("Delete this found item?")) return;
            await api(`/found/${id}`, { method: "DELETE" });
            toast("Found item deleted");
            go("found");
        } else if (act === "set-status") {
            const status = $("#st-" + id).value;
            await api(`/found/${id}/status`, { method: "PUT", body: { status } });
            toast(`Item #${id} is now ${status}`);
            go("found");
        } else if (act === "confirm") {
            await api("/matches/confirm", { method: "POST", body: { lostReportId: Number(lost), foundItemId: Number(found) } });
            toast("Match confirmed. The lost report is now MATCHED.");
            go("matches");
        }
    } catch (err) {
        toast(err.message, "error");
        if (act === "set-status") go("found"); // put the dropdown back to the real status
    }
});

/* ---------- start ---------- */
(function boot() {
    const saved = localStorage.getItem("cf_user");
    if (saved) {
        try {
            enter(JSON.parse(saved)).catch(() => logout());
            return;
        } catch (e) { /* fall through to login */ }
    }
    logout();
})();
