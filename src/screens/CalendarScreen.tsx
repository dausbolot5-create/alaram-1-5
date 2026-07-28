import React, { useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import type { Exam } from "../types";
import { deleteExam } from "../store";
import { ExamCard } from "../components/ExamCard";

interface CalendarScreenProps {
  exams: Exam[];
}

export const CalendarScreen: React.FC<CalendarScreenProps> = ({ exams }) => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedIso, setSelectedIso] = useState<string | null>(null);

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  const firstDay = new Date(year, month, 1);
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const startDayOfWeek = (firstDay.getDay() + 6) % 7; // Monday = 0

  const examsByDate = exams.reduce((acc, e) => {
    acc[e.tanggal] = (acc[e.tanggal] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  const gridDays: Array<string | null> = [];
  for (let i = 0; i < startDayOfWeek; i++) gridDays.push(null);
  for (let d = 1; d <= daysInMonth; d++) {
    const iso = `${year}-${String(month + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
    gridDays.push(iso);
  }

  const selectedExams = selectedIso ? exams.filter((e) => e.tanggal === selectedIso) : [];

  const prevMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
    setSelectedIso(null);
  };

  const nextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
    setSelectedIso(null);
  };

  const monthLabel = currentDate.toLocaleDateString("id-ID", { month: "long", year: "numeric" });
  const dayNames = ["Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"];

  return (
    <div className="space-y-4 pb-20">
      <div>
        <h1 className="text-2xl font-black tracking-tight text-white">Kalender Ujian</h1>
        <p className="text-xs text-slate-400 mt-0.5">Titik menandai tanggal berujian</p>
      </div>

      <div className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 text-slate-100 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <button onClick={prevMonth} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-700">
            <ChevronLeft className="h-5 w-5" />
          </button>
          <h2 className="text-sm font-bold capitalize">{monthLabel}</h2>
          <button onClick={nextMonth} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-700">
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>

        <div className="grid grid-cols-7 gap-1 text-center text-xs mb-2">
          {dayNames.map((d) => (
            <span key={d} className="text-[11px] font-semibold text-slate-400 py-1">
              {d}
            </span>
          ))}
        </div>

        <div className="grid grid-cols-7 gap-1">
          {gridDays.map((iso, idx) => {
            if (!iso) return <div key={idx} className="h-9" />;
            const count = examsByDate[iso] || 0;
            const isSelected = selectedIso === iso;
            const dayNum = parseInt(iso.split("-")[2], 10);

            return (
              <button
                key={iso}
                onClick={() => setSelectedIso(count > 0 ? iso : null)}
                className={`flex flex-col items-center justify-center h-9 rounded-xl text-xs font-medium transition ${
                  isSelected
                    ? "bg-amber-500 text-slate-950 font-bold"
                    : "bg-[#1e2436] text-slate-200 hover:bg-slate-700"
                }`}
              >
                <span>{dayNum}</span>
                {count > 0 && (
                  <span
                    className={`h-1 w-1 rounded-full ${
                      isSelected ? "bg-slate-950" : "bg-amber-400"
                    }`}
                  />
                )}
              </button>
            );
          })}
        </div>
      </div>

      <div>
        {selectedIso ? (
          <div className="space-y-3">
            <h3 className="text-xs font-bold text-slate-300">Jadwal Ujian {selectedIso}:</h3>
            {selectedExams.map((e) => (
              <ExamCard key={e.id} exam={e} onDelete={(id) => deleteExam(id)} />
            ))}
          </div>
        ) : (
          <p className="text-xs text-slate-400">Ketuk tanggal bertitik untuk melihat detail ujiannya.</p>
        )}
      </div>
    </div>
  );
};
