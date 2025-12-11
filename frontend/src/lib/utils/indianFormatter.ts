/**
 * Utility functions for Indian number and currency formatting
 */

/**
 * Format number in Indian currency format (₹)
 * Example: 1234567.89 -> ₹12,34,567.89
 */
export const formatIndianCurrency = (amount: number | string): string => {
  const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
  
  if (isNaN(numAmount)) {
    return '₹0.00';
  }

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(numAmount);
};

/**
 * Format number in Indian numbering system (lakhs and crores)
 * Example: 1234567.89 -> 12,34,567.89
 */
export const formatIndianNumber = (num: number | string, decimals: number = 2): string => {
  const numValue = typeof num === 'string' ? parseFloat(num) : num;
  
  if (isNaN(numValue)) {
    return '0.' + '0'.repeat(decimals);
  }

  return new Intl.NumberFormat('en-IN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(numValue);
};

/**
 * Convert number to words in Indian format
 * Example: 123456 -> "One Lakh Twenty-Three Thousand Four Hundred Fifty-Six"
 */
export const convertToWords = (num: number): string => {
  if (num === 0) return 'Zero';
  if (num < 0) return 'Minus ' + convertToWords(-num);

  const ones = ['', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine'];
  const tens = ['', 'Ten', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];
  const teens = ['Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen'];

  let words = '';
  let number = Math.floor(num);

  // Crores
  if (number >= 10000000) {
    words += convertToWords(Math.floor(number / 10000000)) + ' Crore ';
    number %= 10000000;
  }

  // Lakhs
  if (number >= 100000) {
    words += convertToWords(Math.floor(number / 100000)) + ' Lakh ';
    number %= 100000;
  }

  // Thousands
  if (number >= 1000) {
    words += convertToWords(Math.floor(number / 1000)) + ' Thousand ';
    number %= 1000;
  }

  // Hundreds
  if (number >= 100) {
    words += ones[Math.floor(number / 100)] + ' Hundred ';
    number %= 100;
  }

  // Tens and ones
  if (number >= 20) {
    words += tens[Math.floor(number / 10)] + ' ';
    number %= 10;
  } else if (number >= 10) {
    words += teens[number - 10] + ' ';
    number = 0;
  }

  if (number > 0) {
    words += ones[number] + ' ';
  }

  return words.trim();
};

/**
 * Convert currency amount to words
 * Example: 1234.56 -> "One Thousand Two Hundred Thirty-Four Rupees and Fifty-Six Paise"
 */
export const convertCurrencyToWords = (amount: number): string => {
  if (amount === 0) return 'Zero Rupees';

  const rupees = Math.floor(amount);
  const paise = Math.round((amount - rupees) * 100);

  let words = convertToWords(rupees) + ' Rupees';

  if (paise > 0) {
    words += ' and ' + convertToWords(paise) + ' Paise';
  }

  return words;
};

/**
 * Get current Indian financial year
 * Financial year: April 1 to March 31
 * Example: April 2024 to March 2025 = "2024-25"
 */
export const getCurrentFinancialYear = (): string => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1; // JavaScript months are 0-based

  if (month >= 4) {
    // April onwards - current year to next year
    return `${year}-${String((year + 1) % 100).padStart(2, '0')}`;
  } else {
    // January to March - previous year to current year
    return `${year - 1}-${String(year % 100).padStart(2, '0')}`;
  }
};

/**
 * Validate GSTIN format
 * Format: 22AAAAA0000A1Z5
 * First 2: State code, Next 10: PAN, 13th: Entity number, 14th: Z, 15th: Checksum
 */
export const validateGSTIN = (gstin: string): boolean => {
  if (!gstin || gstin.length !== 15) {
    return false;
  }

  const regex = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;
  return regex.test(gstin);
};

/**
 * Extract state code from GSTIN
 */
export const getStateCodeFromGSTIN = (gstin: string): string | null => {
  if (!gstin || gstin.length < 2) {
    return null;
  }
  return gstin.substring(0, 2);
};

/**
 * Extract PAN from GSTIN
 */
export const getPANFromGSTIN = (gstin: string): string | null => {
  if (!gstin || gstin.length < 12) {
    return null;
  }
  return gstin.substring(2, 12);
};

/**
 * Indian state codes for GST
 */
export const INDIAN_STATE_CODES = {
  '01': 'Jammu and Kashmir',
  '02': 'Himachal Pradesh',
  '03': 'Punjab',
  '04': 'Chandigarh',
  '05': 'Uttarakhand',
  '06': 'Haryana',
  '07': 'Delhi',
  '08': 'Rajasthan',
  '09': 'Uttar Pradesh',
  '10': 'Bihar',
  '11': 'Sikkim',
  '12': 'Arunachal Pradesh',
  '13': 'Nagaland',
  '14': 'Manipur',
  '15': 'Mizoram',
  '16': 'Tripura',
  '17': 'Meghalaya',
  '18': 'Assam',
  '19': 'West Bengal',
  '20': 'Jharkhand',
  '21': 'Odisha',
  '22': 'Chhattisgarh',
  '23': 'Madhya Pradesh',
  '24': 'Gujarat',
  '26': 'Dadra and Nagar Haveli and Daman and Diu',
  '27': 'Maharashtra',
  '28': 'Andhra Pradesh (old)',
  '29': 'Karnataka',
  '30': 'Goa',
  '31': 'Lakshadweep',
  '32': 'Kerala',
  '33': 'Tamil Nadu',
  '34': 'Puducherry',
  '35': 'Andaman and Nicobar Islands',
  '36': 'Telangana',
  '37': 'Andhra Pradesh',
  '38': 'Ladakh',
} as const;

/**
 * Get state name from code
 */
export const getStateName = (code: string): string => {
  return INDIAN_STATE_CODES[code as keyof typeof INDIAN_STATE_CODES] || 'Unknown';
};
