package com.kirosh.stripetestapp.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripeService {

    @Value("${STRIPE_API_KEY}")
    private String key;
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public Customer createCustomer(String email) throws StripeException {
        Stripe.apiKey = key;

        CustomerSearchParams params =
                CustomerSearchParams
                        .builder()
                        .setQuery("email:'" + email + "'")
                        .build();

        CustomerSearchResult result = Customer.search(params);

        Customer customer;
        if (result.getData().isEmpty()) {
            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setEmail(email)
                    .build();
            customer = Customer.create(customerCreateParams);
        } else {
            customer = result.getData().get(0);
        }
        return customer;
    }


    public Map<String, String> createSubscription(String customerId, String priceId) throws StripeException {
        Stripe.apiKey = key;

        SubscriptionCreateParams subCreateParams = SubscriptionCreateParams
                .builder()
                .setCustomer(customerId)
                .addItem(
                        SubscriptionCreateParams.Item.builder()
                                .setPrice(priceId)
                                .build()
                )
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addAllExpand(Arrays.asList("latest_invoice.payment_intent"))
                .build();

        Subscription subscription = Subscription.create(subCreateParams);
        PaymentIntent paymentIntent = (PaymentIntent) subscription.getLatestInvoiceObject().getPaymentIntentObject();
        String clientSecret = paymentIntent.getClientSecret();

        Map<String, String> response = new HashMap<>();
        response.put("subscriptionId", subscription.getId());
        response.put("clientSecret", clientSecret);

        return response;
    }

    public Subscription updateSubscription(String subscriptionId, String newPriceId) throws StripeException {
        Stripe.apiKey = key;

        // Retrieve the subscription to access related subscription item ID
        Subscription subscription = Subscription.retrieve(subscriptionId);

        // Build params to update the Subscription
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .addItem(SubscriptionUpdateParams.Item.builder()
                        .setId(subscription.getItems().getData().get(0).getId()) // assumes only 1 subscription item
                        .setPrice(newPriceId)
                        .build())
                .setCancelAtPeriodEnd(false)
                .build();

        // Update the subscription
        return subscription.update(params);
    }

    public Map<String, String> cancelSubscription(String subscriptionId) throws StripeException {
        Stripe.apiKey = key;

        SubscriptionCancelParams params = SubscriptionCancelParams.builder().build();
        Subscription subscription = Subscription.retrieve(subscriptionId).cancel(params);
        Map<String, String> response = new HashMap<>();
        response.put("subscriptionId", subscription.getId());
        response.put("status", subscription.getStatus());
        return response;
    }

    public List<Subscription> listSubscriptions(String customerId) throws StripeException {
        Stripe.apiKey = key;

        SubscriptionListParams params = SubscriptionListParams.builder()
                .setCustomer(customerId)
                .build();

        return Subscription.list(params).getData();
    }

    public Map<String, Object> previewInvoice(String customerId, String newPriceId, String subscriptionId) throws StripeException {
        Stripe.apiKey = key;

        // Retrieve the subscription to get the ID of the subscription item
        Subscription subscription = Subscription.retrieve(subscriptionId);

        // Build the params for retrieving the invoice preview
        InvoiceUpcomingParams invoiceParams = InvoiceUpcomingParams.builder()
                .setCustomer(customerId)
                .setSubscription(subscriptionId)
                .addSubscriptionItem(
                        InvoiceUpcomingParams.SubscriptionItem.builder()
                                .setId(subscription.getItems().getData().get(0).getId())
                                .setPrice(newPriceId)
                                .build()
                )
                .build();

        // Fetch the invoice preview
        Invoice invoice = Invoice.upcoming(invoiceParams);

        // Prepare the response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("invoice", invoice);

        return responseData;
    }

    public void handleInvoicePaymentSucceeded(Event event) throws StripeException {
        Stripe.apiKey = key;

        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (invoice != null && "subscription_create".equals(invoice.getBillingReason())) {
            String subscriptionId = invoice.getSubscription();
            String paymentIntentId = invoice.getPaymentIntent();

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setDefaultPaymentMethod(paymentIntent.getPaymentMethod())
                    .build();
            subscription.update(params);
        }
    }
}
