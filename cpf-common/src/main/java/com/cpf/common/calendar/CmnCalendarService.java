package com.cpf.common.calendar;

import com.cpf.foundation.api.CpfBaseService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** CPF 고객 업무공통 영업일 Service입니다. */
@Service
public class CmnCalendarService extends CpfBaseService implements CmnBusinessCalendar {
    private static final int MAX_SHIFT_DAYS=3660;
    private static final Logger LOGGER=Logger.getLogger(CmnCalendarService.class.getName());
    private final CmnCalendarStore store;
    private final CmnCalendarChangePublisher changePublisher;
    private final boolean productMode;
    private final Clock clock;

    @Autowired
    public CmnCalendarService(ObjectProvider<CmnCalendarStore> storeProvider,ObjectProvider<CmnCalendarChangePublisher> publisherProvider,@Qualifier(com.cpf.common.spi.CpfCommonPersistenceNames.CLOCK_BEAN) Clock clock){
        this.productMode=true;
        CmnCalendarStore resolved=storeProvider.getIfAvailable();
        if(resolved==null||!resolved.writable()||!resolved.actorAwareMutations()){
            throw new IllegalStateException("CPF Common Calendar requires the canonical writable/actor-aware cpfDB store.");
        }
        this.store=resolved;
        this.changePublisher=publisherProvider.getIfAvailable(CmnCalendarChangePublisher::noop);
        this.clock=Objects.requireNonNull(clock,"clock");
    }
    CmnCalendarService(CmnCalendarStore store){this(store,CmnCalendarChangePublisher.noop(),false,Clock.systemUTC());}
    CmnCalendarService(CmnCalendarStore store,CmnCalendarChangePublisher changePublisher,boolean productMode){this(store,changePublisher,productMode,Clock.systemUTC());}
    CmnCalendarService(CmnCalendarStore store,CmnCalendarChangePublisher changePublisher,boolean productMode,Clock clock){this.store=Objects.requireNonNull(store);this.changePublisher=Objects.requireNonNull(changePublisher);this.productMode=productMode;this.clock=Objects.requireNonNull(clock);}
    @Override public boolean isBusinessDay(String calendarId,LocalDate date){Objects.requireNonNull(date);return store.find(normalize(calendarId),date).map(CmnCalendarDay::businessDay).orElseGet(()->date.getDayOfWeek()!=DayOfWeek.SATURDAY&&date.getDayOfWeek()!=DayOfWeek.SUNDAY);}
    @Override public LocalDate shiftBusinessDay(String calendarId,LocalDate from,int offset){Objects.requireNonNull(from);if(offset==0)return from;int direction=offset>0?1:-1,remaining=Math.abs(offset),guard=0;LocalDate cursor=from;while(remaining>0){cursor=cursor.plusDays(direction);if(isBusinessDay(calendarId,cursor))remaining--;if(++guard>MAX_SHIFT_DAYS)throw new IllegalStateException("영업일 계산 한도를 초과했습니다.");}return cursor;}
    public java.util.Optional<CmnCalendarDay> findDay(String calendarId,LocalDate date){Objects.requireNonNull(date);return store.find(normalize(calendarId),date);}
    public List<CmnCalendarDay> findRange(String calendarId,LocalDate from,LocalDate to,int limit){return store.findRange(normalize(calendarId),from,to,limit);}
    public CmnCalendarDay save(CmnCalendarDay day,long expectedVersion){if(productMode)throw new IllegalStateException("Product Calendar mutation은 operatorId overload가 필수입니다.");return save(day,expectedVersion,"EDU_SYSTEM");}
    @Transactional(transactionManager="cpfCommonTransactionManager")
    public CmnCalendarDay save(CmnCalendarDay day,long expectedVersion,String operatorId){requireWritable();String actor=required(operatorId);CmnCalendarDay saved=store.save(day,expectedVersion,actor);publishChange(new CmnCalendarChangeEvent("UPSERT",saved.calendarId(),saved.businessDate(),saved.version(),clock.instant()));return saved;}
    public void delete(String calendarId,LocalDate date,long expectedVersion){if(productMode)throw new IllegalStateException("Product Calendar mutation은 operatorId overload가 필수입니다.");delete(calendarId,date,expectedVersion,"EDU_SYSTEM");}
    @Transactional(transactionManager="cpfCommonTransactionManager")
    public void delete(String calendarId,LocalDate date,long expectedVersion,String operatorId){requireWritable();String normalized=normalize(calendarId);store.delete(normalized,date,expectedVersion,required(operatorId));publishChange(new CmnCalendarChangeEvent("DELETE",normalized,date,expectedVersion+1,clock.instant()));}
    public boolean writable(){return store.writable();}
    public boolean productMode(){return productMode;}
    private void publishChange(CmnCalendarChangeEvent event){
        if(productMode){
            // Product mode publisher is a durable cpfDB outbox adapter. Propagate failure so the surrounding
            // cmnTransaction rolls back instead of leaving other instances permanently stale.
            changePublisher.publish(event);
            return;
        }
        try{changePublisher.publish(event);}catch(RuntimeException ex){LOGGER.log(Level.WARNING,"CPF Calendar change propagation failed in non-product mode: operation={0}, calendarId={1}, date={2}",new Object[]{event.operation(),event.calendarId(),event.businessDate()});}
    }
    private void requireWritable(){if(!store.writable())throw new IllegalStateException("Calendar Store가 조회 전용입니다. 운영 Override 저장소를 구성하십시오.");}
    private String normalize(String id){return id==null||id.isBlank()?"DEFAULT":id.trim();}
    private String required(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("operatorId는 필수입니다.");return v.trim();}
}
