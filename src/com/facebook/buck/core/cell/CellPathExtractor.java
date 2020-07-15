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

package com.facebook.buck.core.cell;

import com.facebook.buck.core.cell.exception.UnknownCellException;
import com.facebook.buck.core.cell.name.CanonicalCellName;
import com.facebook.buck.core.filesystems.AbsPath;
import java.util.Optional;

/** Extracts Path from a cell name. */
public interface CellPathExtractor {

  /**
   * @param cellName canonical name of cell.
   * @return Absolute path to the physical location of the cell, or {@code Optional.empty()} if the
   *     cell name cannot be resolved.
   */
  Optional<AbsPath> getCellPath(CanonicalCellName cellName);

  /**
   * @return Absolute path to the physical location of the cell that contains the provided target
   * @throws UnknownCellException if cell is not known
   */
  AbsPath getCellPathOrThrow(CanonicalCellName cellName);
}