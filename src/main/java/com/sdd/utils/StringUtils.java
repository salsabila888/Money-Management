package com.sdd.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static boolean emailValidator(String email) {
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();		
	}
	
	public static boolean isNumeric(String s) {  
	    return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	
	public static String numberHandler(String str) throws Exception {
		return str.replaceAll("[^\\d]", "");
	}

	public static String alphanumericHandler(String str) throws Exception {
		return str.replaceAll("[^a-zA-Z0-9]", "");
	}
	
	public static String titleHandler(String str) throws Exception {
		String name = "";
		name = str;
		if (name.toUpperCase().startsWith("H.")) {
			name = name.substring(2, name.length());
		}		
		if (name.toUpperCase().startsWith("HJ.")) {
			name = name.substring(3, name.length());
		}		
		if (name.contains(",")) {
			name = name.substring(0, name.indexOf(","));
		}
		return name.trim();
	}
	
	public static String decimalFormatter(String num) throws Exception {
		try {
			return NumberFormat.getInstance().format(new BigDecimal(num));
		} catch (Exception e) {			
			e.printStackTrace();
			return num;
		}
	}
	
	public static String dateFyyyyMMdd(String strDate) throws Exception {
		try {
			return (strDate.substring(0, 4) + "-" + strDate.substring(4, 6) + "-" + strDate
					.substring(6, 8));
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static String getMonthLabel(int month) {
		String label = "";
		
		switch (month) {
		case 1 :
			label = "January";
			break;
		case 2 :
			label = "Februari";
			break;
		case 3 :
			label = "March";
			break;
		case 4 :
			label = "April";
			break;
		case 5 :
			label = "May";
			break;
		case 6 :
			label = "June";
			break;
		case 7 :
			label = "July";
			break;
		case 8 :
			label = "August";
			break;
		case 9 :
			label = "September";
			break;
		case 10 :
			label = "October";
			break;
		case 11 :
			label = "November";
			break;
		case 12 :
			label = "December";
			break;
		}
		
		return label;
	}
	
	public static String getMonthshortLabel(int month) {
		String label = "";
		
		switch (month) {
		case 1 :
			label = "Jan";
			break;
		case 2 :
			label = "Feb";
			break;
		case 3 :
			label = "Mar";
			break;
		case 4 :
			label = "Apr";
			break;
		case 5 :
			label = "May";
			break;
		case 6 :
			label = "Jun";
			break;
		case 7 :
			label = "Jul";
			break;
		case 8 :
			label = "Aug";
			break;
		case 9 :
			label = "Sep";
			break;
		case 10 :
			label = "Oct";
			break;
		case 11 :
			label = "Nov";
			break;
		case 12 :
			label = "Dec";
			break;
		}
		
		return label;
	}
	
	public static String getDayslocal(int day) {
		String label = "";
		
		switch (day) {
		case 1 :
			label = "Minggu";
			break;
		case 2 :
			label = "Senin";
			break;
		case 3 :
			label = "Selasa";
			break;
		case 4 :
			label = "Rabu";
			break;
		case 5 :
			label = "Kamis";
			break;
		case 6 :
			label = "Jumat";
			break;
		case 7 :
			label = "Sabtu";
			break;
		}
		
		return label;
	}
	
	public static String angkaToTerbilang(Long angka) throws Exception {
		String bilangan = "";
		String[] huruf={"","Satu","Dua","Tiga","Empat","Lima","Enam","Tujuh","Delapan","Sembilan","Sepuluh","Sebelas"};
		try {
			if(angka < 12)
	            return huruf[angka.intValue()];
	        if(angka >=12 && angka <= 19)
	           return huruf[angka.intValue() % 10] + " Belas";
	        if(angka >= 20 && angka <= 99)
	           return angkaToTerbilang(angka / 10) + " Puluh " + huruf[angka.intValue() % 10];
	        if(angka >= 100 && angka <= 199)
	           return "Seratus " + angkaToTerbilang(angka % 100);
	        if(angka >= 200 && angka <= 999)
	           return angkaToTerbilang(angka / 100) + " Ratus " + angkaToTerbilang(angka % 100);
	        if(angka >= 1000 && angka <= 1999)
	           return "Seribu " + angkaToTerbilang(angka % 1000);
	        if(angka >= 2000 && angka <= 999999)
	           return angkaToTerbilang(angka / 1000) + " Ribu " + angkaToTerbilang(angka % 1000);
	        if(angka >= 1000000 && angka <= 999999999)
	           return angkaToTerbilang(angka / 1000000) + " Juta " + angkaToTerbilang(angka % 1000000);
	        if(angka >= 1000000000 && angka <= 999999999999L)
	           return angkaToTerbilang(angka / 1000000000) + " Milyar " + angkaToTerbilang(angka % 1000000000);
	        if(angka >= 1000000000000L && angka <= 999999999999999L)
	           return angkaToTerbilang(angka / 1000000000000L) + " Triliun " + angkaToTerbilang(angka % 1000000000000L);
	        if(angka >= 1000000000000000L && angka <= 999999999999999999L)
	          return angkaToTerbilang(angka / 1000000000000000L) + " Quadrilyun " + angkaToTerbilang(angka % 1000000000000000L);
	        return "";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bilangan;
	}
	
}
