package external.domain.model;

import java.time.Instant;

/** 중앙 Generated Domain Schema와 1:1로 대응하는 Vendor-neutral Sample 모델입니다. */
public final class SampleItem {
    private long sampleItemId;
    private String sampleKey;
    private String itemName;
    private String statusCode;
    private long versionNo;
    private String idempotencyKey;
    private String transactionId;
    private long transactionSequence;
    private Instant transactionAt;
    private String deletedYn;
    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;

    /** SampleItem 작업을 CPF 표준 계약에 따라 수행한다. */
    public SampleItem() { }
    public SampleItem(long sampleItemId, String sampleKey, String itemName, String statusCode,
            long versionNo, String idempotencyKey, String transactionId, long transactionSequence,
            Instant transactionAt, String deletedYn, String createdBy, Instant createdAt,
            String updatedBy, Instant updatedAt) {
        this.sampleItemId=sampleItemId; this.sampleKey=sampleKey; this.itemName=itemName;
        this.statusCode=statusCode; this.versionNo=versionNo; this.idempotencyKey=idempotencyKey;
        this.transactionId=transactionId; this.transactionSequence=transactionSequence;
        this.transactionAt=transactionAt; this.deletedYn=deletedYn; this.createdBy=createdBy;
        this.createdAt=createdAt; this.updatedBy=updatedBy; this.updatedAt=updatedAt;
    }
    public long getSampleItemId() { return sampleItemId; }
    public void setSampleItemId(long value) { sampleItemId=value; }
    public String getSampleKey() { return sampleKey; }
    public void setSampleKey(String value) { sampleKey=value; }
    public String getItemName() { return itemName; }
    public void setItemName(String value) { itemName=value; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String value) { statusCode=value; }
    public long getVersionNo() { return versionNo; }
    public void setVersionNo(long value) { versionNo=value; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String value) { idempotencyKey=value; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String value) { transactionId=value; }
    public long getTransactionSequence() { return transactionSequence; }
    public void setTransactionSequence(long value) { transactionSequence=value; }
    public Instant getTransactionAt() { return transactionAt; }
    public void setTransactionAt(Instant value) { transactionAt=value; }
    public String getDeletedYn() { return deletedYn; }
    public void setDeletedYn(String value) { deletedYn=value; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String value) { createdBy=value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt=value; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String value) { updatedBy=value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { updatedAt=value; }
}
