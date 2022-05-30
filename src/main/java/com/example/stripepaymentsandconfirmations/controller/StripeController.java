package com.example.stripepaymentsandconfirmations.controller;

import com.example.stripepaymentsandconfirmations.dto.StripeDto;
import com.example.stripepaymentsandconfirmations.services.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/stripe")
@Slf4j
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @GetMapping("/createPaymentSession")
    public ResponseEntity createPaymentSession() {

        StripeDto stripeDto = new StripeDto();
        stripeDto.setCurrency("USD");
        stripeDto.setAmount(1500D);
        stripeDto.setSuccessUrl("http://localhost:3000/paymentSuccess");
        stripeDto.setCancelUrl("http://localhost:3000/paymentCancel");

        try {
            Session paymentSession = stripeService.createPaymentSession(stripeDto);
            return ResponseEntity.ok(paymentSession.getUrl());

        } catch (StripeException e) {
            log.error(e.getMessage());
            return  new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/webhook")
    public Object webhook(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload){

        log.info("executing webhook...");
        return stripeService.webhook(request, response, payload);
    }
}
