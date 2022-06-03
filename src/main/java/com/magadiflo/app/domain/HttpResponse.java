package com.magadiflo.app.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.util.Date;
/**
 * La respuesta que enviemos al cliente
 * incluirá esta clase. Esto con la
 * finalidad de UNIFORMIZAR todas las respuestas
 * que dé nuestra API REST.
 * <p>
 * Esta clase no se va a asignar a la BD, ya que será
 * de la misma aplicación.
 * <p>
 * Se comenta en las propiedades de esta clase
 * valores de ejemplo.
 */
public class HttpResponse {

    private int httpStatusCode; //200, 201, 400, 500
    private HttpStatus httpStatus; //OK
    private String reason; //OK
    private String message;//Your request was successful

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "America/Lima")
    private Date timeStamp;

    public HttpResponse() {
    }

    public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
        this.timeStamp = new Date();
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
