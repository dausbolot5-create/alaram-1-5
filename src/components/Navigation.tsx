import React from "react";
import { Home, Calendar, PlusCircle, History, Settings } from "lucide-react";

export type TabType = "dashboard" | "calendar" | "upload" | "history" | "settings";

interface NavigationProps {
  activeTab: TabType;
  onTabChange: (tab: TabType) => void;
}

export const Navigation: React.FC<NavigationProps> = ({ activeTab, onTabChange }) => {
  const tabs = [
    { id: "dashboard", label: "Beranda", icon: Home },
    { id: "calendar", label: "Kalender", icon: Calendar },
    { id: "upload", label: "Upload", icon: PlusCircle },
    { id: "history", label: "Riwayat", icon: History },
    { id: "settings", label: "Pengaturan", icon: Settings },
  ] as const;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#1e2436] border-t border-[#333a52] px-3 py-2 max-w-md mx-auto">
      <div className="flex items-center justify-around">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex flex-col items-center gap-1 rounded-xl px-3 py-1.5 transition ${
                isActive ? "text-amber-400 font-bold bg-amber-500/10" : "text-slate-400 hover:text-slate-200"
              }`}
            >
              <Icon className="h-5 w-5" />
              <span className="text-[10px]">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
