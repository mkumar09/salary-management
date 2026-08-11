import apiClient from "./client";

export async function fetchCompensationHistory(employeeId) {
  const { data } = await apiClient.get(`/employees/${employeeId}/compensation`);
  return data;
}

export async function addCompensationRecord(employeeId, payload) {
  const { data } = await apiClient.post(`/employees/${employeeId}/compensation`, payload);
  return data;
}
