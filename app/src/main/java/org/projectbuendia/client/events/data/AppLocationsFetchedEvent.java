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

package org.projectbuendia.client.events.data;

import org.projectbuendia.client.data.app.AppLocation;
import org.projectbuendia.client.data.app.TypedCursor;
import org.projectbuendia.client.events.DefaultCrudEventBus;

/**
 * An event bus event indicating that {@link AppLocation}s have been fetched from the data store.
 *
 * <p>This event should only be posted on a {@link DefaultCrudEventBus}.
 */
public class AppLocationsFetchedEvent extends TypedCursorFetchedEvent<AppLocation> {

    AppLocationsFetchedEvent(TypedCursor<AppLocation> cursor) {
        super(cursor);
    }
}

