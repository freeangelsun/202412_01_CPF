package com.cpf.core.api.security.network;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * DNS 조회 없이 Literal IPv4/IPv6와 CIDR을 검증하는 공용 Network 정책입니다.
 *
 * <p>설정 단계에서는 Hostname의 암묵 허용을 금지하고, DNS가 필요한 Endpoint는 TLS와
 * 명시적 허용을 요구합니다. DNS Endpoint의 실제 resolved address는 연결 직전에
 * {@link #validateResolvedAddresses(String, Collection)}로 다시 검사하여 DNS rebinding을 차단합니다.</p>
 */
public final class CpfNetworkEndpointPolicy {
    private static final Set<Integer> DEFAULT_TLS_PORTS = Set.of(443, 8443, 9443);
    private final List<Cidr> allowedCidrs;
    private final Set<Integer> allowedPorts;
    private final boolean allowPrivate;
    private final boolean allowPublic;
    private final boolean allowDns;
    private final boolean requireTls;

    /** Source-compatible constructor: public routable addresses remain allowed. */
    public CpfNetworkEndpointPolicy(Collection<String> allowedCidrs, Collection<Integer> allowedPorts,
                                    boolean allowPrivate, boolean allowDns, boolean requireTls) {
        this(allowedCidrs, allowedPorts, allowPrivate, true, allowDns, requireTls);
    }

    public CpfNetworkEndpointPolicy(Collection<String> allowedCidrs, Collection<Integer> allowedPorts,
                                    boolean allowPrivate, boolean allowPublic, boolean allowDns, boolean requireTls) {
        this.allowedCidrs = allowedCidrs == null ? List.of() : allowedCidrs.stream().map(Cidr::parse).toList();
        this.allowedPorts = allowedPorts == null || allowedPorts.isEmpty() ? DEFAULT_TLS_PORTS : Set.copyOf(allowedPorts);
        if (this.allowedPorts.stream().anyMatch(port -> port == null || port < 1 || port > 65_535)) {
            throw new IllegalArgumentException("endpoint port allowlist 범위 오류");
        }
        if (!allowPrivate && !allowPublic) throw new IllegalArgumentException("private/public address가 모두 금지됐습니다.");
        this.allowPrivate = allowPrivate;
        this.allowPublic = allowPublic;
        this.allowDns = allowDns;
        this.requireTls = requireTls;
    }

    public static CpfNetworkEndpointPolicy secureDefault() {
        return new CpfNetworkEndpointPolicy(List.of(), DEFAULT_TLS_PORTS, false, true, false, true);
    }

    public EndpointDecision validateEndpoint(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) throw new IllegalArgumentException("endpoint URL 필수");
        URI uri;
        try { uri = URI.create(rawUrl.trim()); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("endpoint URL 형식 오류", ex); }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!(scheme.equals("https") || (!requireTls && scheme.equals("http")))) {
            throw new IllegalArgumentException("endpoint scheme은 TLS 정책에 맞는 http(s)만 허용");
        }
        if (uri.getUserInfo() != null) throw new IllegalArgumentException("endpoint user-info 금지");
        String host = uri.getHost();
        if (host == null || host.isBlank()) throw new IllegalArgumentException("endpoint host 필수");
        int port = uri.getPort() > 0 ? uri.getPort() : (scheme.equals("https") ? 443 : 80);
        if (!allowedPorts.contains(port)) throw new IllegalArgumentException("endpoint port가 allowlist에 없습니다: " + port);
        if (isLiteral(host)) {
            Address address = Address.parse(host);
            validateAddress(address);
            return new EndpointDecision(uri, true, false, List.of(address.canonical()));
        }
        if (!allowDns) throw new IllegalArgumentException("Hostname은 명시적 allowDns 정책 없이 허용되지 않습니다.");
        if (!scheme.equals("https")) throw new IllegalArgumentException("DNS Endpoint는 TLS가 필수입니다.");
        return new EndpointDecision(uri, false, true, List.of());
    }

    public List<String> validateResolvedAddresses(String hostname, Collection<String> resolvedAddresses) {
        if (!allowDns) throw new IllegalArgumentException("DNS 사용이 비활성화돼 있습니다.");
        if (hostname == null || hostname.isBlank() || isLiteral(hostname)) throw new IllegalArgumentException("DNS hostname 형식 오류");
        if (resolvedAddresses == null || resolvedAddresses.isEmpty()) throw new IllegalArgumentException("DNS resolved address가 없습니다.");
        List<String> result = new ArrayList<>();
        for (String value : resolvedAddresses) {
            Address address = Address.parse(value);
            validateAddress(address);
            result.add(address.canonical());
        }
        return List.copyOf(result);
    }

    public boolean contains(String cidr, String literalAddress) { return Cidr.parse(cidr).contains(Address.parse(literalAddress)); }

    private void validateAddress(Address address) {
        if (address.specialUseDenied()) throw new IllegalArgumentException("Loopback/Link-local/Metadata/Reserved address 금지: " + address.canonical());
        if (address.privateAddress() && !allowPrivate) throw new IllegalArgumentException("Private address 금지: " + address.canonical());
        if (!address.privateAddress() && !allowPublic) throw new IllegalArgumentException("Public address 금지: " + address.canonical());
        if (!allowedCidrs.isEmpty() && allowedCidrs.stream().noneMatch(cidr -> cidr.contains(address))) {
            throw new IllegalArgumentException("address가 CIDR allowlist에 없습니다: " + address.canonical());
        }
    }

    private static boolean isLiteral(String host) {
        String value = stripBrackets(host);
        return value.indexOf(':') >= 0 || value.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}");
    }
    private static String stripBrackets(String value) { return value.startsWith("[") && value.endsWith("]") ? value.substring(1, value.length()-1) : value; }

    public record EndpointDecision(URI uri, boolean literalAddress, boolean resolveBeforeConnect, List<String> validatedAddresses) {}

    public record Cidr(Address network, int prefixLength) {
        public static Cidr parse(String value) {
            if (value == null || value.isBlank() || !value.contains("/")) throw new IllegalArgumentException("CIDR 형식 오류");
            String[] parts=value.trim().split("/",-1); if(parts.length!=2)throw new IllegalArgumentException("CIDR 형식 오류");
            Address address=Address.parse(parts[0]); int prefix;
            try{prefix=Integer.parseInt(parts[1]);}catch(NumberFormatException ex){throw new IllegalArgumentException("CIDR prefix 오류",ex);}
            if(prefix<0||prefix>address.bytes().length*8)throw new IllegalArgumentException("CIDR prefix 범위 오류");
            byte[] masked=mask(address.bytes(),prefix); return new Cidr(new Address(masked),prefix);
        }
        public boolean contains(Address address) {
            if(address.bytes().length!=network.bytes().length)return false;
            return Arrays.equals(mask(address.bytes(),prefixLength),network.bytes());
        }
        private static byte[] mask(byte[] source,int prefix){byte[] out=source.clone();int full=prefix/8,bits=prefix%8;if(full<out.length){
                if(bits>0)out[full]=(byte)(out[full]&(0xff<<(8-bits)));for(int i=full+(bits>0?1:0);i<out.length;i++)out[i]=0;}return out;}
    }

    public record Address(byte[] bytes) {
        public Address { bytes=bytes.clone(); if(bytes.length!=4&&bytes.length!=16)throw new IllegalArgumentException("IP 길이 오류"); }
        @Override
        public byte[] bytes(){return bytes.clone();}
        public static Address parse(String raw) {
            if(raw==null||raw.isBlank())throw new IllegalArgumentException("Literal IP 필수");
            String value=stripBrackets(raw.trim()); if(value.contains("%"))throw new IllegalArgumentException("IPv6 zone identifier 금지");
            return value.contains(":")?new Address(parseIpv6(value)):new Address(parseIpv4(value));
        }
        public String canonical(){if(bytes.length==4)return (bytes[0]&255)+"."+(bytes[1]&255)+"."+(bytes[2]&255)+"."+(bytes[3]&255);StringBuilder b=new StringBuilder();for(int i=0;i<8;i++){
                if(i>0)b.append(':');b.append(Integer.toHexString(((bytes[i*2]&255)<<8)|(bytes[i*2+1]&255)));}return b.toString();}
        public boolean privateAddress() {
            if (bytes.length == 4) {
                int a=bytes[0]&255,b=bytes[1]&255;
                return a==10 || (a==100&&b>=64&&b<=127) || (a==172&&b>=16&&b<=31) || (a==192&&b==168);
            }
            int first=bytes[0]&255;
            return (first&0xfe)==0xfc;
        }
        public boolean specialUseDenied() {
            if (bytes.length == 4) {
                int a=bytes[0]&255,b=bytes[1]&255,c=bytes[2]&255,d=bytes[3]&255;
                return a==0 || a==127 || (a==169&&b==254) || (a==192&&b==0)
                        || (a==192&&b==0&&c==2) || (a==198&&(b==18||b==19))
                        || (a==198&&b==51&&c==100) || (a==203&&b==0&&c==113)
                        || a>=224 || (a==169&&b==254&&c==169&&d==254) || (a==100&&b==100&&c==100&&d==200);
            }
            int first=bytes[0]&255,second=bytes[1]&255;
            boolean unspecified=true;for(byte x:bytes)unspecified&=x==0;
            boolean loopback=!unspecified;for(int i=0;i<15;i++)loopback&=bytes[i]==0;loopback&=bytes[15]==1;
            boolean link=first==0xfe&&(second&0xc0)==0x80;
            boolean multicast=first==0xff;
            boolean doc=first==0x20&&second==0x01&&(bytes[2]&255)==0x0d&&(bytes[3]&255)==0xb8;
            boolean metadata=first==0xfd&&(bytes[1]&255)==0x00&&(bytes[2]&255)==0x0e&&(bytes[3]&255)==0xc2;
            return unspecified||loopback||link||multicast||doc||metadata;
        }
        /** @deprecated use privateAddress/specialUseDenied for explicit policy decisions. */
        @Deprecated
        public boolean blockedByDefault(){ return privateAddress() || specialUseDenied(); }
        private static byte[] parseIpv4(String value){String[] p=value.split("\\.",-1);if(p.length!=4)throw new IllegalArgumentException("IPv4 형식 오류");byte[] out=new byte[4];for(int i=0;i<4;
                i++){if(!p[i].matches("0|[1-9][0-9]{0,2}"))throw new IllegalArgumentException("IPv4 octet 형식 오류");int n=Integer.parseInt(p[i]);if(n>255)throw new
                IllegalArgumentException("IPv4 octet 범위 오류");out[i]=(byte)n;}return out;}
        private static byte[] parseIpv6(String value){
            if(value.indexOf("::")!=value.lastIndexOf("::"))throw new IllegalArgumentException("IPv6 압축 형식 오류");String[] halves=value.split("::",-1);List<Integer>
                    left=parseHextets(halves[0]);List<Integer> right=halves.length==2?parseHextets(halves[1]):List.of();int missing=8-left.size()-right.size();
                    if((halves.length==1&&missing!=0)||(halves.length==2&&missing<1))throw new IllegalArgumentException("IPv6 hextet 수 오류");List<Integer> all=new ArrayList<>(8);
                    all.addAll(left);for(int i=0;i<missing;i++)all.add(0);all.addAll(right);if(all.size()!=8)throw new IllegalArgumentException("IPv6 hextet 수 오류");byte[] out=new byte[16];
                    for(int i=0;i<8;i++){out[i*2]=(byte)(all.get(i)>>8);out[i*2+1]=(byte)(all.get(i)&255);}return out;}
        private static List<Integer> parseHextets(String half){if(half.isEmpty())return List.of();String[] parts=half.split(":",-1);List<Integer> out=new ArrayList<>();for(String
                part:parts){if(part.contains(".")){byte[] v4=parseIpv4(part);out.add(((v4[0]&255)<<8)|(v4[1]&255));out.add(((v4[2]&255)<<8)|(v4[3]&255));}else{
                if(!part.matches("[0-9A-Fa-f]{1,4}"))throw new IllegalArgumentException("IPv6 hextet 형식 오류");out.add(Integer.parseInt(part,16));}}return out;}
    }
}
