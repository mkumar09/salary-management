import { describe, expect, it } from "vitest";
import { formatCompactNumber, formatCompactUsd, formatDate, formatMoney } from "./format";

describe("formatMoney", () => {
  it("formats an amount with its currency symbol", () => {
    expect(formatMoney(100000, "USD")).toContain("100,000");
  });

  it("returns a placeholder for null amount", () => {
    expect(formatMoney(null, "USD")).toBe("—");
  });

  it("falls back to a plain string for a malformed currency code", () => {
    expect(formatMoney(100, "US")).toBe("100 US");
  });
});

describe("formatCompactUsd", () => {
  it("compacts large numbers with a K/M suffix", () => {
    expect(formatCompactUsd(1250000)).toMatch(/1\.[23]M/);
  });

  it("returns a placeholder for null", () => {
    expect(formatCompactUsd(null)).toBe("—");
  });
});

describe("formatCompactNumber", () => {
  it("compacts thousands", () => {
    expect(formatCompactNumber(10000)).toBe("10K");
  });
});

describe("formatDate", () => {
  it("formats an ISO date string", () => {
    expect(formatDate("2024-01-15")).toContain("2024");
  });

  it("returns a placeholder for a missing date", () => {
    expect(formatDate(null)).toBe("—");
  });
});
