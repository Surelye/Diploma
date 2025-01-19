export const defaultHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `${getBasicAuthHeaderValue()}`
}

export function getBasicAuthHeaderValue() {
    const toBase64 = `${localStorage.getItem("username")}:${localStorage.getItem("password")}`;
    return `Basic ${btoa(toBase64)}`;
}