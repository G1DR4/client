// Copyright 2015 The Project Buendia Authors
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at: http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distrib-
// uted under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
// OR CONDITIONS OF ANY KIND, either express or implied.  See the License for
// specific language governing permissions and limitations under the License.

package org.msf.records.events.sync;

import android.support.annotation.Nullable;

/** An event bus event giving details on the last-reported progress of an in-progress sync. */
public class SyncProgressEvent {
    public int progress;
    @Nullable public String label;

    public SyncProgressEvent(int progress) {
        this.progress = progress;
        this.label = null;
    }

    public SyncProgressEvent(int progress, String label) {
        this.progress = progress;
        this.label = label;
    }
}
