package com.cpf.reference.edu.runtime.application;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
public class EduInjectedFailureException extends RuntimeException {
    private final EduFailurePoint point;
    public EduInjectedFailureException(EduFailurePoint point){super("Injected failure: "+point);this.point=point;}
    public EduFailurePoint point(){return point;}
}
