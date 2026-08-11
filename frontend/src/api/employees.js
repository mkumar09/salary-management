import apiClient from "./client";

export async function fetchEmployees({ page = 0, size = 25, search, departmentId, countryId, status } = {}) {
  const { data } = await apiClient.get("/employees", {
    params: { page, size, search, departmentId, countryId, status },
  });
  return data;
}

export async function fetchEmployee(id) {
  const { data } = await apiClient.get(`/employees/${id}`);
  return data;
}

export async function createEmployee(payload) {
  const { data } = await apiClient.post("/employees", payload);
  return data;
}

export async function updateEmployee(id, payload) {
  const { data } = await apiClient.put(`/employees/${id}`, payload);
  return data;
}

export async function deleteEmployee(id) {
  await apiClient.delete(`/employees/${id}`);
}
