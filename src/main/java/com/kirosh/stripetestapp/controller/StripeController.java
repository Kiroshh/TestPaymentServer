package com.kirosh.stripetestapp.controller;

import com.kirosh.stripetestapp.dto.CancelSubscriptionRequest;
import com.kirosh.stripetestapp.dto.CreateCustomerRequest;
import com.kirosh.stripetestapp.dto.CreateSubscriptionRequest;
import com.kirosh.stripetestapp.dto.UpdateSubscriptionRequest;
import com.kirosh.stripetestapp.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class StripeController {
    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-customer")
    public ResponseEntity<?> createCustomer(@RequestBody CreateCustomerRequest request) {
        try {
            Customer customer = stripeService.createCustomer(request.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("id", customer.getId());
            response.put("email", customer.getEmail());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/create-subscription")
    public ResponseEntity<?> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        try {
            Map<String, String> subscriptionDetails = stripeService.createSubscription(request.getCustomerId(), request.getPriceId());
            return ResponseEntity.ok(subscriptionDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/update-subscription")
    public ResponseEntity<?> updateSubscription(@RequestBody UpdateSubscriptionRequest request) {
        try {
            Subscription subscription = stripeService.updateSubscription(request.getSubscriptionId(), request.getNewPriceId());
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/cancel-subscription")
    public ResponseEntity<?> cancelSubscription(@RequestBody CancelSubscriptionRequest request) {
        try {
            Map<String, String> subscriptionDetails = stripeService.cancelSubscription(request.getSubscriptionId());
            return ResponseEntity.ok(subscriptionDetails);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/list-subscriptions/{customerId}")
    public ResponseEntity<?> listSubscriptions(@PathVariable String customerId) {
        try {
            List<Subscription> subscriptions = stripeService.listSubscriptions(customerId);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/invoice-preview")
    public ResponseEntity<?> previewInvoice(@RequestParam String customerId, @RequestParam String newPriceId, @RequestParam String subscriptionId) {
        try {
            Map<String, Object> invoicePreview = stripeService.previewInvoice(customerId, newPriceId, subscriptionId);
            return ResponseEntity.ok(invoicePreview);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            System.out.println("Received webhook payload: " + payload);
            Event event = Webhook.constructEvent(payload, sigHeader, stripeService.getWebhookSecret());

            if ("invoice.payment_succeeded".equals(event.getType())) {
                stripeService.handleInvoicePaymentSucceeded(event);
            }

            // Handle other event types as needed

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}