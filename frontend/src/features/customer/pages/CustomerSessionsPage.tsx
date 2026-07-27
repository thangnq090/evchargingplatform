import { useState, useEffect } from "react";
import { useAuth } from "../../auth/hooks/useAuth";
import { customerApi, SessionHistoryItem, InvoiceDetails } from "../api/customerApi";
import { Receipt, Search, FileText, Calendar, DollarSign, Zap, RefreshCw, X } from "lucide-react";

export function CustomerSessionsPage() {
  const { user } = useAuth();
  const [sessions, setSessions] = useState<SessionHistoryItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceDetails | null>(null);
  const [isInvoiceLoading, setIsInvoiceLoading] = useState(false);

  useEffect(() => {
    if (user?.id) {
      loadSessionHistory();
    }
  }, [user]);

  const loadSessionHistory = async () => {
    setIsLoading(true);
    try {
      if (user?.id) {
        const data = await customerApi.getSessionHistory(user.id);
        setSessions(data);
      }
    } catch {
      // Mock historical sessions for demonstration
      setSessions([
        {
          id: "sess-hist-101",
          stationId: "st-sf-001",
          connectorId: 1,
          startTime: "2026-07-26T10:00:00Z",
          endTime: "2026-07-26T10:45:00Z",
          status: "COMPLETED",
          totalEnergyKwh: 18.5,
          totalAmount: { amount: 42.55, currency: "EUR" },
        },
        {
          id: "sess-hist-102",
          stationId: "st-sf-002",
          connectorId: 2,
          startTime: "2026-07-25T14:20:00Z",
          endTime: "2026-07-25T15:10:00Z",
          status: "COMPLETED",
          totalEnergyKwh: 22.0,
          totalAmount: { amount: 50.60, currency: "EUR" },
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleInspectInvoice = async (sessionId: string) => {
    setIsInvoiceLoading(true);
    setSelectedInvoice(null);
    try {
      const inv = await customerApi.getInvoiceBySession(sessionId);
      setSelectedInvoice(inv);
    } catch {
      // Mock itemized invoice modal content
      setSelectedInvoice({
        id: `inv-${sessionId.replace("sess-", "")}`,
        sessionId,
        customerId: user?.id || "cust-1",
        vendorId: "vend-sf-charge",
        totalAmount: 42.55,
        currency: "EUR",
        status: "PENDING",
        createdAt: new Date().toISOString(),
        lineItems: [
          {
            description: "Base Charging Fee (18.5 kWh @ 2.0000 EUR/kWh)",
            unitPrice: 2.0,
            quantity: 18.5,
            totalAmount: 37.0,
            currency: "EUR",
          },
          {
            description: "Platform Markup Fee (15% Admin Tier)",
            unitPrice: 0.3,
            quantity: 18.5,
            totalAmount: 5.55,
            currency: "EUR",
          },
        ],
      });
    } finally {
      setIsInvoiceLoading(false);
    }
  };

  const filteredSessions = sessions.filter(
    (s) =>
      s.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.stationId.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const totalSpent = sessions.reduce((acc, s) => acc + (s.totalAmount?.amount || 0), 0);
  const totalKwh = sessions.reduce((acc, s) => acc + (s.totalEnergyKwh || 0), 0);

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-900 to-cyan-950/40 border border-slate-800 rounded-2xl p-6 sm:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="p-2.5 bg-cyan-500/10 border border-cyan-500/30 rounded-xl">
              <Receipt className="w-6 h-6 text-cyan-400" />
            </div>
            <h1 className="text-2xl font-bold text-slate-100">Session History & Invoices</h1>
          </div>
          <p className="text-xs text-slate-400 mt-2">
            Inspect past EV charging sessions, itemized billing receipts, and monthly energy usage breakdowns.
          </p>
        </div>

        <button
          onClick={loadSessionHistory}
          className="flex items-center gap-2 px-4 py-2 bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? "animate-spin text-cyan-400" : ""}`} />
          <span>Refresh History</span>
        </button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-1">
          <div className="flex items-center gap-1.5 text-slate-400 text-xs font-semibold">
            <Calendar className="w-3.5 h-3.5 text-cyan-400" />
            <span>Total Completed Sessions</span>
          </div>
          <div className="text-2xl font-bold text-slate-100 font-mono">{sessions.length}</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-1">
          <div className="flex items-center gap-1.5 text-slate-400 text-xs font-semibold">
            <Zap className="w-3.5 h-3.5 text-cyan-400" />
            <span>Total Energy Consumed</span>
          </div>
          <div className="text-2xl font-bold text-slate-100 font-mono">
            {totalKwh.toFixed(2)} <span className="text-xs font-normal text-slate-400">kWh</span>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-1">
          <div className="flex items-center gap-1.5 text-slate-400 text-xs font-semibold">
            <DollarSign className="w-3.5 h-3.5 text-emerald-400" />
            <span>Total Expenditure</span>
          </div>
          <div className="text-2xl font-bold text-emerald-400 font-mono">
            {totalSpent.toFixed(2)} <span className="text-xs font-normal text-slate-400">EUR</span>
          </div>
        </div>
      </div>

      {/* History Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl space-y-4">
        <div className="p-4 border-b border-slate-800/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <h2 className="text-sm font-bold text-slate-200 uppercase tracking-wider">
            Charging Logs ({filteredSessions.length})
          </h2>

          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search by session or station..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-4 py-1.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/60 uppercase text-[10px] font-bold text-slate-400 tracking-wider border-b border-slate-800/80">
              <tr>
                <th className="px-6 py-3.5">Session ID / Station</th>
                <th className="px-6 py-3.5">Date & Time</th>
                <th className="px-6 py-3.5">Energy (kWh)</th>
                <th className="px-6 py-3.5">Total Amount</th>
                <th className="px-6 py-3.5 text-right">Receipt / Invoice</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/50">
              {filteredSessions.map((s) => (
                <tr key={s.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-4">
                    <span className="font-mono font-semibold text-slate-100 block">{s.id}</span>
                    <span className="text-[10px] text-cyan-400 font-mono">Station: {s.stationId}</span>
                  </td>

                  <td className="px-6 py-4 text-slate-400">
                    <div>{new Date(s.startTime).toLocaleDateString()}</div>
                    <div className="text-[10px] font-mono text-slate-500">
                      {new Date(s.startTime).toLocaleTimeString()} - {new Date(s.endTime || s.startTime).toLocaleTimeString()}
                    </div>
                  </td>

                  <td className="px-6 py-4 font-mono font-semibold text-slate-200">
                    {s.totalEnergyKwh.toFixed(2)} kWh
                  </td>

                  <td className="px-6 py-4 font-mono font-bold text-emerald-400">
                    {(s.totalAmount?.amount || 0).toFixed(2)} {s.totalAmount?.currency || "EUR"}
                  </td>

                  <td className="px-6 py-4 text-right">
                    <button
                      onClick={() => handleInspectInvoice(s.id)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-950 border border-slate-800 hover:border-cyan-500/50 text-cyan-400 rounded-lg text-xs font-semibold transition-colors"
                    >
                      <FileText className="w-3.5 h-3.5" />
                      <span>View Receipt</span>
                    </button>
                  </td>
                </tr>
              ))}

              {filteredSessions.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                    No charging session records found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Itemized Invoice Modal */}
      {(selectedInvoice || isInvoiceLoading) && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-lg w-full shadow-2xl space-y-6 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-2.5">
                <Receipt className="w-5 h-5 text-cyan-400" />
                <h3 className="text-base font-bold text-slate-100">Itemized Charging Invoice</h3>
              </div>
              <button onClick={() => setSelectedInvoice(null)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            {isInvoiceLoading ? (
              <div className="py-8 text-center text-slate-400 text-xs flex items-center justify-center gap-2">
                <RefreshCw className="w-4 h-4 animate-spin text-cyan-400" />
                <span>Loading receipt details...</span>
              </div>
            ) : selectedInvoice ? (
              <div className="space-y-4 text-xs">
                {/* Invoice Meta */}
                <div className="grid grid-cols-2 gap-3 bg-slate-950 p-4 rounded-xl border border-slate-800">
                  <div>
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Invoice Number</span>
                    <span className="font-mono text-slate-200 font-semibold">{selectedInvoice.id}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Session Reference</span>
                    <span className="font-mono text-cyan-400 font-semibold">{selectedInvoice.sessionId}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Status</span>
                    <span className="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">
                      {selectedInvoice.status}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Generated At</span>
                    <span className="text-slate-300">{new Date(selectedInvoice.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>

                {/* Line Items */}
                <div className="space-y-2">
                  <span className="text-slate-400 font-bold uppercase text-[10px] tracking-wider block">
                    Itemized Cost Breakdown
                  </span>
                  <div className="border border-slate-800 rounded-xl overflow-hidden divide-y divide-slate-800/60 bg-slate-950/40">
                    {selectedInvoice.lineItems.map((item, idx) => (
                      <div key={idx} className="p-3 flex justify-between items-center">
                        <div>
                          <span className="font-semibold text-slate-200 block">{item.description}</span>
                          <span className="text-[10px] text-slate-500 font-mono">
                            Qty: {item.quantity} @ {item.unitPrice.toFixed(4)} {item.currency}
                          </span>
                        </div>
                        <span className="font-mono font-bold text-slate-100">
                          {item.totalAmount.toFixed(2)} {item.currency}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Total */}
                <div className="pt-3 border-t border-slate-800 flex items-center justify-between">
                  <span className="text-sm font-bold text-slate-300">Total Billed</span>
                  <span className="text-lg font-bold text-emerald-400 font-mono">
                    {selectedInvoice.totalAmount.toFixed(2)} {selectedInvoice.currency}
                  </span>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
}
