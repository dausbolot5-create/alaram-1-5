import { registerPlugin } from "@capacitor/core";

export interface ExactAlarmScheduleOptions {
  /** Stable unique id for this alarm (used for cancel + request code). */
  id: string;
  /** Trigger time in epoch milliseconds. */
  at: number;
  title?: string;
  body?: string;
}

export interface ExactAlarmPlugin {
  schedule(options: ExactAlarmScheduleOptions): Promise<void>;
  cancel(options: { id: string }): Promise<void>;
  cancelAll(): Promise<void>;
  stopAlarm(): Promise<void>;
  isExactAlarmAllowed(): Promise<{ value: boolean }>;
  requestExactAlarmPermission(): Promise<void>;
  hasIgnoreBatteryOptimizations(): Promise<{ value: boolean }>;
  requestIgnoreBatteryOptimizations(): Promise<void>;
  openBatteryOptimizationSettings(): Promise<void>;
  openAppSettings(): Promise<void>;
}

export const ExactAlarm = registerPlugin<ExactAlarmPlugin>("ExactAlarm");
