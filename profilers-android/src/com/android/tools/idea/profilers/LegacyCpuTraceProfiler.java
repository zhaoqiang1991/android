/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.profilers;

import com.android.tools.profiler.proto.Cpu;
import com.android.tools.profiler.proto.CpuProfiler.*;
import java.util.List;

/**
 * An interface to perform method-level tracing using JDWP.
 */
public interface LegacyCpuTraceProfiler {
  CpuProfilingAppStartResponse startProfilingApp(CpuProfilingAppStartRequest request);
  CpuProfilingAppStopResponse stopProfilingApp(CpuProfilingAppStopRequest request);
  List<Cpu.CpuTraceInfo> getTraceInfo(GetTraceInfoRequest request);
}
