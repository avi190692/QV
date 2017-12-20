/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quickveggies.controller.popup;

/**
 *
 * @author serg.merlin
 */
public class SendEmailData {
    
    String to;
    String from;
    String title;
    String text;
    String attachment;

    private SendEmailData() {
    }

    public static SendEmailData buildSendEmailData(String to, String from,
            String title, String text, String attachment) {
        SendEmailData buyer = new SendEmailData();
        buyer.to = to;
        buyer.from = from;
        buyer.title = title;
        buyer.text = text;
        buyer.attachment = attachment;
        return buyer;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
    
}
