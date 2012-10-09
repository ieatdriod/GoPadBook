package com.gsimedia.sa.Internet;

/**
 * @author WilliamHseih
 *
 */
public interface InternetConstantManager {
	public static final int NETWORK_CONNECTTIMEOUT = 5000;
	public static final int NETWORK_SOCKETTIMEOUT = 10000;
	
	public static final int NETWORK_ACTIVE = 0;
	public static final int NETWORK_INACTIVE = -1;

	public static final int NETWORK_TYPE_WIFI_CONNECTED = 2;
	public static final int NETWORK_TYPE_WCDMAUSIMCARD_CONNECTED = 3;
	public static final int NETWORK_TYPE_SIMCARD_CONNECTED = 4;
	public static final int NETWORK_TYPE_CDMAUIMCARD_CONNECTED = 5;
	public static final int NETWORK_TYPE_UNKOWN_CONNECTED = 6;
	
	public static final int NETWORK_TYPE_WIFI_DISCONNECTED = -2;
	public static final int NETWORK_TYPE_WCDMAUSIMCARD_DISCONNECTED = -3;
	public static final int NETWORK_TYPE_SIMCARD_DISCONNECTED = -4;
	public static final int NETWORK_TYPE_CDMAUIMCARD_DISCONNECTED = -5;
	public static final int NETWORK_TYPE_UNKOWN_DISCONNECTED = -6;

}
