package com.cpf.core.api.time;
import java.time.*;
public interface CpfTimeOperations extends CpfTimeSource {
    CpfTimeSnapshot snapshot(ZoneId businessZone, Duration maximumAllowedSkew);
    default CpfDeadline deadline(Duration timeout){ return CpfDeadline.after(this,timeout); }
}
