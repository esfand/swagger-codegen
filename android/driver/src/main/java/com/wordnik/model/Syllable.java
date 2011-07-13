package com.wordnik.model;

import com.wordnik.common.WordnikObject;
import com.wordnik.annotations.AllowableValues;
import com.wordnik.annotations.Required;

import com.wordnik.common.WordListType;


/**
 * 
 * NOTE: This class is auto generated by the drive code generator program so please do not edit the program manually.  
 * @author ramesh
 *
 */
public class Syllable extends WordnikObject {

		 //
		 private String type ;  
			 //
		 private int seq ;  
			 //
		 private String text ;  
		
	
		 //
		 
		 public String getType() {
		 	return type;
		 }  
		 
		 public void setType(String  type) {
		 	this.type = type;
		 }
			 //
		 
		 public int getSeq() {
		 	return seq;
		 }  
		 
		 public void setSeq(int  seq) {
		 	this.seq = seq;
		 }
			 //
		 
		 public String getText() {
		 	return text;
		 }  
		 
		 public void setText(String  text) {
		 	this.text = text;
		 }
		
}