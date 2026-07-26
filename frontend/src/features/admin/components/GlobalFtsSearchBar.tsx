import React, { useState, useEffect } from 'react';
import { Search, User, Car, AlertTriangle, ArrowRight, Activity } from 'lucide-react';
import { adminApi } from '../api/adminApi';
import { FtsSearchResultItem } from '../types/admin.types';

export const GlobalFtsSearchBar: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<FtsSearchResultItem[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (query.trim().length < 2) {
      setResults([]);
      setIsOpen(false);
      return;
    }

    const timer = setTimeout(async () => {
      setLoading(true);
      try {
        const res = await adminApi.searchFts(query);
        setResults(res);
        setIsOpen(true);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }, 250);

    return () => clearTimeout(timer);
  }, [query]);

  const getTypeIcon = (type: FtsSearchResultItem['type']) => {
    switch (type) {
      case 'SESSION':
        return <Activity className="w-4 h-4 text-cyan-400" />;
      case 'CUSTOMER':
        return <User className="w-4 h-4 text-emerald-400" />;
      case 'VEHICLE':
        return <Car className="w-4 h-4 text-purple-400" />;
      case 'ERROR_CODE':
        return <AlertTriangle className="w-4 h-4 text-amber-400" />;
    }
  };

  return (
    <div className="relative w-full max-w-md">
      <div className="relative">
        <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-500" />
        <input
          type="text"
          placeholder="Global Search (sessions, plates AUD*, account #, errors)..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => query.length >= 2 && setIsOpen(true)}
          className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-4 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500/60 shadow-inner"
        />
        {loading && (
          <span className="absolute right-3 top-2.5 text-xs text-slate-500 animate-pulse">
            Searching...
          </span>
        )}
      </div>

      {isOpen && results.length > 0 && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-slate-900 border border-slate-800 rounded-xl shadow-2xl overflow-hidden z-50 animate-in fade-in zoom-in-95 duration-150">
          <div className="px-3 py-2 border-b border-slate-800/80 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
            Matching Search Results
          </div>

          <div className="max-h-80 overflow-y-auto divide-y divide-slate-800/50">
            {results.map((item) => (
              <a
                key={item.id}
                href={item.targetUrl}
                onClick={() => setIsOpen(false)}
                className="flex items-start gap-3 p-3 hover:bg-slate-800/60 transition-colors group"
              >
                <div className="mt-0.5 p-1.5 rounded-lg bg-slate-950 border border-slate-800">
                  {getTypeIcon(item.type)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-slate-100 group-hover:text-cyan-400 transition-colors">
                      {item.title}
                    </span>
                    <span className="text-[10px] text-slate-500 uppercase tracking-wider font-mono">
                      {item.type}
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400 truncate">{item.subtitle}</p>
                  <p className="text-[11px] text-slate-500 italic mt-0.5 truncate">{item.snippet}</p>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-600 group-hover:text-cyan-400 group-hover:translate-x-0.5 transition-all self-center" />
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
