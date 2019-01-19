package io.kamon.meta.reporter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KanelaInfoProvider {
    public static Boolean isKanelaEnabled() {
        return false;
    }

    public static Map<String, String> getInstrumentationModulesInfo() {
        return Collections.emptyMap();
    }

    public static Map<String, List<Throwable>> getKanelaErrors() {
        return Collections.emptyMap();
    }

}