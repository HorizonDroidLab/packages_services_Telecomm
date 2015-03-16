/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.server.telecom;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.util.Pair;

import com.android.internal.util.IndentingPrintWriter;

import java.util.HashMap;

/**
 * Searches for and returns connection services.
 */
final class ConnectionServiceRepository {
    private final HashMap<Pair<ComponentName, UserHandle>, ConnectionServiceWrapper> mServiceCache =
            new HashMap<>();
    private final PhoneAccountRegistrar mPhoneAccountRegistrar;
    private final Context mContext;

    private final ServiceBinder.Listener<ConnectionServiceWrapper> mUnbindListener =
            new ServiceBinder.Listener<ConnectionServiceWrapper>() {
                @Override
                public void onUnbind(ConnectionServiceWrapper service) {
                    mServiceCache.remove(service.getComponentName());
                }
            };

    ConnectionServiceRepository(PhoneAccountRegistrar phoneAccountRegistrar, Context context) {
        mPhoneAccountRegistrar = phoneAccountRegistrar;
        mContext = context;
    }

    ConnectionServiceWrapper getService(ComponentName componentName, UserHandle userHandle) {
        Pair<ComponentName, UserHandle> cacheKey = Pair.create(componentName, userHandle);
        ConnectionServiceWrapper service = mServiceCache.get(cacheKey);
        if (service == null) {
            service = new ConnectionServiceWrapper(
                    componentName,
                    this,
                    mPhoneAccountRegistrar,
                    mContext,
                    userHandle);
            service.addListener(mUnbindListener);
            mServiceCache.put(cacheKey, service);
        }
        return service;
    }

    /**
     * Dumps the state of the {@link ConnectionServiceRepository}.
     *
     * @param pw The {@code IndentingPrintWriter} to write the state to.
     */
    public void dump(IndentingPrintWriter pw) {
        pw.println("mServiceCache:");
        pw.increaseIndent();
        for (Pair<ComponentName, UserHandle> cacheKey : mServiceCache.keySet()) {
            ComponentName componentName = cacheKey.first;
            pw.println(componentName);
        }
        pw.decreaseIndent();
    }
}
