package com.silambarasan.smartstack.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceHelper {

	Context context;
	SharedPreferences sharedPreferences;

	public PreferenceHelper(Context context) {
		this.context = context;
		sharedPreferences = context.getSharedPreferences("smart_stack",
				Context.MODE_MULTI_PROCESS);
	}

	public SharedPreferences getGcmPreferences() {

		return sharedPreferences;
	}

	public String get(String KeyName) {
		return sharedPreferences.getString(KeyName, "");
	}

	

	public boolean set(String KeyName, String value) {
		Editor editor = sharedPreferences.edit();
		editor.putString(KeyName, value);
		editor.commit();
		return true;
	}

	public int GetIntFromSharedPrefs(String KeyName) {
		return sharedPreferences.getInt(KeyName, 1);
	}

	public boolean GetBoolFromSharedPrefs(String KeyName) {
		return sharedPreferences.getBoolean(KeyName, false);
	}

	public void savePreferenceIndex(String keyName, int index) {
		Editor editor = sharedPreferences.edit();
		editor.putInt(keyName, index);
		editor.commit();
	}

	public int getPreferenceIndex(String keyName) {
		return sharedPreferences.getInt(keyName, 0);
	}
	
	public void SaveBooleanValueToSharedPrefs(String KeyName,boolean value){
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(KeyName, value);
		editor.commit();
	}

	public void RemoveValue(String keyName){
		Editor editor = sharedPreferences.edit();
		editor.remove(keyName);
		editor.apply();
	}

	public void clear()
	{
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}
}
