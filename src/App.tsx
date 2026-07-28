import React, { useState, useEffect } from "react";
import type { Exam, AppData } from "./types";
import { loadData, subscribe, setExamStatus } from "./store";
import { Navigation, type TabType } from "./components/Navigation";
import { AlarmHost } from "./components/AlarmHost";
import { DashboardScreen } from "./screens/DashboardScreen";
import { CalendarScreen } from "./screens/CalendarScreen";
import { UploadScreen } from "./screens/UploadScreen";
import { HistoryScreen } from "./screens/HistoryScreen";
import { SettingsScreen } from "./screens/SettingsScreen";

export function App() {
  const [data, setData] = useState<AppData>(loadData());
  const [activeTab, setActiveTab] = useState<TabType>("dashboard");
  const [activeAlarmExam, setActiveAlarmExam] = useState<Exam | null>(null);

  useEffect(() => {
    return subscribe(() => setData(loadData()));
  }, []);

  // Check alarm trigger loop
  useEffect(() => {
    const interval = setInterval(() => {
      const now = Date.now();
      for (const t of data.triggers) {
        if (!t.sudah_bunyi && now >= t.waktu && now - t.waktu < 30 * 60 * 1000) {
          const exam = data.exams.find((e) => e.id === t.exam_id);
          if (exam) {
            setActiveAlarmExam(exam);
            break;
          }
        }
      }
    }, 5000);

    return () => clearInterval(interval);
  }, [data]);

  const handleDismissAlarm = () => {
    if (activeAlarmExam) {
      setExamStatus(activeAlarmExam.id, "ongoing");
      setActiveAlarmExam(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#131726] text-slate-100 flex justify-center">
      <main className="w-full max-w-md min-h-screen bg-[#131726] px-4 pt-6 pb-24 shadow-2xl relative">
        {activeTab === "dashboard" && (
          <DashboardScreen
            exams={data.exams}
            onNavigateToUpload={() => setActiveTab("upload")}
          />
        )}
        {activeTab === "calendar" && <CalendarScreen exams={data.exams} />}
        {activeTab === "upload" && (
          <UploadScreen onNavigateHome={() => setActiveTab("dashboard")} />
        )}
        {activeTab === "history" && <HistoryScreen exams={data.exams} />}
        {activeTab === "settings" && (
          <SettingsScreen settings={data.settings} exams={data.exams} />
        )}

        <Navigation activeTab={activeTab} onTabChange={setActiveTab} />
      </main>

      <AlarmHost activeAlarmExam={activeAlarmExam} onDismiss={handleDismissAlarm} />
    </div>
  );
}

export default App;
