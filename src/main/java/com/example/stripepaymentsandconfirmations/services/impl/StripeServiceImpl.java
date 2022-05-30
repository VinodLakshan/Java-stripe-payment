package com.example.stripepaymentsandconfirmations.services.impl;

import com.example.stripepaymentsandconfirmations.dto.StripeDto;
import com.example.stripepaymentsandconfirmations.services.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.apiKey}")
    private String apiKey;

    @Value("${stripe.endpoint-secret}")
    private String endpointSecret;

    @Override
    public Session createPaymentSession(StripeDto stripeDto) throws StripeException {

        Stripe.apiKey = apiKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)

                // You can store a reference using this to retrieve later in webhook. eg: an ID
                //.setClientReferenceId("")

                .setSuccessUrl(stripeDto.getSuccessUrl())
                .setCancelUrl(stripeDto.getCancelUrl())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(stripeDto.getCurrency())
                                                .setUnitAmount((long) (stripeDto.getAmount() * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Total Amount")
                                                                .build())
                                                .build())
                                .build())
                .build();

        return Session.create(params);

    }

    @Override
    public Object webhook(HttpServletRequest request, HttpServletResponse response, String payload) {

        Stripe.apiKey = apiKey;
        String sigHeader = request.getHeader("Stripe-Signature");
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

        } catch (SignatureVerificationException e) {
            response.setStatus(400);
            return "";
        }

        if ("checkout.session.completed".equals(event.getType())) {

            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

            if (dataObjectDeserializer.getObject().isPresent()) {

                // deserialize session if you want to access session that you created before
                Session session = (Session) dataObjectDeserializer.getObject().get();

                // Store the order in DB, send customer conformation email, etc
                // code here

                // don't forget to divide the amount by 100
                log.info("Payment succeeded : " + session.getCustomerDetails().getName() + " : " + session.getAmountTotal() / 100);
            }

        }

        response.setStatus(200);
        return "";
    }
}
