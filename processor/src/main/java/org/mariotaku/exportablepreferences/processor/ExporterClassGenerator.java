package org.mariotaku.exportablepreferences.processor;

import android.content.SharedPreferences;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import org.mariotaku.library.exportablepreferences.PreferencesExporter;
import org.mariotaku.library.exportablepreferences.annotation.PreferenceType;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

/**
 * Created by mariotaku on 15/11/28.
 */
public class ExporterClassGenerator {

    private final ClassInfo objectClassInfo;
    private final ClassName indicesClassName;
    private final String indicesClassNameWithoutPackage;

    ExporterClassGenerator(ClassInfo objectClassInfo, Elements elements) {
        this.objectClassInfo = objectClassInfo;
        final String packageName = String.valueOf(elements.getPackageOf(objectClassInfo.objectType).getQualifiedName());
        final String binaryName = String.valueOf(elements.getBinaryName(objectClassInfo.objectType));
        indicesClassNameWithoutPackage = binaryName.substring(packageName.length() + 1) + PreferencesExporter.EXPORTER_SUFFIX;
        indicesClassName = ClassName.get(packageName, indicesClassNameWithoutPackage);
    }

    void writeContent(Appendable appendable, Elements elements, Types types) throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(indicesClassNameWithoutPackage);

        builder.superclass(ClassName.get(PreferencesExporter.class));
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        builder.addMethod(createExportToMethod());
        builder.addMethod(createImportToMethod());

        builder.addType(createImportHandler());
        JavaFile.builder(objectClassInfo.getPackageName(), builder.build()).build().writeTo(appendable);
    }

    private MethodSpec createExportToMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("exportTo");
        builder.addAnnotation(Override.class);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addParameter(SharedPreferences.class, "preferences");
        builder.addParameter(PreferencesExporter.ExportHandler.class, "handler");
        builder.addException(IOException.class);

        for (ClassInfo.ExportableFieldInfo info : objectClassInfo.fieldInfoList) {
            builder.beginControlFlow("if (preferences.contains($S))", info.keyName);
            switch (info.annotation.value()) {
                case PreferenceType.BOOLEAN: {
                    builder.addStatement("handler.onBoolean($S, preferences.getBoolean($S, false))",
                            info.keyName, info.keyName);
                    break;
                }
                case PreferenceType.INT: {
                    builder.addStatement("handler.onInt($S, preferences.getInt($S, 0))",
                            info.keyName, info.keyName);
                    break;
                }
                case PreferenceType.LONG: {
                    builder.addStatement("handler.onLong($S, preferences.getLong($S, 0))",
                            info.keyName, info.keyName);
                    break;
                }
                case PreferenceType.FLOAT: {
                    builder.addStatement("handler.onFloat($S, preferences.getFloat($S, 0))",
                            info.keyName, info.keyName);
                    break;
                }
                case PreferenceType.STRING: {
                    builder.addStatement("handler.onString($S, preferences.getString($S, null))",
                            info.keyName, info.keyName);
                    break;
                }
                case PreferenceType.STRING_SET: {
                    builder.addStatement("handler.onStringSet($S, preferences.getStringSet($S, null))",
                            info.keyName, info.keyName);
                    break;
                }
            }
            builder.endControlFlow();
        }
        return builder.build();
    }


    private MethodSpec createImportToMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("importTo");
        builder.addAnnotation(Override.class);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addParameter(SharedPreferences.Editor.class, "editor");
        builder.returns(PreferencesExporter.ImportHandler.class);
        builder.addException(IOException.class);
        builder.addStatement("return new ImportHandlerImpl(editor)");
        return builder.build();
    }

    private TypeSpec createImportHandler() {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("ImportHandlerImpl");
        builder.addModifiers(Modifier.STATIC);
        builder.superclass(PreferencesExporter.ImportHandler.class);
        builder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(SharedPreferences.Editor.class, "editor")
                .addStatement("super(editor)").build());
        builder.addMethod(createGetTypeMethod());
        return builder.build();
    }

    private MethodSpec createGetTypeMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("getType");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addAnnotation(Override.class);
        builder.addParameter(String.class, "key");
        builder.returns(int.class);
        builder.beginControlFlow("switch (key)");
        for (ClassInfo.ExportableFieldInfo info : objectClassInfo.fieldInfoList) {
            String typeName = "";

            switch (info.annotation.value()) {
                case PreferenceType.BOOLEAN: {
                    typeName = "BOOLEAN";
                    break;
                }
                case PreferenceType.INT: {
                    typeName = "INT";
                    break;
                }
                case PreferenceType.LONG: {
                    typeName = "LONG";
                    break;
                }
                case PreferenceType.FLOAT: {
                    typeName = "FLOAT";
                    break;
                }
                case PreferenceType.STRING: {
                    typeName = "STRING";
                    break;
                }
                case PreferenceType.STRING_SET: {
                    typeName = "STRING_SET";
                    break;
                }
            }
            builder.addStatement("case $S: return $T.$L", info.keyName, PreferenceType.class, typeName);
        }
        builder.addStatement("default: return 0");
        builder.endControlFlow();
        return builder.build();
    }

    public void saveCursorIndicesFile(Filer filer, Elements elements, Types types) throws IOException {
        JavaFileObject fileObj = filer.createSourceFile(indicesClassName.toString());
        try (Writer writer = fileObj.openWriter()) {
            writeContent(writer, elements, types);
            writer.flush();
        }
    }
}
