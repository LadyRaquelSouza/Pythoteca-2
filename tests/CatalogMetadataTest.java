import br.com.pythoteca.experimental.reader.catalog.CatalogMetadata;
import java.util.*;
import java.security.MessageDigest;

public class CatalogMetadataTest {
    static final byte[] DATA = new byte[]{1,2,3};
    static String hash(byte[] data) throws Exception {
        StringBuilder s = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(data))
            s.append(String.format("%02x", b & 255));
        return s.toString();
    }
    static Map<String,Object> row(String id, String discipline) throws Exception {
        Map<String,Object> r = new HashMap<>();
        r.put("id",id); r.put("title","Introdução à lógica"); r.put("subtitle","Exercícios");
        r.put("sha256",hash(DATA)); r.put("bytes",DATA.length);
        if (discipline != null) r.put("discipline",discipline);
        return r;
    }
    static Map<String,Object> index(int schema, List<?> rows) {
        return Map.of("schema_version",schema,"lessons",rows);
    }
    static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    static void reject(Runnable r) {
        try { r.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Invalid metadata accepted");
    }
    public static void main(String[] args) throws Exception {
        var legacy = row("aula01",null);
        var demo = row("aula99",null);
        var java = row("aula301","java");
        var portugol = row("aula201","portugol");
        var old = index(1,List.of(legacy,demo));
        var current = index(2,List.of(java,portugol));
        var result = CatalogMetadata.validate(old,current);
        check(result.size()==3);
        check(result.get(0).path.equals("catalog/aula01.json") && result.get(0).discipline.equals("python"));
        check(result.get(1).path.equals("modules/aula301.json") && result.get(2).id.equals("aula201"));
        check(result.get(0).matches(DATA));
        check(!result.get(0).matches(new byte[]{1,2,4}));
        check(!result.get(0).matches(new byte[]{1,2}));
        try { result.clear(); throw new AssertionError(); } catch (UnsupportedOperationException expected) {}
        java.put("title","Changed");
        check(!result.get(1).title.equals("Changed"));
        reject(() -> CatalogMetadata.validate(current,old));
        reject(() -> CatalogMetadata.validate(old,index(2,List.of(java,java))));
        var collision = row("aula01","java");
        reject(() -> CatalogMetadata.validate(old,index(2,List.of(collision))));
        for (String id : List.of("../aula01","aula01/","aula%2f01","aula1","aula123456789")) {
            var bad = row(id,"java");
            reject(() -> CatalogMetadata.validate(old,index(2,List.of(bad))));
        }
        for (Object size : List.of(0,-1,4194305,3.0,"3",Long.MAX_VALUE)) {
            var bad = row("aula301","java"); bad.put("bytes",size);
            reject(() -> CatalogMetadata.validate(old,index(2,List.of(bad))));
        }
        for (String field : List.of("sha256","title","subtitle","discipline")) {
            var bad = row("aula301","java");
            bad.put(field,field.equals("subtitle") ? "x".repeat(513) : "");
            reject(() -> CatalogMetadata.validate(old,index(2,List.of(bad))));
        }
        var bad = row("aula301","java"); bad.put("url","https://example.invalid");
        reject(() -> CatalogMetadata.validate(old,index(2,List.of(bad))));
        var invalidDemo = row("aula99",null); invalidDemo.put("bytes",0);
        reject(() -> CatalogMetadata.validate(index(1,List.of(invalidDemo)),current));
        byte[] large = new byte[CatalogMetadata.MAX_LESSON_BYTES];
        large[large.length-1] = 42;
        var expanded = row("aula301","java");
        expanded.put("bytes",large.length); expanded.put("sha256",hash(large));
        check(CatalogMetadata.validate(old,index(2,List.of(expanded))).get(1).matches(large));
        large[0] = 1;
        check(!CatalogMetadata.validate(old,index(2,List.of(expanded))).get(1).matches(large));
        List<Map<String,Object>> many = new ArrayList<>();
        for (int i=0;i<CatalogMetadata.MAX_LESSONS;i++) many.add(row("aula"+(10000+i),"java"));
        check(CatalogMetadata.validate(index(1,List.of()),index(2,many)).size()==2048);
        reject(() -> CatalogMetadata.validate(old,index(2,many)));
        System.out.println("CATALOG_METADATA_OK");
    }
}
