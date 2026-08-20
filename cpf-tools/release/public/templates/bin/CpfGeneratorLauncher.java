import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

/** CPF Public Binary Repository의 OS별 Generator 배포본을 검증 후 실행하는 thin launcher입니다. */
public final class CpfGeneratorLauncher {
    public static void main(String[] args) throws Exception {
        Path root=Path.of(".").toAbsolutePath().normalize(); List<String> forwarded=new ArrayList<>();
        for(int i=0;i<args.length;i++){if(("--root".equals(args[i])||"--workspace".equals(args[i]))&&i+1<args.length){root=Path.of(args[++i]).toAbsolutePath().normalize();}else forwarded.add(args[i]);}
        String version=required("CPF_VERSION"); String repo=required("CPF_MAVEN_REPOSITORY_URL").replaceAll("/+$","");
        String classifier=classifier(); String stem="cpf-generator-cli-"+version+"-"+classifier;
        Path cache=root.resolve("build/cpf-tools/generator").resolve(version).resolve(classifier); Files.createDirectories(cache);
        Path executable=cache.resolve(classifier.startsWith("windows-")?"cpf-generator.exe":"cpf-generator");
        if(!Files.isRegularFile(executable)){
            byte[] zip=download(repo+"/com/cpf/tooling/cpf-generator-cli/"+version+"/"+stem+".zip");
            String expected=new String(download(repo+"/com/cpf/tooling/cpf-generator-cli/"+version+"/"+stem+".zip.sha256"),StandardCharsets.US_ASCII).trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
            String actual=hex(MessageDigest.getInstance("SHA-256").digest(zip)); if(!actual.equals(expected))throw new SecurityException("CPF Generator checksum mismatch classifier="+classifier);
            Path archive=cache.resolve(stem+".zip"); Files.write(archive,zip); unzip(archive,cache); Files.deleteIfExists(archive);
            if(!Files.isRegularFile(executable))throw new IllegalStateException("Generator executable missing after extraction: "+executable);
            if(!classifier.startsWith("windows-"))executable.toFile().setExecutable(true,true);
        }
        List<String> cmd=new ArrayList<>();cmd.add(executable.toString());cmd.add("--root");cmd.add(root.toString());cmd.addAll(forwarded);
        ProcessBuilder pb=new ProcessBuilder(cmd).directory(root.toFile()).inheritIO();Process p=pb.start();if(!p.waitFor(1800,TimeUnit.SECONDS)){p.destroyForcibly();throw new IllegalStateException("Generator timeout");}System.exit(p.exitValue());
    }
    static byte[] download(String url)throws Exception{URI uri=URI.create(url);if("file".equalsIgnoreCase(uri.getScheme()))return Files.readAllBytes(Path.of(uri));HttpRequest r=HttpRequest.newBuilder(uri).timeout(java.time.Duration.ofMinutes(2)).GET().build();HttpResponse<byte[]> x=HttpClient.newHttpClient().send(r,HttpResponse.BodyHandlers.ofByteArray());if(x.statusCode()!=200)throw new IOException("Generator artifact download failed status="+x.statusCode()+" url="+url);return x.body();}
    static void unzip(Path zip,Path target)throws IOException{try(ZipInputStream in=new ZipInputStream(Files.newInputStream(zip))){for(var e=in.getNextEntry();e!=null;e=in.getNextEntry()){Path out=target.resolve(e.getName()).normalize();if(!out.startsWith(target))throw new SecurityException("Unsafe generator zip entry");if(e.isDirectory())Files.createDirectories(out);else{Files.createDirectories(out.getParent());Files.copy(in,out,StandardCopyOption.REPLACE_EXISTING);}}}}
    static String classifier(){String os=System.getProperty("os.name","").toLowerCase(Locale.ROOT),arch=System.getProperty("os.arch","").toLowerCase(Locale.ROOT);String o=os.contains("win")?"windows":os.contains("linux")?"linux":null;String a=Set.of("amd64","x86_64").contains(arch)?"x64":Set.of("aarch64","arm64").contains(arch)?"arm64":null;if(o==null||a==null)throw new IllegalStateException("Unsupported Generator platform os="+os+" arch="+arch);return o+"-"+a;}
    static String required(String name){String v=System.getenv(name);if(v==null||v.isBlank())throw new IllegalStateException(name+" 환경변수가 필요합니다.");return v.trim();}
    static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}
}
