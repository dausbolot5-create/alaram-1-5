import React, { useState, useEffect, useRef } from "react";
import type { Exam, AppData } from "./types";
import { loadData, subscribe, setExamStatus } from "./store";
import { syncNotifications } from "./notifications";
import { LocalNotifications } from "@capacitor/local-notifications";
import { Navigation, type TabType } from "./components/Navigation";
import { AlarmHost } from "./components/AlarmHost";
import { DashboardScreen } from "./screens/DashboardScreen";
import { CalendarScreen } from "./screens/CalendarScreen";
import { UploadScreen } from "./screens/UploadScreen";
import { HistoryScreen } from "./screens/HistoryScreen";
import { SettingsScreen } from "./screens/SettingsScreen";

import { App as CapApp } from '@capacitor/app';

export function App() {
  const [data, setData] = useState<AppData>(loadData());
  const [activeTab, setActiveTab] = useState<TabType>("dashboard");
  const [activeAlarmExam, setActiveAlarmExam] = useState<Exam | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    return subscribe(() => {
      const freshData = loadData();
      setData(freshData);
      syncNotifications(freshData); // sync on every store update
    });
  }, []);

  // Handle capacitor notification tap action to bring app to foreground
  useEffect(() => {
    LocalNotifications.addListener("localNotificationActionPerformed", (notification) => {
      // Find the associated exam id from the generated numerical id/title mapping if needed
      // Actually we just rely on the setInterval to trigger the AlarmHost overlay 
      // but listening to this ensures the app is launched and we don't miss the check
      syncNotifications(loadData());
    });
  }, []);
  useEffect(() => {
    syncNotifications(data);
    CapApp.addListener('appStateChange', ({ isActive }) => {
      if (isActive) {
        syncNotifications(loadData());
      }
    });
  }, []);

  // Audio lifecycle
  useEffect(() => {
    if (activeAlarmExam && data.settings.suaraAlarm && audioRef.current) {
      audioRef.current.loop = true;
      audioRef.current.play().catch(e => console.warn("Audio autoplay blocked", e));
    } else if (!activeAlarmExam && audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
    }
  }, [activeAlarmExam, data.settings.suaraAlarm]);

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
      {/* 
        ponytail: using a remote mp3 file for alarm sound so we don't need to add asset bundler logic for Android 
        Update: add raw local file if offline strict is needed 
      */}
      <audio ref={audioRef} src="https://actions.google.com/sounds/v1/alarms/alarm_clock.ogg" preload="auto" />
    </div>
  );
}

export default App;
