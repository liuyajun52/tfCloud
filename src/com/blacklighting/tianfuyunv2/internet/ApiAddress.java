/**
/**
/**
 * 
 */
package com.blacklighting.tianfuyunv2.internet;

/**这个类包含了要用到的API地址，主机地址，主机端口
 * @author Happinesslight 
 */
public class ApiAddress {
	static String HOST_IP = "http://218.200.234.130";
//	static String HOST_IP = "http://localhost";
	static String HOST_PORT = "7782";

	public static String getHOST_IP() {
		return HOST_IP;
	}

	public static void setHOST_IP(String hOST_IP) {
		HOST_IP = "http://" + hOST_IP;
	}

	public static String getHOST_PORT() {
		return HOST_PORT;
	}

	public static void setHOST_PORT(String hOST_PORT) {
		HOST_PORT = hOST_PORT;
	}

	public static String getLOGIN_API() {
		return getBaseURL() + "/Api/Login.action";
	}

	public static String getREISTER_API() {
		return getBaseURL() + "/Api/Register.action";
	}

	public static String getSEARCH_API() {
		return getBaseURL() + "/Api/Search.action";
	}

	public static String getPASSAGE_API() {
		return getBaseURL() + "/Api/GetPassage.action";
	}

	public static String getMAGAZINE_BY_PERIOD_API() {
		return getBaseURL() + "/Api/GetMagazine.action";
	}

	public static String getALL_MAGAZINE() {
		return getBaseURL() + "/Api/GetAllMagazine.action";
	}

	public static String getCOMPANY_API() {
		return getBaseURL() + "/Api/GetCompany.action";
	}

	public static String getACTIVITY_API() {
		return getBaseURL() + "/Api/GetActivity.action";
	}

	public static String getACTIVITIES_API() {
		return getBaseURL() + "/Api/GetActivities.action";
	}

	public static String getBaseURL() {
		return HOST_IP + ":" + HOST_PORT + "/Cloud";

	}

}
