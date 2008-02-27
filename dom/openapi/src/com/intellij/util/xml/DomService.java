/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.util.xml;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;
import java.util.Collection;

/**
 * @author Gregory.Shrago
 */
public abstract class DomService {

  public static DomService getInstance() {
    return ServiceManager.getService(DomService.class);
  }

  public List<VirtualFile> getAllFiles(Class<? extends DomFileDescription> description, Project project, final GlobalSearchScope scope) {
    return ContainerUtil.findAll(getAllFiles(description, project), new Condition<VirtualFile>() {
      public boolean value(final VirtualFile file) {
        return scope.contains(file);
      }
    });
  }

  public abstract Collection<VirtualFile> getAllFiles(Class<? extends DomFileDescription> description, Project project);

  public abstract ModelMerger createModelMerger();

  public enum StructureViewMode {
    SHOW, SHOW_CHILDREN, SKIP
  }
  public abstract StructureViewBuilder createSimpleStructureViewBuilder(final XmlFile file, final Function<DomElement, StructureViewMode> modeProvider);
}
