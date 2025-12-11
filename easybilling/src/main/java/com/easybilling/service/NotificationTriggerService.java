package com.easybilling.service;

import com.easybilling.dto.NotificationRequest;
import com.easybilling.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for triggering automatic notifications based on business events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTriggerService {
    
    private final NotificationService notificationService;
    
    /**
     * Send invoice notification to customer
     */
    public void sendInvoiceNotification(Integer tenantId, String customerEmail, String customerPhone,
                                       String invoiceNumber, BigDecimal amount) {
        log.info("Triggering invoice notification for invoice: {}", invoiceNumber);
        
        // Send SMS
        NotificationRequest smsRequest = new NotificationRequest();
        smsRequest.setType(NotificationType.SMS);
        smsRequest.setRecipient(customerPhone);
        smsRequest.setSubject("Invoice Generated");
        smsRequest.setMessage(String.format("Invoice %s generated. Amount: %s. Thank you for your purchase!",
                invoiceNumber, amount));
        
        notificationService.sendNotification(smsRequest, tenantId);
        
        // Send Email if available
        if (customerEmail != null && !customerEmail.isBlank()) {
            NotificationRequest emailRequest = new NotificationRequest();
            emailRequest.setType(NotificationType.EMAIL);
            emailRequest.setRecipient(customerEmail);
            emailRequest.setSubject("Invoice #" + invoiceNumber);
            emailRequest.setMessage(String.format(
                    "Dear Customer,\n\nYour invoice %s has been generated.\nAmount: %s\n\nThank you for your business!",
                    invoiceNumber, amount));
            
            notificationService.sendNotification(emailRequest, tenantId);
        }
    }
    
    /**
     * Send low stock alert
     */
    public void sendLowStockAlert(Integer tenantId, String adminEmail, String productName,
                                 int currentStock, int threshold) {
        log.info("Triggering low stock alert for product: {}", productName);
        
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.EMAIL);
        request.setRecipient(adminEmail);
        request.setSubject("Low Stock Alert - " + productName);
        request.setMessage(String.format(
                "ALERT: Product '%s' is running low.\nCurrent Stock: %d\nThreshold: %d\n\nPlease reorder soon.",
                productName, currentStock, threshold));
        
        notificationService.sendNotification(request, tenantId);
    }
    
    /**
     * Send payment reminder to customer
     */
    public void sendPaymentReminder(Integer tenantId, String customerEmail, String customerPhone,
                                   BigDecimal outstandingAmount, int daysDue) {
        log.info("Triggering payment reminder. Outstanding amount: {}", outstandingAmount);
        
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.SMS);
        request.setRecipient(customerPhone);
        request.setSubject("Payment Reminder");
        request.setMessage(String.format(
                "Payment reminder: You have an outstanding balance of %s. Due since %d days. Please pay at your earliest convenience.",
                outstandingAmount, daysDue));
        
        notificationService.sendNotification(request, tenantId);
    }
    
    /**
     * Send welcome notification to new customer
     */
    public void sendWelcomeNotification(Integer tenantId, String customerEmail, String customerPhone,
                                       String customerName) {
        log.info("Sending welcome notification to: {}", customerName);
        
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.SMS);
        request.setRecipient(customerPhone);
        request.setSubject("Welcome!");
        request.setMessage(String.format(
                "Welcome %s! Thank you for choosing us. Enjoy exclusive offers and loyalty rewards on your purchases!",
                customerName));
        
        notificationService.sendNotification(request, tenantId);
    }
    
    /**
     * Send loyalty points earned notification
     */
    public void sendLoyaltyPointsNotification(Integer tenantId, String customerPhone,
                                              int pointsEarned, int totalPoints) {
        log.info("Sending loyalty points notification. Points earned: {}", pointsEarned);
        
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.SMS);
        request.setRecipient(customerPhone);
        request.setSubject("Loyalty Points Earned");
        request.setMessage(String.format(
                "You earned %d loyalty points! Total points: %d. Redeem for rewards on your next purchase!",
                pointsEarned, totalPoints));
        
        notificationService.sendNotification(request, tenantId);
    }
    
    /**
     * Send offer activation notification
     */
    public void sendOfferNotification(Integer tenantId, String recipientEmail, String recipientPhone,
                                     String offerName, String offerDescription) {
        log.info("Sending offer notification: {}", offerName);
        
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.SMS);
        request.setRecipient(recipientPhone);
        request.setSubject("Special Offer - " + offerName);
        request.setMessage(String.format(
                "Exclusive offer for you: %s - %s. Visit us soon to avail!",
                offerName, offerDescription));
        
        notificationService.sendNotification(request, tenantId);
    }
    
    /**
     * Send payment confirmation
     */
    public void sendPaymentConfirmation(Integer tenantId, String customerEmail, String customerPhone,
                                       BigDecimal amount, String paymentMethod) {
        log.info("Sending payment confirmation. Amount: {}", amount);
        
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.SMS);
        request.setRecipient(customerPhone);
        request.setSubject("Payment Received");
        request.setMessage(String.format(
                "Payment of %s received via %s. Thank you!",
                amount, paymentMethod));
        
        notificationService.sendNotification(request, tenantId);
    }
}
