import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { BrowserRouter } from "react-router-dom";
import { CustomerProfilePage } from "../pages/CustomerProfilePage";

vi.mock("../../auth/hooks/useAuth", () => ({
  useAuth: () => ({
    user: {
      id: "cust-test-123",
      email: "customer@evcharging.test",
      fullName: "Test Customer",
      role: "ROLE_CUSTOMER",
    },
    logout: vi.fn(),
  }),
}));

describe("CustomerProfilePage", () => {
  it("renders customer profile details and account number", () => {
    render(
      <BrowserRouter>
        <CustomerProfilePage />
      </BrowserRouter>
    );

    expect(screen.getByText("Test Customer")).toBeInTheDocument();
    expect(screen.getByText("ROLE_CUSTOMER")).toBeInTheDocument();
    expect(screen.getByText("ACC-CUST-CUST-TES")).toBeInTheDocument();
    expect(screen.getByText("customer@evcharging.test")).toBeInTheDocument();
  });
});
