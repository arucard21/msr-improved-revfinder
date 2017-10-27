package com.github.arucard21.msr.revfinder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Message {
       private final String id;
       private final GerritUser author;
       private final GerritUser realAuthor;
       private final LocalDateTime date;
       private final String message;
       private final int revisionNumber;
       private static final String JSON_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.nnnnnnnnn";

       public Message(String id, GerritUser author, GerritUser realAuthor, LocalDateTime date, String message, int revisionNumber) {
               this.id = id;
               this.author = author;
               this.realAuthor = realAuthor;
               this.date = date;
               this.message = message;
               this.revisionNumber = revisionNumber;
       }
       
       public Message(JsonObject messageJSON) {
               this.id = messageJSON.getString("id", "");
               JsonObject authorJSON = messageJSON.getJsonObject("author");
               if(authorJSON != null) {
                       this.author = new GerritUser(authorJSON);
               }
               else {
                       this.author = null;
               }
               JsonObject realAuthorJSON = messageJSON.getJsonObject("real_author");
               if(realAuthorJSON != null) {
                       this.realAuthor = new GerritUser(realAuthorJSON);
               }
               else {
                       this.realAuthor = null;
               }
               this.date = toLocalDateTime(messageJSON.getString("date"));
               this.message = messageJSON.getString("message", "");
               this.revisionNumber = messageJSON.getInt("_revision_number", -1);
       }
       
       public JsonObject asJsonObject() {
               JsonObjectBuilder builder = Json.createObjectBuilder()
                               .add("id", id)
                               .add("date", fromLocalDateTime(date))
                               .add("_revision_number", revisionNumber)
                               .add("message", message);
               if (author != null) {                   
                       builder.add("author", author.asJsonObject());
               }
               if (realAuthor != null) {
                       builder.add("real_author", realAuthor.asJsonObject());
               }
               return builder.build();
       }

       public String getId() {
               return id;
       }

       public GerritUser getAuthor() {
               return author;
       }

       public GerritUser getRealAuthor() {
               return realAuthor;
       }

       public LocalDateTime getDate() {
               return date;
       }

       public String getMessage() {
               return message;
       }

       public int getRevisionNumber() {
               return revisionNumber;
       }

       private LocalDateTime toLocalDateTime(String dateString) {
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern(JSON_DATE_TIME_PATTERN);
               LocalDateTime  date = LocalDateTime.parse(dateString, formatter);
               return date;
       }

       private String fromLocalDateTime(LocalDateTime date) {
               if (date == null) {
                       return "";
               }
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern(JSON_DATE_TIME_PATTERN);
               return date.format(formatter);
       }
}

