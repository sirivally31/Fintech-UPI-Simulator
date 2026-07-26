package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response container holding a list of beneficiaries")
public class BeneficiaryListResponse {

    @Schema(description = "List of beneficiary profiles")
    private List<BeneficiaryResponse> beneficiaries;

    @Schema(description = "Total number of beneficiaries in the result set", example = "5")
    private int total;

    public BeneficiaryListResponse() {
    }

    public BeneficiaryListResponse(List<BeneficiaryResponse> beneficiaries, int total) {
        this.beneficiaries = beneficiaries;
        this.total = total;
    }

    public List<BeneficiaryResponse> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<BeneficiaryResponse> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
