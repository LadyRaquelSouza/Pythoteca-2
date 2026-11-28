package br.com.pythoteca.experimental.reader.catalog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Validates decoded metadata only. This is not an authenticity boundary. */
public final class CatalogMetadata {
    public static final int MAX_LESSONS = 2048;
    public static final int MAX_LESSON_BYTES = 4 * 1024 * 1024;
    public static final int MAX_TITLE = 240;
    public static final int MAX_SUBTITLE = 512;
    private static final Pattern ID = Pattern.compile("aula[0-9]{2,8}");
    private static final Pattern HASH = Pattern.compile("[0-9a-f]{64}");
    private static final Set<String> V2_DISCIPLINES = Set.of("arquitetura", "portugol", "java",
        "sistemas_operacionais", "ti", "modelagem_dados");
    private static final Set<String> V1_KEYS = Set.of("id", "title", "subtitle", "sha256", "bytes");
    private static final Set<String> V2_KEYS = Set.of("id", "title", "subtitle", "sha256", "bytes", "discipline");

    private CatalogMetadata() {}

    public static final class Lesson {
        public final String id, title, subtitle, discipline, sha256, path;
        public final int schema, bytes;
        private Lesson(int schema, String id, String title, String subtitle,
                       String discipline, String sha256, int bytes) {
            this.schema = schema;
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.discipline = discipline;
            this.sha256 = sha256;
            this.bytes = bytes;
            this.path = (schema == 1 ? "catalog/" : "modules/") + id + ".json";
        }
        public boolean isDemo() { return schema == 1 && id.equals("aula99"); }

        /** Verify actual bytes before decoding; the caller must authenticate metadata separately. */
        public boolean matches(byte[] content) {
            if (content == null || content.length != bytes) return false;
            try {
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
                byte[] expected = new byte[32];
                for (int i = 0; i < expected.length; i++)
                    expected[i] = (byte) Integer.parseInt(sha256.substring(2*i, 2*i+2), 16);
                return MessageDigest.isEqual(digest, expected);
            } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        }
    }

    /** Explicit legacy/current slots prevent an index from choosing its own directory. */
    public static List<Lesson> validate(Map<?, ?> legacy, Map<?, ?> current) {
        List<?> a = entries(legacy, 1), b = entries(current, 2);
        if (a.size() > MAX_LESSONS || b.size() > MAX_LESSONS ||
                a.size() + b.size() > MAX_LESSONS) throw invalid("Too many lessons");
        Set<String> seen = new HashSet<>();
        List<Lesson> output = new ArrayList<>();
        append(a, 1, seen, output);
        append(b, 2, seen, output);
        return Collections.unmodifiableList(output);
    }

    private static List<?> entries(Map<?, ?> index, int schema) {
        if (index == null || !index.keySet().equals(Set.of("schema_version", "lessons")) ||
                integer(index.get("schema_version")) != schema ||
                !(index.get("lessons") instanceof List<?>)) throw invalid("Invalid index");
        return (List<?>) index.get("lessons");
    }

    private static void append(List<?> entries, int schema, Set<String> seen, List<Lesson> output) {
        for (Object entry : entries) {
            if (!(entry instanceof Map<?, ?>)) throw invalid("Invalid lesson");
            Map<?, ?> row = (Map<?, ?>) entry;
            if (!row.keySet().equals(schema == 1 ? V1_KEYS : V2_KEYS)) throw invalid("Unexpected fields");
            String id = string(row.get("id"), 12, false);
            String title = string(row.get("title"), MAX_TITLE, false);
            String subtitle = string(row.get("subtitle"), MAX_SUBTITLE, true);
            String hash = string(row.get("sha256"), 64, false);
            String discipline = schema == 1 ? "python" : string(row.get("discipline"), 64, false);
            long size = integer(row.get("bytes"));
            if (!ID.matcher(id).matches() || !seen.add(id)) throw invalid("Invalid or duplicate ID");
            if (!HASH.matcher(hash).matches()) throw invalid("Invalid digest");
            if (schema == 2 && !V2_DISCIPLINES.contains(discipline)) throw invalid("Unsupported discipline");
            if (size < 1 || size > MAX_LESSON_BYTES) throw invalid("Invalid lesson size");
            Lesson lesson = new Lesson(schema, id, title, subtitle, discipline, hash, (int) size);
            if (!lesson.isDemo()) output.add(lesson);
        }
    }

    private static long integer(Object value) {
        if (value instanceof Integer || value instanceof Long) return ((Number) value).longValue();
        throw invalid("Expected integer");
    }

    private static String string(Object value, int max, boolean emptyAllowed) {
        if (!(value instanceof String)) throw invalid("Expected string");
        String s = (String) value;
        if (s.length() > max || (!emptyAllowed && s.trim().isEmpty())) throw invalid("Invalid text length");
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isISOControl(ch)) throw invalid("Control character");
            if (Character.isHighSurrogate(ch)) {
                if (++i >= s.length() || !Character.isLowSurrogate(s.charAt(i))) throw invalid("Invalid Unicode");
            } else if (Character.isLowSurrogate(ch)) throw invalid("Invalid Unicode");
        }
        return s;
    }

    private static IllegalArgumentException invalid(String reason) {
        return new IllegalArgumentException(reason);
    }
}
