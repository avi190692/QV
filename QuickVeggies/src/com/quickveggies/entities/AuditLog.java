package com.quickveggies.entities;

import java.util.Date;

public class AuditLog {

    private Integer id;
    private String userId;
    private Date eventTime;
    private String eventDetail;

    private String eventObject;
    private Integer eventObjectId;
    
    private String oldValues;
    private String newValues;
    
    private String name;
    private Date date;
    private Double amount;
    //For internal purpose
    private Object entryObject;
    
    public AuditLog(String userId, Date eventTime,String eventDetail, String eventObject, Integer eventObjectId) 
    {
        super();
        this.userId = userId;
        this.eventTime = eventTime;
        this.eventDetail = eventDetail;
        this.eventObject = eventObject;
        this.eventObjectId = eventObjectId;
    }

    public AuditLog(Integer id, String userId, Date eventTime,String eventDetail, String eventObject, Integer eventObjectId) 
    {
        super();
        this.id = id;
        this.userId = userId;
        this.eventTime = eventTime;
        this.eventDetail = eventDetail;
        this.eventObject = eventObject;
        this.eventObjectId = eventObjectId;
    }

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(String eventDetail) {
        this.eventDetail = eventDetail;
    }

    public String getEventObject() {
        return eventObject;
    }

    public void setEventObject(String eventObject) {
        this.eventObject = eventObject;
    }

    public Integer getEventObjectId() {
        return eventObjectId;
    }

    public void setEventObjectId(Integer eventObjectId) {
        this.eventObjectId = eventObjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public Object getEntryObject() {
        return entryObject;
    }

    public void setEntryObject(Object entryObject) {
        this.entryObject = entryObject;
    }

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
    
    
}
