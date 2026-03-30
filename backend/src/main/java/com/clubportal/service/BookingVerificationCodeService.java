package com.clubportal.service;

import com.clubportal.repository.BookingRecordRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class BookingVerificationCodeService {

    private static final int MAX_ATTEMPTS = 32;

    private final SecureRandom random = new SecureRandom();
    private final BookingRecordRepository bookingRecordRepository;

    public BookingVerificationCodeService(BookingRecordRepository bookingRecordRepository) {
        this.bookingRecordRepository = bookingRecordRepository;
    }

    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt += 1) {
            String candidate = String.format("%06d", random.nextInt(1_000_000));
            if (!bookingRecordRepository.existsByBookingVerificationCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique booking verification code");
    }
}
