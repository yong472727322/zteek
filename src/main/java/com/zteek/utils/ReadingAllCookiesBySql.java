package com.zteek.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zteek.entity.AllCookie;

public class ReadingAllCookiesBySql {
	public static int DataRecords = 1;
	public static int x =0;
	public static String Testfirst = null;
	public static String Testlast = null;
	public static List<List<AllCookie>>  Cookies = new ArrayList<List<AllCookie>>();
	
	
	 public static List<List<AllCookie>>  ReadingAllCookie(List<String> cookies) throws IOException, ParseException {
		 
	        for (String cookie : cookies) {
                          List<AllCookie> Cook = new ArrayList<AllCookie>(); 
		                  String[] CK = cookie.split("},");
		                  for(int x=0;x<CK.length;x++) {
		                	  		                	  
		                	  if(x==0) {
		                		 String[] one  = CK[0].split("\\[");		                		
		                		  Testfirst = one[1];
		                		  CK[0] = Testfirst;
		                	  }if(x==CK.length-1) {		                		  		                		
		                		  String[] twos   = CK[CK.length-1].split("}]");		                		     		 
		                		  Testlast = twos[0];
		                		  CK[CK.length-1] = Testlast;
		                	 }
		                  }	
   
		                 List<String> breakup =  new ArrayList<String>();

                          for(int x=0;x<CK.length;x++) {                        	                          
		                	String[] Cookies =   CK[x].split("\\{"); 
		                	for(int z=0;z<Cookies.length;z++) {
		                		if(z==1) {
		                			breakup.add(Cookies[z]);
		                		}
		                	}		                		                			                	
		                  }
              
                          String DomainS = "";
                          Date ExpirationDateS = null;
                          boolean HostOnlyS = false;
                          boolean HttpOnlyS = false;
                          String NameS = "";
                          String PathS = "";
                          boolean SecureS = false;
                          String sameSite="";
                          boolean SessionS = false;
                          String StoreIdS = "";
                          String ValueS = "";
                          int IdS = 0;
                          
                       
                          
                          for (String str : breakup) {							 
                     
                        	  String[]  TEST = str.split(",");
                        	  boolean containsValue = TEST[1].contains("value");
                        	  for(int z=0;z<TEST.length;z++) {
                        		  if(!containsValue) {
                        			  if(z==0) {
                             			 try {
                             			 String[] domain =  TEST[0].split("\"domain\":\"");
                             			 String[] Domain =  domain[1].split("\"");
                             			 DomainS =  Domain[0];
                             			 }catch(Exception ex){
                             				 
                             			 }
                             			
                             		 }else if(z==1) {
                             			 String[] expirationDate = null;
                             			 try {
                             				 if(TEST[1].contains("expirationDate")) {
                             					 expirationDate =  TEST[1].split("\"expirationDate\":");
                                     			 String ExpirationDateS1 =  stampToDate(expirationDate[1]);                       			
                                     			 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                     			 ExpirationDateS = simpleDateFormat.parse(ExpirationDateS1);
                             				 }
                             				
                             			 }catch(Exception e) {
                             				e.printStackTrace(); 
                             			 }
                             			
                             			                    			                     			     
                             		 }else if(z==2) {  
                             			 String[] hostOnly = null;
                             			 try {
                             				 if(TEST[2].contains("hostOnly")) {
                             					 hostOnly =  TEST[2].split("\"hostOnly\":");
                                     			 String conversions1 =   hostOnly[1];
                                     		     HostOnlyS =  Boolean.parseBoolean(conversions1);
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             		 }else if(z==3) {  
                             			 try {
                             				 if(TEST[3].contains("httpOnly")) {
                             					 String[] httpOnly =  TEST[3].split("\"httpOnly\":");
                                     			 String conversions2 =   httpOnly[1];
                                     			 HttpOnlyS =  Boolean.parseBoolean(conversions2);
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             		 }else if(z==4) {  
                             			 try {
                             				 if(TEST[4].contains("name")) {
                             					 String[] name =  TEST[4].split("\"name\":\"");
                                     			 String []  Name =   name[1].split("\""); 
                                     			 NameS =  Name[0];
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             		     
                             		 }else if(z==5) {  
                             			 try {
                             				 if(TEST[5].contains("path")) {
                             					 String[] path =  TEST[5].split("\"path\":\"");
                                     			 String []  Path =   path[1].split("\""); 
                                     			 PathS =  Path[0];
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			 
                             			
                             		    
                             		 }else if(z==6) {  
                             			 try {
                             				 if(TEST[6].contains("sameSite")) {
                             					 String[] sameSites =  TEST[6].split("\"sameSite\":");
                                     			 String[]   conversions3 =   sameSites[1].split(","); 
                                     			 sameSite =  conversions3[0];
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			
                             		     
                             		 } else  if(z==7) {  
                             			 try {
                             				 if(TEST[7].contains("secure")) {
                             					 String[] secure =  TEST[7].split("\"secure\":");
                                     			 String   conversions3 =   secure[1]; 
                                     			 SecureS =  Boolean.parseBoolean(conversions3);
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			
                             		     
                             		 }else if(z==8) {  
                             			 try {
                             				 if(TEST[8].contains("session")) {
                             					 String[] session =  TEST[8].split("\"session\":");
                                     			 String   conversions4 =   session[1]; 
                                     			 SessionS =  Boolean.parseBoolean(conversions4);
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			
                             		    
                             		 }else if(z==9) {  
                             			 try {
                             				 if(TEST[9].contains("storeId")) {
                             					 String[] storeId =  TEST[9].split("\"storeId\":\"");
                                     			 String []  StoreId =   storeId[1].split("\""); 
                                     			 StoreIdS =  StoreId[0];
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			 
                             		     
                             		 }else if(z==10) {  
                             			 try {
                             				 if(TEST[10].contains("value")) {
                             					 String[] value =  TEST[10].split("\"value\":\"");                        		
                                     			 if("\"".equals(value[1])) {
                                     				ValueS="";
                                     				continue;
                                     			 }else {
                                     				 String[] Value =  value[1].split("\"");
                                     				 ValueS=Value[0];                        				
                                     			 }
                                                  System.out.println(ValueS);
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			 
                             			 
                             		 }else if(z==11) {  
                             			 try {
                             				 if(TEST[11].contains("id")) {
                             					 String[] id =  TEST[11].split("\"id\":");
                                     			 String   Id =   id[1]; 
                                     			 IdS =  Integer.valueOf(Id);   
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             		     
                             		 }
                             		  
                        		  }else {
                        			 //fake_user的cookie 
                        			  if(z==0) {
                              			 try {
                              			 String[] domain =  TEST[3].split("\"domain\":\"");
                              			 String[] Domain =  domain[1].split("\"");
                              			 DomainS =  Domain[0];
                              			 }catch(Exception ex){
                              				 
                              			 }
                              			
                              		 }else if(z==1) {
                             			 String[] expirationDate = null;
                             			 try {
                             				 if(TEST[1].contains("expiry")) {
                             					 expirationDate =  TEST[1].split("\"expirationDate\":");
                                     			 String ExpirationDateS1 =  stampToDate(expirationDate[1]);                       			
                                     			 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                     			 ExpirationDateS = simpleDateFormat.parse(ExpirationDateS1);
                             				 }
                             				
                             			 }catch(Exception e) {
                             				e.printStackTrace(); 
                             			 }
                             			
                             			                    			                     			     
                             		 }else if(z==4) {  
                             			 try {
                             				 if(TEST[1].contains("value")) {
                             					 String[] value =  TEST[1].split("\"value\":\"");                        		
                                     			 if("\"".equals(value[1])) {
                                     				ValueS="";
                                     				continue;
                                     			 }else {
                                     				 String[] Value =  value[1].split("\"");
                                     				 ValueS=Value[0];                        				
                                     			 }
                                                  System.out.println(ValueS);
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			 
                             			 
                             		 }else if(z==3) {  
                             			 try {
                             				 if(TEST[2].contains("path")) {
                             					 String[] path =  TEST[2].split("\"path\":\"");
                                     			 String []  Path =   path[1].split("\""); 
                                     			 PathS =  Path[0];
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             			 
                             			
                             		    
                             		 }else if(z==2) {  
                             			 try {
                             				 if(TEST[0].contains("name")) {
                             					 String[] name =  TEST[0].split("\"name\":\"");
                                     			 String []  Name =   name[1].split("\""); 
                                     			 NameS =  Name[0];
                             				 }
                             			 }catch(Exception e) {
                             				 e.printStackTrace();
                             			 }
                             		     
                             		 }
                        		  }
                        	  }
                        	  Cook.add(new AllCookie(DomainS, ExpirationDateS, HostOnlyS, HttpOnlyS, NameS, PathS, SecureS, SessionS, StoreIdS, ValueS, IdS));
						  }
                          Cookies.add(Cook);
		                }        	            	           
	        return Cookies;
	 }
	
	  public static String stampToDate(String s){
		  if(s.contains(".")) {
//			  String[] split = s.split("\\.");
			  s = s.substring(0, 10);
		  }
//		  s=  s.substring(0,s.indexOf("."));
          String res;
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          long lt = new Long(s);
          Date date = new Date(lt);
          res = simpleDateFormat.format(date);
          return res;
    }

}
