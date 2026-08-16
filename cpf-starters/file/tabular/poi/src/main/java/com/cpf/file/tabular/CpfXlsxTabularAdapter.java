package com.cpf.file.tabular;

import com.cpf.file.tabular.api.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.eventusermodel.*;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** XLSX SAX Streaming Reader와 SXSSF Streaming Writer입니다. */
public final class CpfXlsxTabularAdapter implements CpfTabularReader, CpfTabularWriter {
    private static final long MAX_INPUT_BYTES = 512L * 1024 * 1024;
    static {
        ZipSecureFile.setMinInflateRatio(0.02d);
        ZipSecureFile.setMaxEntrySize(100L * 1024 * 1024);
        ZipSecureFile.setMaxTextSize(20L * 1024 * 1024);
    }

    @Override

    public boolean supports(CpfTabularFormat format) { return format == CpfTabularFormat.XLSX; }

    @Override
    public CpfTabularReadResult read(CpfTabularReadRequest request, CpfTabularRowConsumer consumer) {
        if (!supports(request.format())) throw new IllegalArgumentException("XLSX 요청이 아닙니다.");
        List<CpfTabularReadResult.RowError> errors = new ArrayList<>();
        AtomicLong accepted = new AtomicLong(), rejected = new AtomicLong();
        try {
            PathBackedDigest input = PathBackedDigest.copy(request.input());
            try (input; OPCPackage pkg = OPCPackage.open(input.path().toFile(), org.apache.poi.openxml4j.opc.PackageAccess.READ)) {
                if (request.rejectMacro() && pkg.getRelationshipsByType(
                        "http://schemas.microsoft.com/office/2006/relationships/vbaProject").size() > 0) {
                    throw new IllegalArgumentException("Macro가 포함된 XLSX는 허용하지 않습니다.");
                }
                XSSFReader reader = new XSSFReader(pkg);
                SharedStrings strings = reader.getSharedStringsTable();
                SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                parserFactory.setNamespaceAware(true);
                parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                parserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                XMLReader parser = parserFactory.newSAXParser().getXMLReader();
                parser.setContentHandler(new SheetHandler(request, consumer, strings, errors, accepted, rejected));
                XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
                if (!sheets.hasNext()) throw new IllegalArgumentException("XLSX Sheet가 없습니다.");
                try (InputStream sheet = sheets.next()) { parser.parse(new InputSource(sheet)); }
                if (sheets.hasNext()) throw new IllegalArgumentException("XLSX는 단일 Sheet만 허용합니다.");
                return new CpfTabularReadResult(accepted.get(), rejected.get(), input.sha256(), errors);
            }
        } catch (Exception error) {
            throw error instanceof RuntimeException runtime ? runtime
                    : new IllegalStateException("XLSX Streaming 처리에 실패했습니다.", error);
        }
    }

