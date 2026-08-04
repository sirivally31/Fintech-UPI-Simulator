package com.example.demo.producer;

import com.example.demo.events.*;

/**
 * EventPublisher contract defining domain event publishing methods for Kafka messaging.
 * Implementation will be created in a subsequent phase.
 */
public interface EventPublisher {

    void publishTransactionCompleted(TransactionCompletedEvent event);

    void publishPaymentRequestCreated(PaymentRequestCreatedEvent event);

    void publishPaymentRequestAccepted(PaymentRequestAcceptedEvent event);

    void publishPaymentRequestRejected(PaymentRequestRejectedEvent event);

    void publishPaymentRequestCancelled(PaymentRequestCancelledEvent event);

    void publishQrCreated(QrCreatedEvent event);

    void publishQrPaymentSuccess(QrPaymentSuccessEvent event);

    void publishBeneficiaryAdded(BeneficiaryAddedEvent event);

    void publishBeneficiaryUpdated(BeneficiaryUpdatedEvent event);

    void publishBeneficiaryDeleted(BeneficiaryDeletedEvent event);

    void publishAutoPayCreated(AutoPayCreatedEvent event);

    void publishAutoPayExecuted(AutoPayExecutedEvent event);

    void publishAutoPayFailed(AutoPayFailedEvent event);

    void publishAutoPayCancelled(AutoPayCancelledEvent event);

    void publishFraudDetected(FraudDetectedEvent event);

    void publishFraudBlocked(FraudBlockedEvent event);

    void publishHighRiskTransaction(HighRiskTransactionEvent event);

    void publishSettlementCompleted(SettlementCompletedEvent event);

    void publishSettlementFailed(SettlementFailedEvent event);

    void publishSettlementReconciled(SettlementReconciledEvent event);

    void publishSettlementReversed(SettlementReversedEvent event);

    void publishAuditCreated(AuditCreatedEvent event);

    void publishAdminAction(AdminActionEvent event);

    void publishRoleAssigned(RoleAssignedEvent event);

    void publishSystemConfigUpdated(SystemConfigUpdatedEvent event);

    void publishMerchantApproved(MerchantApprovedEvent event);

    void publishMerchantBlocked(MerchantBlockedEvent event);

    // Module 19: Reports
    void publishReportGenerated(com.example.demo.events.ReportGeneratedEvent event);
    void publishReportDownloaded(com.example.demo.events.ReportDownloadedEvent event);
    void publishScheduledReportGenerated(com.example.demo.events.ScheduledReportGeneratedEvent event);
}
