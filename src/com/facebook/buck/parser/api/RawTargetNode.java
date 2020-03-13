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

package com.facebook.buck.parser.api;

import com.facebook.buck.core.path.ForwardRelativePath;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.util.collect.TwoArraysImmutableHashMap;
import com.google.common.base.Preconditions;
import java.util.Map;
import javax.annotation.Nullable;

/** What we got from Starlark/Python interpreter. */
@BuckStyleValue
public abstract class RawTargetNode {
  public abstract ForwardRelativePath getBasePath();

  public abstract String getBuckType();

  public abstract TwoArraysImmutableHashMap<String, Object> getAttrs();

  @Nullable
  public Object get(String name) {
    return getAttrs().get(name);
  }

  public static RawTargetNode of(
      ForwardRelativePath basePath,
      String buckType,
      TwoArraysImmutableHashMap<String, Object> attrs) {
    Preconditions.checkArgument(!buckType.isEmpty());
    return ImmutableRawTargetNode.ofImpl(basePath, buckType, attrs);
  }

  public static RawTargetNode copyOf(
      ForwardRelativePath basePath, String buckType, Map<String, Object> attrs) {
    return of(basePath, buckType, TwoArraysImmutableHashMap.copyOf(attrs));
  }
}