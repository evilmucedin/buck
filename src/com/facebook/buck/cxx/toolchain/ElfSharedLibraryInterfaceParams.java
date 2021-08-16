/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.cxx.toolchain;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.toolchain.toolprovider.ToolProvider;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.google.common.collect.ImmutableList;

@BuckStyleValue
public abstract class ElfSharedLibraryInterfaceParams implements SharedLibraryInterfaceParams {

  public static ElfSharedLibraryInterfaceParams of(
      ToolProvider objcopy,
      ImmutableList<String> ldflags,
      boolean removeUndefinedSymbols,
      boolean objcopyRecalculatesLayout) {
    return ImmutableElfSharedLibraryInterfaceParams.ofImpl(
        objcopy, ldflags, removeUndefinedSymbols, objcopyRecalculatesLayout);
  }

  public abstract ToolProvider getObjcopy();

  @Override
  public abstract ImmutableList<String> getLdflags();

  public abstract boolean isRemoveUndefinedSymbols();

  public abstract boolean doesObjcopyRecalculateLayout();

  @Override
  public Iterable<BuildTarget> getParseTimeDeps(TargetConfiguration targetConfiguration) {
    return getObjcopy().getParseTimeDeps(targetConfiguration);
  }

  @Override
  public Kind getKind() {
    return Kind.ELF;
  }
}
