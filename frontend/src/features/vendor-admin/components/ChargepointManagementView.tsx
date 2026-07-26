import React, { useState, useEffect } from "react";
import { Chargepoint, ChargepointGroup } from "../types/vendorAdmin.types";
import { INITIAL_CHARGEPOINTS, INITIAL_GROUPS } from "../mocks/vendorAdminData";
import { vendorAdminApi, BackendStationResponse } from "../api/vendorAdminApi";
import { Zap, Plus, Search, Tag, Edit3, Trash2, ShieldAlert, CheckCircle2, DollarSign, RefreshCw } from "lucide-react";

export const ChargepointManagementView: React.FC = () => {
  const [chargepoints, setChargepoints] = useState<Chargepoint[]>(INITIAL_CHARGEPOINTS);
  const [groups, setGroups] = useState<ChargepointGroup[]>(INITIAL_GROUPS);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedGroupFilter, setSelectedGroupFilter] = useState<string>("ALL");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCp, setEditingCp] = useState<Partial<Chargepoint> | null>(null);

  // Group Label Modal State
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [newGroupName, setNewGroupName] = useState("");
  const [newGroupPrice, setNewGroupPrice] = useState("350");

  const mapBackendStationToChargepoint = (st: BackendStationResponse): Chargepoint => ({
    id: st.id,
    stationCode: st.stationCode,
    name: st.name,
    vendorId: st.vendorId,
    vendorName: "Owned Vendor Station",
    status: (st.status as any) || "AVAILABLE",
    connectorsCount: st.connectors?.length || 2,
    groupLabel: st.groupLabel || "General",
    basePriceTenthsOfCentsPerKwh: st.pricingPolicy?.basePriceTenthsOfCentsPerKwh || 350,
    locationAddress: st.location?.address || "Station Location",
    createdAt: st.createdAt,
    updatedAt: st.updatedAt,
  });

  const fetchStations = async () => {
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const res = await vendorAdminApi.listStations();
      if (res && res.items) {
        const fetchedCps = res.items.map(mapBackendStationToChargepoint);
        setChargepoints(fetchedCps);
      }
    } catch (err: any) {
      console.warn("Backend API offline or error, using mock chargepoints:", err.message);
      setErrorMsg("Backend unavailable — showing local mock chargepoints.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStations();
  }, []);

  const filteredChargepoints = chargepoints.filter((cp) => {
    const matchesSearch =
      cp.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cp.stationCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cp.locationAddress.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesGroup = selectedGroupFilter === "ALL" || cp.groupLabel === selectedGroupFilter;
    return matchesSearch && matchesGroup;
  });

  const handleSaveChargepoint = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCp?.name || !editingCp?.stationCode) return;

    try {
      if (editingCp.id) {
        // API Update station
        await vendorAdminApi.updateStation(editingCp.id, {
          name: editingCp.name,
          groupLabel: editingCp.groupLabel,
          basePriceTenthsOfCentsPerKwh: Number(editingCp.basePriceTenthsOfCentsPerKwh) || 350,
          location: {
            address: editingCp.locationAddress || "Main Location",
            latitude: 37.7749,
            longitude: -122.4194,
          },
        });
        await fetchStations();
      } else {
        // API Create station
        await vendorAdminApi.createStation({
          stationCode: editingCp.stationCode,
          name: editingCp.name,
          location: {
            address: editingCp.locationAddress || "Main Location",
            latitude: 37.7749,
            longitude: -122.4194,
          },
          connectors: Array.from({ length: Number(editingCp.connectorsCount) || 2 }, (_, i) => ({
            connectorId: i + 1,
            type: "CCS2",
            maxPowerKw: 150,
          })),
          groupLabel: editingCp.groupLabel || "General",
          basePriceTenthsOfCentsPerKwh: Number(editingCp.basePriceTenthsOfCentsPerKwh) || 350,
        });
        await fetchStations();
      }
    } catch (err: any) {
      // Fallback local update if backend fails
      if (editingCp.id) {
        setChargepoints((prev) =>
          prev.map((c) =>
            c.id === editingCp.id
              ? {
                  ...c,
                  ...(editingCp as Chargepoint),
                  updatedAt: new Date().toISOString(),
                }
              : c
          )
        );
      } else {
        const newCp: Chargepoint = {
          id: `cp-${Date.now()}`,
          stationCode: editingCp.stationCode,
          name: editingCp.name,
          vendorId: "v-001",
          vendorName: "EcoCharge Networks",
          status: (editingCp.status as any) || "AVAILABLE",
          connectorsCount: Number(editingCp.connectorsCount) || 2,
          groupLabel: editingCp.groupLabel || "General",
          basePriceTenthsOfCentsPerKwh: Number(editingCp.basePriceTenthsOfCentsPerKwh) || 300,
          locationAddress: editingCp.locationAddress || "Main Office",
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };
        setChargepoints((prev) => [newCp, ...prev]);
      }
    }

    setIsModalOpen(false);
    setEditingCp(null);
  };

  const handleDeleteChargepoint = async (id: string) => {
    if (confirm("Are you sure you want to remove this chargepoint from your vendor account?")) {
      try {
        await vendorAdminApi.deleteStation(id);
        await fetchStations();
      } catch (err: any) {
        setChargepoints((prev) => prev.filter((c) => c.id !== id));
      }
    }
  };

  const handleAddGroup = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newGroupName) return;
    const newGroup: ChargepointGroup = {
      id: `grp-${Date.now()}`,
      name: newGroupName,
      chargepointCount: 0,
      defaultPriceTenthsOfCents: Number(newGroupPrice),
      description: "Custom group",
    };
    setGroups((prev) => [...prev, newGroup]);
    setNewGroupName("");
    setIsGroupModalOpen(false);
  };

  return (
    <div className="space-y-6">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Zap className="w-5 h-5 text-cyan-400" /> Chargepoint Management
          </h2>
          <p className="text-xs text-slate-400">
            Configure charging hardware, assign group tags, and manage base kWh pricing (in tenths of cents).
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchStations}
            disabled={isLoading}
            className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs border border-slate-700 transition-colors"
            title="Refresh Stations"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? "animate-spin text-cyan-400" : ""}`} />
          </button>

          <button
            onClick={() => setIsGroupModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold border border-slate-700 transition-colors"
          >
            <Tag className="w-4 h-4 text-cyan-400" /> Manage Group Labels
          </button>

          <button
            onClick={() => {
              setEditingCp({
                status: "AVAILABLE",
                connectorsCount: 4,
                basePriceTenthsOfCentsPerKwh: 350,
                groupLabel: groups[0]?.name || "General",
              });
              setIsModalOpen(true);
            }}
            className="flex items-center gap-1.5 px-4 py-2 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold rounded-xl text-xs shadow-lg shadow-cyan-500/20 transition-all"
          >
            <Plus className="w-4 h-4" /> Add Chargepoint
          </button>
        </div>
      </div>

      {errorMsg && (
        <div className="p-3 bg-amber-950/60 border border-amber-800/80 rounded-xl text-xs text-amber-300 flex items-center justify-between">
          <span>{errorMsg}</span>
          <button onClick={() => setErrorMsg(null)} className="text-amber-400 font-bold ml-2">Dismiss</button>
        </div>
      )}

      {/* Filters & Search */}
      <div className="flex flex-col sm:flex-row items-center gap-3 bg-slate-900/60 p-3 rounded-2xl border border-slate-800">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            placeholder="Search by code, station name, or location address..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-4 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <span className="text-xs font-medium text-slate-400 whitespace-nowrap">Group:</span>
          <select
            value={selectedGroupFilter}
            onChange={(e) => setSelectedGroupFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 w-full sm:w-auto"
          >
            <option value="ALL">All Groups ({chargepoints.length})</option>
            {groups.map((g) => (
              <option key={g.id} value={g.name}>
                {g.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Chargepoints Table */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-950/60 border-b border-slate-800 text-slate-400 uppercase font-mono text-[10px] tracking-wider">
                <th className="py-3.5 px-4">Station Code / Name</th>
                <th className="py-3.5 px-4">Group Label</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Connectors</th>
                <th className="py-3.5 px-4">Base Price (kWh)</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300 font-sans">
              {filteredChargepoints.map((cp) => {
                const dollars = (cp.basePriceTenthsOfCentsPerKwh / 1000).toFixed(3);
                return (
                  <tr key={cp.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="font-bold text-slate-100">{cp.name}</div>
                      <div className="font-mono text-[10px] text-cyan-400">{cp.stationCode}</div>
                      <div className="text-[11px] text-slate-500 truncate max-w-xs">{cp.locationAddress}</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-slate-800 text-cyan-300 border border-cyan-500/20 text-[11px] font-medium">
                        <Tag className="w-3 h-3 text-cyan-400" /> {cp.groupLabel || "Default"}
                      </span>
                    </td>

                    <td className="py-3.5 px-4">
                      {cp.status === "AVAILABLE" && (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-semibold bg-emerald-950 text-emerald-400 border border-emerald-800">
                          <CheckCircle2 className="w-3 h-3" /> Available
                        </span>
                      )}
                      {cp.status === "CHARGING" && (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-semibold bg-blue-950 text-blue-400 border border-blue-800">
                          <Zap className="w-3 h-3 animate-pulse" /> Charging
                        </span>
                      )}
                      {cp.status === "MAINTENANCE" && (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-semibold bg-amber-950 text-amber-400 border border-amber-800">
                          <ShieldAlert className="w-3 h-3" /> Maintenance
                        </span>
                      )}
                    </td>

                    <td className="py-3.5 px-4 font-mono font-medium text-slate-300">
                      {cp.connectorsCount} Ports
                    </td>

                    <td className="py-3.5 px-4 font-mono">
                      <span className="text-slate-100 font-bold">${dollars}</span>
                      <span className="text-[10px] text-slate-500 block font-sans">
                        ({cp.basePriceTenthsOfCentsPerKwh} tenths)
                      </span>
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => {
                            setEditingCp(cp);
                            setIsModalOpen(true);
                          }}
                          className="p-1.5 text-slate-400 hover:text-cyan-400 hover:bg-slate-800 rounded-lg transition-colors"
                          title="Edit Chargepoint"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteChargepoint(cp.id)}
                          className="p-1.5 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition-colors"
                          title="Remove Chargepoint"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}

              {filteredChargepoints.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-500 italic">
                    No chargepoints found matching criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add / Edit Chargepoint Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-base font-bold text-slate-100">
              {editingCp?.id ? "Edit Chargepoint & Pricing" : "Add New Chargepoint"}
            </h3>

            <form onSubmit={handleSaveChargepoint} className="space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Station Code</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. CP-US-WEST-09"
                  value={editingCp?.stationCode || ""}
                  onChange={(e) => setEditingCp({ ...editingCp, stationCode: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Display Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Bay Area Hub 1"
                  value={editingCp?.name || ""}
                  onChange={(e) => setEditingCp({ ...editingCp, name: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Group Label</label>
                  <select
                    value={editingCp?.groupLabel || ""}
                    onChange={(e) => setEditingCp({ ...editingCp, groupLabel: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                  >
                    {groups.map((g) => (
                      <option key={g.id} value={g.name}>
                        {g.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Connectors Count</label>
                  <input
                    type="number"
                    min="1"
                    max="16"
                    value={editingCp?.connectorsCount || 4}
                    onChange={(e) => setEditingCp({ ...editingCp, connectorsCount: Number(e.target.value) })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1 flex items-center justify-between">
                  <span>Base Price (Tenths of Cents / kWh)</span>
                  <span className="text-cyan-400 font-mono text-[11px]">
                    =${((Number(editingCp?.basePriceTenthsOfCentsPerKwh || 0)) / 1000).toFixed(3)}/kWh
                  </span>
                </label>
                <div className="relative">
                  <DollarSign className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                  <input
                    type="number"
                    required
                    min="0"
                    step="10"
                    placeholder="350 (represents $0.35/kWh)"
                    value={editingCp?.basePriceTenthsOfCentsPerKwh || 350}
                    onChange={(e) =>
                      setEditingCp({ ...editingCp, basePriceTenthsOfCentsPerKwh: Number(e.target.value) })
                    }
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-4 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
                  />
                </div>
                <p className="text-[10px] text-slate-500 mt-1">
                  1 cent = 10 tenths. For $0.35/kWh enter 350.
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Location Address</label>
                <input
                  type="text"
                  placeholder="Street address, city, state"
                  value={editingCp?.locationAddress || ""}
                  onChange={(e) => setEditingCp({ ...editingCp, locationAddress: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl bg-cyan-500 text-slate-950 text-xs font-bold hover:bg-cyan-400"
                >
                  Save Chargepoint
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Group Label Modal */}
      {isGroupModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-sm w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-base font-bold text-slate-100">Create Chargepoint Group Label</h3>
            <form onSubmit={handleAddGroup} className="space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Group Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Commercial Fleet Hub"
                  value={newGroupName}
                  onChange={(e) => setNewGroupName(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Default Base Price (Tenths/kWh)</label>
                <input
                  type="number"
                  required
                  value={newGroupPrice}
                  onChange={(e) => setNewGroupPrice(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsGroupModalOpen(false)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl bg-cyan-500 text-slate-950 text-xs font-bold hover:bg-cyan-400"
                >
                  Create Group
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
