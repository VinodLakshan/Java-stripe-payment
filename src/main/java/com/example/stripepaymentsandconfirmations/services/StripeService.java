package com.example.stripepaymentsandconfirmations.services;

import com.example.stripepaymentsandconfirmations.dto.StripeDto;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface StripeService {

    Session createPaymentSession(StripeDto stripeDto) throws StripeException;

    Object webhook (HttpServletRequest request, HttpServletResponse response, String payload);
}
