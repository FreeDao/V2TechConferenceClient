package com.V2.jni;

public class ConfigRequest
{
	
	//���������ļ�������
	public native void setConfigProp(String szItemPath, String szConfigAttr, String szValue);
	//��ȡ�����ļ�������
	public native void getConfigProp(String szItemPath,String szConfigAttr, byte[] pValueBuf, int nBufLen);
	//��ȡ�����ļ�����������
	public native void getConfigPropCount(String szItemPath);
	//ɾ�������ļ���ĳ������
	public native void removeConfigProp(String szItemPath,String szConfigAttr);
	//���÷�������ַ
	public native void setServerAddress(String szServerIP, int nPort);
	//���ô洢����ַ
	public native void setExtStoragePath(String szPath);
}
