import { describe, it, expect } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { ChargepointManagementView } from "../components/ChargepointManagementView";
import { VendorRevenueAnalyticsView } from "../components/VendorRevenueAnalyticsView";
import { VendorStaffManagementView } from "../components/VendorStaffManagementView";

describe("VendorAdminPortal Components", () => {
  it("renders Chargepoint Management table and filters by search input", () => {
    render(<ChargepointManagementView />);
    
    expect(screen.getByText("Chargepoint Management")).toBeInTheDocument();
    expect(screen.getByText("Downtown EV Hub - Bay 1")).toBeInTheDocument();
    expect(screen.getByText("Airport Supercharger North")).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText("Search by code, station name, or location address...");
    fireEvent.change(searchInput, { target: { value: "Airport" } });

    expect(screen.getByText("Airport Supercharger North")).toBeInTheDocument();
    expect(screen.queryByText("Downtown EV Hub - Bay 1")).not.toBeInTheDocument();
  });

  it("renders Financial Analytics dashboard with KPI cards and export button", () => {
    render(<VendorRevenueAnalyticsView />);

    expect(screen.getByText("Vendor Financial Analytics")).toBeInTheDocument();
    expect(screen.getByText("Total Gross Income")).toBeInTheDocument();
    expect(screen.getByText("Platform Markup (10%)")).toBeInTheDocument();
    expect(screen.getByText("Net Vendor Payout")).toBeInTheDocument();
    expect(screen.getByText("Export CSV")).toBeInTheDocument();
  });

  it("renders Staff Management list and invites a new staff member", () => {
    render(<VendorStaffManagementView />);

    expect(screen.getByText("Vendor Staff Management")).toBeInTheDocument();
    expect(screen.getAllByText("Sarah Jenkins")[0]).toBeInTheDocument();

    const inviteBtn = screen.getByText("Invite Staff Member");
    fireEvent.click(inviteBtn);

    expect(screen.getByText("Invite Vendor Staff Member")).toBeInTheDocument();
  });
});
