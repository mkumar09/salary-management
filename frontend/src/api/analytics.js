import apiClient from "./client";

export async function fetchSummary() {
  const { data } = await apiClient.get("/analytics/summary");
  return data;
}

export async function fetchByDepartment() {
  const { data } = await apiClient.get("/analytics/by-department");
  return data;
}

export async function fetchByCountry() {
  const { data } = await apiClient.get("/analytics/by-country");
  return data;
}

export async function fetchDistribution() {
  const { data } = await apiClient.get("/analytics/distribution");
  return data;
}
