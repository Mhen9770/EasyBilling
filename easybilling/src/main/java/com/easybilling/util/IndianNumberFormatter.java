package com.easybilling.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for Indian number and currency formatting.
 */
public class IndianNumberFormatter {
    
    private static final Locale INDIA_LOCALE = new Locale("en", "IN");
    
    /**
     * Format currency in Indian format with ₹ symbol.
     * Example: ₹12,34,567.89
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "₹0.00";
        }
        NumberFormat indianFormat = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        return indianFormat.format(amount);
    }
    
    /**
     * Format number in Indian numbering system (lakhs and crores).
     * Example: 12,34,567.89
     */
    public static String formatNumber(BigDecimal number) {
        if (number == null) {
            return "0.00";
        }
        DecimalFormat indianFormatter = new DecimalFormat("##,##,###.##", 
            new DecimalFormatSymbols(INDIA_LOCALE));
        return indianFormatter.format(number);
    }
    
    /**
     * Format number in Indian format with specific decimal places.
     */
    public static String formatNumber(BigDecimal number, int decimalPlaces) {
        if (number == null) {
            return "0." + "0".repeat(decimalPlaces);
        }
        String pattern = "##,##,###." + "#".repeat(decimalPlaces);
        DecimalFormat indianFormatter = new DecimalFormat(pattern, 
            new DecimalFormatSymbols(INDIA_LOCALE));
        return indianFormatter.format(number);
    }
    
    /**
     * Convert number to words in Indian format.
     * Example: 123456 -> "One Lakh Twenty-Three Thousand Four Hundred Fifty-Six"
     */
    public static String convertToWords(long number) {
        if (number == 0) {
            return "Zero";
        }
        
        if (number < 0) {
            return "Minus " + convertToWords(-number);
        }
        
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        String[] tens = {"", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        String[] teens = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        
        StringBuilder words = new StringBuilder();
        
        // Crores
        if (number >= 10000000) {
            words.append(convertToWords(number / 10000000)).append(" Crore ");
            number %= 10000000;
        }
        
        // Lakhs
        if (number >= 100000) {
            words.append(convertToWords(number / 100000)).append(" Lakh ");
            number %= 100000;
        }
        
        // Thousands
        if (number >= 1000) {
            words.append(convertToWords(number / 1000)).append(" Thousand ");
            number %= 1000;
        }
        
        // Hundreds
        if (number >= 100) {
            words.append(ones[(int)(number / 100)]).append(" Hundred ");
            number %= 100;
        }
        
        // Tens and ones
        if (number >= 20) {
            words.append(tens[(int)(number / 10)]).append(" ");
            number %= 10;
        } else if (number >= 10) {
            words.append(teens[(int)(number - 10)]).append(" ");
            number = 0;
        }
        
        if (number > 0) {
            words.append(ones[(int)number]).append(" ");
        }
        
        return words.toString().trim();
    }
    
    /**
     * Convert currency amount to words.
     * Example: 1234.56 -> "One Thousand Two Hundred Thirty-Four Rupees and Fifty-Six Paise"
     */
    public static String convertCurrencyToWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Zero Rupees";
        }
        
        long rupees = amount.longValue();
        long paise = amount.subtract(new BigDecimal(rupees))
                .multiply(BigDecimal.valueOf(100))
                .longValue();
        
        StringBuilder words = new StringBuilder();
        words.append(convertToWords(rupees)).append(" Rupees");
        
        if (paise > 0) {
            words.append(" and ").append(convertToWords(paise)).append(" Paise");
        }
        
        return words.toString();
    }
}
