// src/api/client.js

// 1. GET 요청용 함수 (기존 코드 유지)
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
    err.status = res.status;
    err.data = data;
    throw err;
  }

  return data;
}

// 2. POST 요청용 함수 (✅ 새로 추가됨)
// 회원가입, 로그인 등 데이터를 서버에 보낼 때 사용합니다.
export async function apiPost(path, body) {
  const res = await fetch(path, {
    method: "POST", // 데이터를 보낼 때는 POST 방식을 사용합니다.
    headers: {
      "Content-Type": "application/json", // JSON 형식의 데이터를 보낸다는 설정
    },
    body: JSON.stringify(body), // 자바스크립트 객체를 문자열로 변환하여 전송
  });

  const text = await res.text().catch(() => "");
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { raw: text };
  }

  if (!res.ok) {
    const err = new Error(data?.message || data?.error || `HTTP ${res.status}`);
    err.status = res.status;
    err.data = data;
    throw err;
  }

  return data;
}
