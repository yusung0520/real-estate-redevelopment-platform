// src/api/client.js
export async function apiGet(path) {
  const res = await fetch(path);

  const text = await res.text().catch(() => "");
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { raw: text };
  }

  if (!res.ok) {
    const err = new Error(data?.message || data?.error || `HTTP ${res.status}`);
    err.status = res.status; // ✅ 바로 여기!
    err.data = data;
    throw err;
  }

  return data;
}
