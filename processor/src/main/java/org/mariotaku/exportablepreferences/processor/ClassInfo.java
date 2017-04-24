package org.mariotaku.exportablepreferences.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import org.mariotaku.library.exportablepreferences.annotation.ExportablePreference;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by mariotaku on 15/11/27.
 */
public class ClassInfo {

    final Elements elements;
    final TypeElement objectType;

    final ClassName objectClassName;
    final List<ExportableFieldInfo> fieldInfoList;
    TypeName[] exceptions;


    public ClassInfo(Elements elements, TypeElement objectType) {
        this.elements = elements;
        this.objectType = objectType;
        objectClassName = ClassName.get(objectType);
        fieldInfoList = new ArrayList<>();
    }

    public static ClassName getSuffixedClassName(Elements elements, TypeElement cls, String suffix) {
        final String packageName = String.valueOf(elements.getPackageOf(cls).getQualifiedName());
        final String binaryName = String.valueOf(elements.getBinaryName(cls));
        return ClassName.get(packageName, binaryName.substring(packageName.length() + 1) + suffix);
    }

    public ExportableFieldInfo addField(VariableElement field) {
        if (field.getKind() != ElementKind.FIELD) throw new AssertionError();
        final Object constantValue = field.getConstantValue();
        if (!(constantValue instanceof String)) {
            throw new AssertionError();
        }

        final ExportableFieldInfo fieldInfo = new ExportableFieldInfo(elements, field);
        fieldInfoList.add(fieldInfo);
        return fieldInfo;
    }

    public String getPackageName() {
        return objectClassName.packageName();
    }

    public static class ExportableFieldInfo {

        final ExportablePreference annotation;
        final String keyName;

        public ExportableFieldInfo(Elements elements, VariableElement field) {
            annotation = field.getAnnotation(ExportablePreference.class);
            keyName = (String) field.getConstantValue();
        }


    }
}
