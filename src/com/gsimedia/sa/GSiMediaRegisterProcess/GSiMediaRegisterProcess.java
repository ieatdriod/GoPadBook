/**
 * 
 */
package com.gsimedia.sa.GSiMediaRegisterProcess;

import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.gsimedia.sa.Internet.Internet;
import com.gsimedia.sa.Internet.InternetXMLDocument;

import android.net.wifi.WifiManager;

/**
 * @author user1
 *
 * 20110506 http post
 */
public class GSiMediaRegisterProcess {
	//Begin Need Modify 1-2: Http URI
	//20110506 Pre-Production: http post
	/*
	private static String Http_getRegister = "http://124.29.140.208/DeliverWeb/New_Register";
	private static String Http_checkDomain = "http://124.29.140.208/DeliverWeb/New_Check_Domain";
	private static String Http_getManageDomain = "http://124.29.140.208/DeliverWeb/New_Manage_Domain";
	*/
	/*
	//20110506 Staging: http post
	private static String Http_getRegister = "http://124.29.140.83/DeliverWeb/New_Register";
	private static String Http_checkDomain = "http://124.29.140.83/DeliverWeb/New_Check_Domain";
	private static String Http_getManageDomain = "http://124.29.140.83/DeliverWeb/New_Manage_Domain";
	*/
	//20110506 Production: http post
	
	private static String Http_getRegister = "http://delivery.twmebook.match.net.tw/DeliverWeb/New_Register";
	private static String Http_checkDomain = "http://delivery.twmebook.match.net.tw/DeliverWeb/New_Check_Domain";
	private static String Http_getManageDomain = "http://delivery.twmebook.match.net.tw/DeliverWeb/New_Manage_Domain";
	
	//End Need Modify 1
	
	//Begin Need Modify 2-2: Http Platform Name(Phone: android_phone, Pad: android_pad)
	private static String Http_platform_Name = "android_pad";
	//End Need Modify 2
	
	//20110506 for pad:http post
	private static String Http_device_id = "device_id";
	private static String Http_platform = "platform";
	private static String Http_device_Num = "num";
	private static String Http_device_Num_Index = null;
	private static String Http_device_token = "token";
	private static String Http_device_token_name = null;

	//META DATA  
	private static final String ROOT_ELEMENT_TAG_P12GENERATION = "p12_generation";
	private static final String ROOT_ELEMENT_TAG_REGISTER = "register";
	private static final String ELEMENT_TAG_RESULTCODE = "resultCode";
	private static final String ELEMENT_TAG_RESULT = "results";
	private static final String ELEMENT_TAG_P12 = "p12";	  
	private static final String ELEMENT_TAG_DOMAIN = "domain";	  
	private static final String ELEMENT_TAG_NAME = "name";
	private static final String ELEMENT_TAG_MOBILE_EQUIPMENT_TYPE = "mobile_equipment_type";
	private static final String ELEMENT_TAG_INDEX = "index";
	private static final String ELEMENT_TAG_TYPE = "type";
	//STORE DATA
	private static String P12_Data = null;
	private static String P12_FileName = "TWM.p12";
	
	public GSiMediaRegisterProcess() {
		
	}
	
	public static boolean isP12Exist(String p12) {
		try {
			File file = new File(p12,P12_FileName);
			return file.exists();
		}catch(Exception e) {
			return false;
		}
	}
	