    @Override
    public String write(CpfTabularWriteRequest request) {
        Objects.requireNonNull(request, "request");
        if (!supports(request.format())) throw new IllegalArgumentException("XLSX 요청이 아닙니다.");
        SXSSFWorkbook workbook = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            workbook = new SXSSFWorkbook(200);
            workbook.setCompressTempFiles(true);
            try (var digestOut = new java.security.DigestOutputStream(request.output(), digest);
                 var rows = request.rows()) {
                var sheet = workbook.createSheet("data");
                var header = sheet.createRow(0);
                for (int i=0;i<request.schema().columns().size();i++) {
                    header.createCell(i, CellType.STRING).setCellValue(request.schema().columns().get(i).label());
                }
                Set<String> allowed = new HashSet<>();
                request.schema().columns().forEach(column -> allowed.add(column.name()));
                AtomicLong count = new AtomicLong();
                SXSSFWorkbook targetWorkbook = workbook;
                rows.forEachOrdered(row -> {
                    Objects.requireNonNull(row, "row");
                    long current = count.incrementAndGet();
                    if (current > request.schema().maxRows()) throw new IllegalArgumentException("허용 행 수를 초과했습니다.");
                    Set<String> unknown = new HashSet<>(row.values().keySet());
                    unknown.removeAll(allowed);
                    if (!unknown.isEmpty()) throw new IllegalArgumentException("정의되지 않은 Column: " + unknown);
                    var target = targetWorkbook.getSheetAt(0).createRow(Math.toIntExact(current));
                    for (int i=0;i<request.schema().columns().size();i++) {
                        CpfTabularColumn column = request.schema().columns().get(i);
                        String value = CpfCsvTabularAdapter.normalize(
                                row.values().getOrDefault(column.name(), ""), column.type());
                        String code = CpfCsvTabularAdapter.validateValue(column, value, false, false);
                        if (code != null) throw new IllegalArgumentException(column.name() + " 출력값 오류: " + code);
                        if (CpfCsvTabularAdapter.isFormula(value) && !request.escapeFormula()) {
                            throw new IllegalArgumentException(column.name() + "에 XLSX Formula Injection 위험값이 있습니다.");
                        }
                        target.createCell(i, CellType.STRING)
                                .setCellValue(CpfCsvTabularAdapter.sanitize(value, request.escapeFormula()));
                    }
                });
                workbook.write(digestOut);
                digestOut.flush();
            }
            return CpfCsvTabularAdapter.hex(digest.digest());
        } catch (Exception error) {
            throw error instanceof RuntimeException runtime ? runtime
                    : new IllegalStateException("XLSX Streaming 생성에 실패했습니다.", error);
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException ignored) { }
            }
        }
    }

    private static final class SheetHandler extends DefaultHandler {
        private final CpfTabularReadRequest request;
        private final CpfTabularRowConsumer consumer;
        private final SharedStrings strings;
        private final List<CpfTabularReadResult.RowError> errors;
        private final AtomicLong accepted, rejected;
        private final Map<Integer,String> row = new HashMap<>();
        private final Set<Integer> formulaColumns = new HashSet<>();
        private String type = "";
        private final StringBuilder value = new StringBuilder();
        private int column = -1;
        private long rowNumber;
        private long physicalRow;

        SheetHandler(CpfTabularReadRequest request, CpfTabularRowConsumer consumer, SharedStrings strings,
                     List<CpfTabularReadResult.RowError> errors, AtomicLong accepted, AtomicLong rejected) {
            this.request=request;this.consumer=consumer;this.strings=strings;this.errors=errors;
            this.accepted=accepted;this.rejected=rejected;
        }

        @Override

        public void startElement(String uri,String local,String qName,Attributes attributes) {
            if ("row".equals(qName)) {
                row.clear(); formulaColumns.clear(); physicalRow++;
                String rowRef = attributes.getValue("r");
                rowNumber = rowRef == null || rowRef.isBlank() ? physicalRow : Long.parseLong(rowRef);
            }
            else if ("c".equals(qName)) {
                type = Optional.ofNullable(attributes.getValue("t")).orElse("");
                column = columnIndex(attributes.getValue("r"));
            } else if ("f".equals(qName)) {
                if (column < 0) throw new IllegalArgumentException("XLSX Formula Cell 참조가 없습니다.");
                formulaColumns.add(column);
            } else if ("v".equals(qName) || "t".equals(qName)) value.setLength(0);
        }
        @Override
        public void characters(char[] ch,int start,int length) { value.append(ch,start,length); }
        @Override
        public void endElement(String uri,String local,String qName) {
            if ("v".equals(qName) || ("t".equals(qName) && "inlineStr".equals(type))) {
                String decoded = value.toString();
                if ("s".equals(type) && !decoded.isBlank()) decoded = strings.getItemAt(Integer.parseInt(decoded)).getString();
                if (decoded.length() > request.schema().maxCellLength()) throw new IllegalArgumentException("XLSX Cell 길이를 초과했습니다.");
                row.put(column, decoded);
            } else if ("row".equals(qName)) processRow();
        }

        private void processRow() {
            if (physicalRow == 1) {
                if (rowNumber != 1) throw new IllegalArgumentException("XLSX Header는 첫 번째 행이어야 합니다.");
                List<String> actual = new ArrayList<>();
                for (int i=0;i<request.schema().columns().size();i++) actual.add(row.getOrDefault(i,""));
                List<String> expected = request.schema().columns().stream().map(CpfTabularColumn::label).toList();
                if (!actual.equals(expected)) throw new IllegalArgumentException("XLSX Header가 Template version과 일치하지 않습니다.");
                return;
            }
            if (rowNumber - 1 > request.schema().maxRows()) throw new IllegalArgumentException("허용 행 수를 초과했습니다.");
            Map<String,String> values = new LinkedHashMap<>();
            boolean valid = true;
            for (int i=0;i<request.schema().columns().size();i++) {
                CpfTabularColumn c = request.schema().columns().get(i);
                String v = CpfCsvTabularAdapter.normalize(row.getOrDefault(i,""), c.type());
                String code = CpfCsvTabularAdapter.validateValue(c, v, request.rejectFormula(), formulaColumns.contains(i));
                if (code != null) {
                    valid=false;
                    CpfCsvTabularAdapter.addError(errors,rowNumber,c.name(),code,"행 값이 올바르지 않습니다.");
                }
                values.put(c.name(),v);
            }
            if (row.keySet().stream().anyMatch(index -> index < 0 || index >= request.schema().columns().size())) {
                valid=false;
                CpfCsvTabularAdapter.addError(errors,rowNumber,"","EXTRA_COLUMN","정의되지 않은 Column이 존재합니다.");
            }
            if (valid) {
                try { consumer.accept(new CpfTabularRow(rowNumber,values)); accepted.incrementAndGet(); }
                catch (Exception error) { throw new IllegalStateException("행 Consumer 처리에 실패했습니다.",error); }
            } else rejected.incrementAndGet();
        }

        private int columnIndex(String ref) {
            int result=0;
            for (int i=0;ref!=null&&i<ref.length()&&Character.isLetter(ref.charAt(i));i++) {
                result=result*26+(Character.toUpperCase(ref.charAt(i))-'A'+1);
            }
            if (result == 0) throw new IllegalArgumentException("XLSX Cell reference가 없습니다.");
            return result-1;
        }
    }

    private static final class PathBackedDigest implements AutoCloseable {
        private final java.nio.file.Path path;
        private final String sha256;
        private PathBackedDigest(java.nio.file.Path path,String sha256){this.path=path;this.sha256=sha256;}
        static PathBackedDigest copy(InputStream source) throws Exception {
            Objects.requireNonNull(source, "source");
            java.nio.file.Path path=java.nio.file.Files.createTempFile("cpf-xlsx-", ".xlsx");
            try {
                MessageDigest digest=MessageDigest.getInstance("SHA-256");
                try(var out=java.nio.file.Files.newOutputStream(path);
                    var digestOut=new java.security.DigestOutputStream(out,digest)){
                    byte[] buffer = new byte[64 * 1024];
                    long total = 0;
                    int read;
                    while ((read = source.read(buffer)) >= 0) {
                        if (read == 0) continue;
                        total += read;
                        if (total > MAX_INPUT_BYTES) throw new IllegalArgumentException("XLSX 입력 크기가 512MB를 초과했습니다.");
                        digestOut.write(buffer,0,read);
                    }
                }
                return new PathBackedDigest(path,CpfCsvTabularAdapter.hex(digest.digest()));
            } catch(Exception error){java.nio.file.Files.deleteIfExists(path);throw error;}
        }
        java.nio.file.Path path(){return path;} String sha256(){return sha256;}
        @Override
        public void close() throws IOException {java.nio.file.Files.deleteIfExists(path);}
    }
}
