/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.exportablepreferences.processor;


import org.mariotaku.library.exportablepreferences.annotation.ExportablePreference;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(ExportablePreference.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        HashMap<Name, ClassInfo> exportableClasses = new HashMap<>();
        final Elements elements = processingEnv.getElementUtils();
        final Types types = processingEnv.getTypeUtils();

        for (Element element : roundEnv.getElementsAnnotatedWith(ExportablePreference.class)) {
            final VariableElement var = (VariableElement) element;
            final TypeElement type = (TypeElement) var.getEnclosingElement();
            final ClassInfo classInfo = getOrNew(exportableClasses, elements, type);
            classInfo.addField(var);
        }

        final Filer filer = processingEnv.getFiler();
        for (ClassInfo classInfo : exportableClasses.values()) {
            try {
                ExporterClassGenerator exporterClassGenerator = new ExporterClassGenerator(classInfo, elements);
                exporterClassGenerator.saveCursorIndicesFile(filer, elements, types);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return true;
    }

    private ClassInfo getOrNew(HashMap<Name, ClassInfo> cursorObjectClasses, Elements elements,
                               TypeElement type) {
        final Name qualifiedName = type.getQualifiedName();
        ClassInfo info = cursorObjectClasses.get(qualifiedName);
        if (info == null) {
            info = new ClassInfo(elements, type);
            cursorObjectClasses.put(qualifiedName, info);
        }
        return info;
    }
}
