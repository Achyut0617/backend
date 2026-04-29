// Utility functions for API calls and token management
function getToken(role) {
    return localStorage.getItem(role + 'Token');
}

function setToken(role, token) {
    localStorage.setItem(role + 'Token', token);
}

function logout(role) {
    localStorage.removeItem(role + 'Token');
    window.location.href = `/${role}/login.html`;
}

function checkAuth(role) {
    if (!getToken(role)) {
        window.location.href = `/${role}/login.html`;
    }
}

async function apiCall(endpoint, method = 'GET', body = null, role = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (role) {
        const token = getToken(role);
        if (token) headers['Authorization'] = 'Bearer ' + token;
    }
    
    const config = { method, headers };
    if (body) config.body = JSON.stringify(body);
    
    const response = await fetch(endpoint, config);
    let json = {};
    try {
        json = await response.json();
    } catch (e) {
        // No JSON response body
    }
    
    if (!response.ok) {
        throw new Error(json.message || 'API Error: ' + response.statusText);
    }
    return json.data;
}
