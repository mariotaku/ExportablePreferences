package org.mariotaku.library.exportablepreferences;

import android.content.SharedPreferences;

import org.mariotaku.library.exportablepreferences.annotation.PreferenceType;

import java.io.IOException;
import java.util.Set;

/**
 * Created by mariotaku on 2017/4/24.
 */

public abstract class PreferencesExporter {

    public final static String EXPORTER_SUFFIX = "PreferencesExporter";

    public abstract void exportTo(SharedPreferences preferences, ExportHandler handler)
            throws IOException;

    public abstract ImportHandler importTo(SharedPreferences.Editor editor) throws IOException;

    public static PreferencesExporter get(Class<?> cls) {
        try {
            //noinspection unchecked
            Class<PreferencesExporter> indicesClass = (Class<PreferencesExporter>)
                    Class.forName(cls.getName() + EXPORTER_SUFFIX);
            return indicesClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract static class ExportHandler {

        public abstract void onBoolean(String key, boolean value) throws IOException;

        public abstract void onInt(String key, int value) throws IOException;

        public abstract void onLong(String key, long value) throws IOException;

        public abstract void onFloat(String key, float value) throws IOException;

        public abstract void onString(String key, String value) throws IOException;

        public abstract void onStringSet(String key, Set<String> value) throws IOException;

    }

    public abstract static class ImportHandler {

        private final SharedPreferences.Editor editor;

        @PreferenceType
        public abstract int getType(String key);

        protected ImportHandler(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        public final void onBoolean(String key, boolean value) throws IOException {
            if (getType(key) != PreferenceType.BOOLEAN) return;
            editor.putBoolean(key, value);
        }

        public final void onInt(String key, int value) throws IOException {
            if (getType(key) != PreferenceType.INT) return;
            editor.putInt(key, value);
        }

        public final void onLong(String key, long value) throws IOException {
            if (getType(key) != PreferenceType.LONG) return;
            editor.putLong(key, value);
        }

        public final void onFloat(String key, float value) throws IOException {
            if (getType(key) != PreferenceType.FLOAT) return;
            editor.putFloat(key, value);
        }

        public final void onString(String key, String value) throws IOException {
            if (getType(key) != PreferenceType.STRING) return;
            editor.putString(key, value);
        }

        public final void onStringSet(String key, Set<String> value) throws IOException {
            if (getType(key) != PreferenceType.STRING_SET) return;
            editor.putStringSet(key, value);
        }

    }
}
