package io.kamon.meta.reporter;

import java.util.Collections;
import java.util.Map;

public class KanelaInfoProvider {
        public static final KanelaInfoProvider MODULE$ = null;

        public Boolean isActive() {
            return false;
        }

        public Map<String, String> modules() {
            return Collections.emptyMap();
        }

        public Map<String, java.util.List<Throwable>> errors() {
            return Collections.emptyMap();
        }


}