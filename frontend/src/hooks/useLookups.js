import { useEffect, useState } from "react";
import { fetchCountries, fetchDepartments } from "../api/lookups";

export default function useLookups() {
  const [departments, setDepartments] = useState([]);
  const [countries, setCountries] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    Promise.all([fetchDepartments(), fetchCountries()])
      .then(([departmentData, countryData]) => {
        if (cancelled) return;
        setDepartments(departmentData);
        setCountries(countryData);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return { departments, countries, loading };
}
