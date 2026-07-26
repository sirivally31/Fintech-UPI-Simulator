package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * GlobalException Handler for catching and formatting exceptions thrown across the application.
 *
 * Why global exception handling is preferred:
 * 1. DRY Principle (Don't Repeat Yourself): It eliminates the need to clutter every individual 
 *    Controller method with repetitive try-catch blocks.
 * 2. Separation of Concerns: Controllers can focus exclusively on routing and returning the "Happy Path" 
 *    while this centralized class manages all HTTP failure mappings.
 *
 * Difference between 400, 403, 404, 409, and 500 status codes:
 * - 400 (Bad Request): The client provided invalid input (e.g., failed @Valid constraints).
 * - 403 (Forbidden): The client is authenticated but strictly lacks the permission to perform 
 *   the action (e.g., deleting someone else's account).
 * - 404 (Not Found): The requested resource does not exist in the database.
 * - 409 (Conflict): The client is trying to do something that breaks business rules or data integrity 
 *   (e.g., registering an account number that is already taken).
 * - 500 (Internal Server Error): An unhandled anomaly or bug crashed the application logic.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAccount(DuplicateAccountException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(UnauthorizedAccountAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedAccess(UnauthorizedAccountAccessException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMerchantNotFound(MerchantNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleMerchantAlreadyExists(MerchantAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(InvalidQrException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQr(InvalidQrException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentProcessing(PaymentProcessingException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BeneficiaryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBeneficiaryNotFound(BeneficiaryNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(BeneficiaryAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleBeneficiaryAlreadyExists(BeneficiaryAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(BeneficiaryValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBeneficiaryValidation(BeneficiaryValidationException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(AutoPayNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAutoPayNotFound(AutoPayNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(AutoPayValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleAutoPayValidation(AutoPayValidationException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(FraudRuleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFraudRuleNotFound(FraudRuleNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(FraudDetectedException.class)
    public ResponseEntity<ApiErrorResponse> handleFraudDetected(FraudDetectedException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFound(NotificationNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(SettlementBatchNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSettlementBatchNotFound(SettlementBatchNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(SettlementProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handleSettlementProcessing(SettlementProcessingException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // We avoid sending internal stack traces or exact exceptions to the client for security reasons
        return buildErrorResponse("An unexpected internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(apiErrorResponse, status);
    }
}
