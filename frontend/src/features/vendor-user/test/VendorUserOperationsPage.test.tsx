import { describe, it, expect } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { VendorUserOperationsPage } from "../pages/VendorUserOperationsPage";
import { RealtimeMonitorGrid } from "../components/RealtimeMonitorGrid";
import { SessionReportingTable } from "../components/SessionReportingTable";
import { INITIAL_MOCK_CHARGERS, INITIAL_MOCK_SESSIONS } from "../mocks/vendorUserMockData";

describe("VendorUserOperationsPage & Components", () => {
  it("renders Vendor Operational Console header and tab switcher", () => {
    render(<VendorUserOperationsPage />);

    expect(screen.getByText("Vendor Operational Console")).toBeInTheDocument();
    expect(screen.getByText("Live Status Grid")).toBeInTheDocument();
    expect(screen.getByText("Session Reports")).toBeInTheDocument();
  });

  it("renders RealtimeMonitorGrid with charger status cards and filtering", () => {
    render(
      <RealtimeMonitorGrid
        chargers={INITIAL_MOCK_CHARGERS}
        isLiveStreamActive={true}
        onToggleLiveStream={() => {}}
        eventLogs={[]}
        onOpenMaintenanceModal={() => {}}
        onOpenGroupMaintenanceModal={() => {}}
      />
    );

    expect(screen.getByText("Total Chargers")).toBeInTheDocument();
    expect(screen.getByText("FastCharger-Alpha-1")).toBeInTheDocument();
    expect(screen.getByText("CityExpress-01")).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText("Search charger, ID, station...");
    fireEvent.change(searchInput, { target: { value: "Alpha-1" } });

    expect(screen.getByText("FastCharger-Alpha-1")).toBeInTheDocument();
    expect(screen.queryByText("CityExpress-01")).not.toBeInTheDocument();
  });

  it("renders SessionReportingTable and filters by date and status", () => {
    render(<SessionReportingTable sessions={INITIAL_MOCK_SESSIONS} />);

    expect(screen.getByText("Reported Sessions")).toBeInTheDocument();
    expect(screen.getByText("SES-8821")).toBeInTheDocument();
    expect(screen.getByText("Export CSV")).toBeInTheDocument();
    expect(screen.getByText("PDF Report")).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText("Search session ID, email, charger...");
    fireEvent.change(searchInput, { target: { value: "SES-8821" } });

    expect(screen.getByText("SES-8821")).toBeInTheDocument();
    expect(screen.queryByText("SES-8822")).not.toBeInTheDocument();
  });
});