	public static String getID(Context context)throws DeviceIDException{
		String sDeviceID = "";
  		try {
  			TelephonyManager teleManager = 
  				(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
  			sDeviceID = teleManager.getDeviceId();
			if(sDeviceID == null) {
				sDeviceID = getPropertiesIMEI(context);
			}
			if(sDeviceID == null || sDeviceID.equals("")) {
				sDeviceID = getWifiMacAddr(context);
			}
  		} catch (Exception e) {
  			throw new DeviceIDException(e);
  		}
  		return sDeviceID;
	}
	//20110506 http post
    /*2011.03.28 add Wifi Mac Address Check*/
	public static DataClass register(String p12, Context context, String token) throws 
	IllegalNetworkException, TimeOutException, XmlP12FileException, XmlException, DeviceIDException {
        String sDeviceID = "";
  		try {
  			TelephonyManager teleManager = 
  				(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
  			sDeviceID = teleManager.getDeviceId();
			if(sDeviceID == null) {
				sDeviceID = getPropertiesIMEI(context);
			}
			if(sDeviceID == null || sDeviceID.equals("")) {
				sDeviceID = getWifiMacAddr(context);
			}
  		} catch (Exception e) {
  			throw new DeviceIDException(e);
  		}
  		
		/*測試網路是否連線*/
  		try {
			int InternetStatus = Internet.haveInternet(context);
			if (InternetStatus < 0) {
				throw new IllegalNetworkException(String.valueOf(InternetStatus));
			}
		} catch (Exception e) {
			throw new IllegalNetworkException(e);
		}
  		
		String URL = Http_getRegister;
		DataClass dataClass = null;
		dataClass = getDataClass(URL, sDeviceID, token);
		
		//save P12_Data to p12 directory path.
		if(dataClass != null && dataClass.resultCode_P12 != null && dataClass.resultCode_P12.endsWith("0")) {
			try {
				File mP12Dir = new File(p12,P12_FileName);
	//			File mP12Dir = new File("/sdcard",P12_FileName);
				if(!mP12Dir.exists())
					mP12Dir.createNewFile();
				FileOutputStream fileOutputStream = new FileOutputStream(mP12Dir);
				if (P12_Data != null) {
					//fileOutputStream.write(P12_Data.getBytes());
					fileOutputStream.write(HexStringToBytesArray(P12_Data));
					//fileOutputStream.close();
				}else {
					String str = "時間"+String.valueOf(System.currentTimeMillis());
					fileOutputStream.write(str.getBytes());
					//throw new XmlP12FileException("P12 Data is null");
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				throw new XmlP12FileException(e);
			}
		}
		
		return dataClass;
	}
	
	public static byte[] HexStringToBytesArray(String hexString){
        byte bytes[] = new byte[hexString.length()/2];
        for(int i=0; i<(hexString.length()/2); i++){
            int iTemp = Integer.parseInt(hexString.substring((i*2), 2*(i+1)), 16);
            bytes[i] = (byte) iTemp;
        }
        return bytes;
    }
	
    /*2011.03.28 add Wifi Mac Address Check*/
	public static DataClass checkDomain(Context context, String token) throws 
	IllegalNetworkException, TimeOutException, XmlException, DeviceIDException {
	    String sDeviceID = "";
		try {
			TelephonyManager teleManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			sDeviceID = teleManager.getDeviceId();
			if(sDeviceID == null) {
				sDeviceID = getPropertiesIMEI(context);
			}
			if(sDeviceID == null || sDeviceID.equals("")) {
				sDeviceID = getWifiMacAddr(context);
			}
		} catch (Exception e) {
			throw new DeviceIDException(e);
		}
		
		/*測試網路是否連線*/
		try {
			int InternetStatus = Internet.haveInternet(context);
			if (InternetStatus < 0) {
				throw new IllegalNetworkException(String.valueOf(InternetStatus));
			}
		} catch (Exception e) {
			throw new IllegalNetworkException(e);
		}

		String URL = Http_checkDomain;
		DataClass dataClass = null;
		dataClass = getDataClass(URL, sDeviceID, token);
		return dataClass;
	}
	
	/*2011.03.28 add Wifi Mac Address Check*/
	public static DataClass manageDomain(String num, Context context, String token) throws 
	IllegalNetworkException, TimeOutException, XmlException, DeviceIDException {
		    String sDeviceID = "";
			try {
				TelephonyManager teleManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				sDeviceID = teleManager.getDeviceId();
				if(sDeviceID == null) {
					sDeviceID = getPropertiesIMEI(context);
				}
				if(sDeviceID == null || sDeviceID.equals("")) {
					sDeviceID = getWifiMacAddr(context);
				}
			} catch (Exception e) {
				throw new DeviceIDException(e);
			}
			
			/*測試網路是否連線*/
			try {
				int InternetStatus = Internet.haveInternet(context);
				if (InternetStatus < 0) {
					throw new IllegalNetworkException(String.valueOf(InternetStatus));
				}
			} catch (Exception e) {
				throw new IllegalNetworkException(e);
			}

			String URL = Http_getManageDomain; 
			DataClass dataClass = null;
			dataClass = getDataClass(URL, sDeviceID, token, num);
			return dataClass;
	}
	
	private static DataClass getDataClass(String URL) throws
	XmlException, TimeOutException {
		int i,j,k,m,n;
		String resultCode_p12 = null; 
		String resultCode_register = null; 
		String domain = null; 
		String[] index = null; 
		String[] type = null; 
		Document doc;
		InternetXMLDocument internetXMLDocument;
		P12_Data = null;
		
		internetXMLDocument = new InternetXMLDocument(URL);
		doc = internetXMLDocument.getDocument();
		
        // XmlNodeList object: Find the p12_generation node
        NodeList nodeList = doc.getElementsByTagName( ROOT_ELEMENT_TAG_P12GENERATION );
        if(nodeList != null && nodeList.getLength() > 0) {
        	for (i=0; i<nodeList.getLength(); i++) {
        		Node firstPersonNode = nodeList.item(i);
        		if(firstPersonNode != null) {
        			if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
            			NodeList mychildList = firstPersonNode.getChildNodes();
            			if(mychildList != null && mychildList.getLength() > 0) {
            				for (j=0; j<mychildList.getLength(); j++) {
            					Node mychildNode = mychildList.item(j);
            					if (mychildNode != null) {
            						if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULTCODE)) {
                						//get resultCode_p12
                						if (mychildNode.getChildNodes() != null && mychildNode.getChildNodes().getLength()>0) {
                							if (mychildNode.getFirstChild().getNodeValue() != null)
                								resultCode_p12 = mychildNode.getFirstChild().getNodeValue().trim();
                						}
                					}else if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULT)) {
                						//get p12
                						NodeList p12List = mychildNode.getChildNodes();
                						if (p12List != null && p12List.getLength() > 0) {
                							for (k=0; k<p12List.getLength(); k++) {
                								Node p12Node = p12List.item(k);
            									if (p12Node.getNodeName().equals(ELEMENT_TAG_P12)) {
            										if (p12Node.getChildNodes() != null && p12Node.getChildNodes().getLength()>0) {
            											if (p12Node.getFirstChild().getNodeValue() != null)
            												P12_Data = p12Node.getFirstChild().getNodeValue().trim();
            										}
            									}
            								}	
                						}
                					}
            					}
            				}                  				           		           	  			
            			}
            		}
        		}
        	}	
        }
        
        // XmlNodeList object: Find the register node
        nodeList = doc.getElementsByTagName( ROOT_ELEMENT_TAG_REGISTER );
        if(nodeList != null && nodeList.getLength() > 0) {
        	for (i=0; i<nodeList.getLength(); i++) {
        		Node firstPersonNode = nodeList.item(i);
        		if(firstPersonNode != null) {
        			if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
            			NodeList mychildList = firstPersonNode.getChildNodes();
            			if(mychildList != null && mychildList.getLength() > 0) {
            				for (j=0; j<mychildList.getLength(); j++) {
            					Node mychildNode = mychildList.item(j);
            					if (mychildNode != null) {
            						if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULTCODE)) {
            							//get resultCode_register
                						if (mychildNode.getChildNodes() != null && mychildNode.getChildNodes().getLength()>0) {
                							if (mychildNode.getFirstChild().getNodeValue() != null)
                								resultCode_register = mychildNode.getFirstChild().getNodeValue().trim();
                						}
                					}else if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULT)) {
                						//get register data
                						NodeList registerList = mychildNode.getChildNodes();
                						if (registerList != null && registerList.getLength() > 0) {
                							for (k=0; k<registerList.getLength(); k++) {
                								Node domainNode = registerList.item(k);
            									if (domainNode.getNodeName().equals(ELEMENT_TAG_DOMAIN)) {
            										//get domain data
            										NodeList domainList = domainNode.getChildNodes();
            										if (domainList != null && domainList.getLength() > 0) {
            											int index_num = 0;
    													int type_num = 0;
            											for (m=0; m<domainList.getLength(); m++) {
            												Node domainContentNode = domainList.item(m);
            												if (domainContentNode.getNodeName().equals(ELEMENT_TAG_NAME)) {
            													//get name
            													if (domainContentNode.getChildNodes() != null && domainContentNode.getChildNodes().getLength()>0) {
            														if (domainContentNode.getFirstChild().getNodeValue() != null)
            															domain = domainContentNode.getFirstChild().getNodeValue().trim();
            													}
            												}else if (domainContentNode.getNodeName().equals(ELEMENT_TAG_MOBILE_EQUIPMENT_TYPE)) {
            													//get mobile_equipment_type data
            													NodeList equipmentTypeList = domainContentNode.getChildNodes();
            													if (equipmentTypeList != null && equipmentTypeList.getLength() > 0) {
            														for (n=0; n<equipmentTypeList.getLength(); n++) {
            															Node equipmentTypeNode = equipmentTypeList.item(n);
            															if (equipmentTypeNode.getNodeName().equals(ELEMENT_TAG_INDEX)) {
            																//get index
            																if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
            																	if (index == null) {
            																		NodeList indexList = equipmentTypeNode.getOwnerDocument().getElementsByTagName(ELEMENT_TAG_INDEX);
            																		if (indexList != null && indexList.getLength() > 0) {
            																			index = new String[indexList.getLength()];
            																		}
            																	}
            																	if (equipmentTypeNode.getChildNodes() != null && equipmentTypeNode.getChildNodes().getLength()>0) {
	        																		if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
	        																			index[index_num] = equipmentTypeNode.getFirstChild().getNodeValue().trim();
	        																			index_num++;
	        																		}
            																	}
            																}
            															}else if (equipmentTypeNode.getNodeName().equals(ELEMENT_TAG_TYPE)) {
            																//get type
            																if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
            																	if (type == null) {
            																		NodeList typeList = equipmentTypeNode.getOwnerDocument().getElementsByTagName(ELEMENT_TAG_TYPE);
            																		if (typeList != null && typeList.getLength() > 0) {
            																			type = new String[typeList.getLength()];
            																		}
            																	}
            																	if (equipmentTypeNode.getChildNodes() != null && equipmentTypeNode.getChildNodes().getLength()>0) {
	        																		if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
	        																			type[type_num] = equipmentTypeNode.getFirstChild().getNodeValue().trim();
	        																			type_num++;
	        																		}
            																	}
            																}
            															}
            														}
            													}
            												}
            											}
            										}
            									}
            								}	
                						}	
                    				}
            					}
            				}
            			}
        			}
        		}
        	}
        }
        
        DataClass dataClass =  new DataClass();

    	dataClass.setData(resultCode_p12, resultCode_register, domain, index, type);

        return dataClass;
	}
	
	private static DataClass getDataClass(String URL, String sDeviceID, String token, String sNum) throws
	XmlException, TimeOutException {
		Http_device_token_name = token;
		Http_device_Num_Index = sNum;
		return getDataClass(URL, sDeviceID);
	}
	
	private static DataClass getDataClass(String URL, String sDeviceID, String token) throws
	XmlException, TimeOutException {
		Http_device_token_name = token;
		return getDataClass(URL, sDeviceID);
	}
	
	private static DataClass getDataClass(String URL, String sDeviceID) throws
	XmlException, TimeOutException {
		int i,j,k,m,n;
		String resultCode_p12 = null; 
		String resultCode_register = null; 
		String domain = null; 
		String[] index = null; 
		String[] type = null; 
		Document doc;
		InternetXMLDocument internetXMLDocument;
		P12_Data = null;
		
		int ArrayIndex = 6;
		if(Http_device_Num_Index != null) {
			ArrayIndex = 8;
		}
		String[] SendData = new String[ArrayIndex];
		SendData[0] = Http_device_id;
		SendData[1] = sDeviceID;
		SendData[2] = Http_platform;
		SendData[3] = Http_platform_Name;
		SendData[4] = Http_device_token;
		SendData[5] = Http_device_token_name;
		if(Http_device_Num_Index != null) {
			SendData[6] = Http_device_Num;
			SendData[7] = Http_device_Num_Index;
		}
		
		internetXMLDocument = new InternetXMLDocument(URL, SendData);
		doc = internetXMLDocument.getDocument();
		
        // XmlNodeList object: Find the p12_generation node
        NodeList nodeList = doc.getElementsByTagName( ROOT_ELEMENT_TAG_P12GENERATION );
        if(nodeList != null && nodeList.getLength() > 0) {
        	for (i=0; i<nodeList.getLength(); i++) {
        		Node firstPersonNode = nodeList.item(i);
        		if(firstPersonNode != null) {
        			if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
            			NodeList mychildList = firstPersonNode.getChildNodes();
            			if(mychildList != null && mychildList.getLength() > 0) {
            				for (j=0; j<mychildList.getLength(); j++) {
            					Node mychildNode = mychildList.item(j);
            					if (mychildNode != null) {
            						if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULTCODE)) {
                						//get resultCode_p12
                						if (mychildNode.getChildNodes() != null && mychildNode.getChildNodes().getLength()>0) {
                							if (mychildNode.getFirstChild().getNodeValue() != null)
                								resultCode_p12 = mychildNode.getFirstChild().getNodeValue().trim();
                						}
                					}else if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULT)) {
                						//get p12
                						NodeList p12List = mychildNode.getChildNodes();
                						if (p12List != null && p12List.getLength() > 0) {
                							for (k=0; k<p12List.getLength(); k++) {
                								Node p12Node = p12List.item(k);
            									if (p12Node.getNodeName().equals(ELEMENT_TAG_P12)) {
            										if (p12Node.getChildNodes() != null && p12Node.getChildNodes().getLength()>0) {
            											if (p12Node.getFirstChild().getNodeValue() != null)
            												P12_Data = p12Node.getFirstChild().getNodeValue().trim();
            										}
            									}
            								}	
                						}
                					}
            					}
            				}                  				           		           	  			
            			}
            		}
        		}
        	}	
        }
        
        // XmlNodeList object: Find the register node
        nodeList = doc.getElementsByTagName( ROOT_ELEMENT_TAG_REGISTER );
        if(nodeList != null && nodeList.getLength() > 0) {
        	for (i=0; i<nodeList.getLength(); i++) {
        		Node firstPersonNode = nodeList.item(i);
        		if(firstPersonNode != null) {
        			if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
            			NodeList mychildList = firstPersonNode.getChildNodes();
            			if(mychildList != null && mychildList.getLength() > 0) {
            				for (j=0; j<mychildList.getLength(); j++) {
            					Node mychildNode = mychildList.item(j);
            					if (mychildNode != null) {
            						if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULTCODE)) {
            							//get resultCode_register
                						if (mychildNode.getChildNodes() != null && mychildNode.getChildNodes().getLength()>0) {
                							if (mychildNode.getFirstChild().getNodeValue() != null)
                								resultCode_register = mychildNode.getFirstChild().getNodeValue().trim();
                						}
                					}else if (mychildNode.getNodeName().equals(ELEMENT_TAG_RESULT)) {
                						//get register data
                						NodeList registerList = mychildNode.getChildNodes();
                						if (registerList != null && registerList.getLength() > 0) {
                							for (k=0; k<registerList.getLength(); k++) {
                								Node domainNode = registerList.item(k);
            									if (domainNode.getNodeName().equals(ELEMENT_TAG_DOMAIN)) {
            										//get domain data
            										NodeList domainList = domainNode.getChildNodes();
            										if (domainList != null && domainList.getLength() > 0) {
            											int index_num = 0;
    													int type_num = 0;
            											for (m=0; m<domainList.getLength(); m++) {
            												Node domainContentNode = domainList.item(m);
            												if (domainContentNode.getNodeName().equals(ELEMENT_TAG_NAME)) {
            													//get name
            													if (domainContentNode.getChildNodes() != null && domainContentNode.getChildNodes().getLength()>0) {
            														if (domainContentNode.getFirstChild().getNodeValue() != null)
            															domain = domainContentNode.getFirstChild().getNodeValue().trim();
            													}
            												}else if (domainContentNode.getNodeName().equals(ELEMENT_TAG_MOBILE_EQUIPMENT_TYPE)) {
            													//get mobile_equipment_type data
            													NodeList equipmentTypeList = domainContentNode.getChildNodes();
            													if (equipmentTypeList != null && equipmentTypeList.getLength() > 0) {
            														for (n=0; n<equipmentTypeList.getLength(); n++) {
            															Node equipmentTypeNode = equipmentTypeList.item(n);
            															if (equipmentTypeNode.getNodeName().equals(ELEMENT_TAG_INDEX)) {
            																//get index
            																if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
            																	if (index == null) {
            																		NodeList indexList = equipmentTypeNode.getOwnerDocument().getElementsByTagName(ELEMENT_TAG_INDEX);
            																		if (indexList != null && indexList.getLength() > 0) {
            																			index = new String[indexList.getLength()];
            																		}
            																	}
            																	if (equipmentTypeNode.getChildNodes() != null && equipmentTypeNode.getChildNodes().getLength()>0) {
	        																		if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
	        																			index[index_num] = equipmentTypeNode.getFirstChild().getNodeValue().trim();
	        																			index_num++;
	        																		}
            																	}
            																}
            															}else if (equipmentTypeNode.getNodeName().equals(ELEMENT_TAG_TYPE)) {
            																//get type
            																if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
            																	if (type == null) {
            																		NodeList typeList = equipmentTypeNode.getOwnerDocument().getElementsByTagName(ELEMENT_TAG_TYPE);
            																		if (typeList != null && typeList.getLength() > 0) {
            																			type = new String[typeList.getLength()];
            																		}
            																	}
            																	if (equipmentTypeNode.getChildNodes() != null && equipmentTypeNode.getChildNodes().getLength()>0) {
	        																		if (equipmentTypeNode.getFirstChild().getNodeValue() != null) {
	        																			type[type_num] = equipmentTypeNode.getFirstChild().getNodeValue().trim();
	        																			type_num++;
	        																		}
            																	}
            																}
            															}
            														}
            													}
            												}
            											}
            										}
            									}
            								}	
                						}	
                    				}
            					}
            				}
            			}
        			}
        		}
        	}
        }
        
        DataClass dataClass =  new DataClass();

    	dataClass.setData(resultCode_p12, resultCode_register, domain, index, type);

        return dataClass;
	}
	
	/*2011.03.28 add Wifi Mac Address Check*/
	public static String getWifiMacAddr(Context context) {
		String str = null;
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if(wm != null) {
			str = wm.getConnectionInfo().getMacAddress();
			if(str != null){
				str = str.replace(":", "");
			}
		}
		
		return str;
	}
	
	/*2011.08.17 add SystemProperties "ro.gsm.imei" Check*/
	public static String getPropertiesIMEI(Context context) {
		String str = null;
		try {
			str = SystemPropertiesProxy.get(context, "ro.gsm.imei");
		} catch (IllegalArgumentException e) {
		
		}
		return str;
	}
}
