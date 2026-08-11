import apiClient from "./client";

export async function fetchDepartments() {
  const { data } = await apiClient.get("/departments");
  return data;
}

export async function fetchCountries() {
  const { data } = await apiClient.get("/countries");
  return data;
}
