package com.gestionacademica.android.presentation.reportes;

import com.gestionacademica.android.data.dto.ReportePageDto;
import com.gestionacademica.android.utils.ApiError;

public final class ReportesUiState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final ReportType reportType;
    private final Status status;
    private final ReportePageDto<?> page;
    private final ApiError error;

    private ReportesUiState(ReportType reportType, Status status,
            ReportePageDto<?> page, ApiError error) {
        this.reportType = reportType;
        this.status = status;
        this.page = page;
        this.error = error;
    }

    public static ReportesUiState idle() {
        return new ReportesUiState(null, Status.IDLE, null, null);
    }

    public static ReportesUiState loading(ReportType type) {
        return new ReportesUiState(type, Status.LOADING, null, null);
    }

    public static ReportesUiState success(ReportType type, ReportePageDto<?> page) {
        return new ReportesUiState(type, Status.SUCCESS, page, null);
    }

    public static ReportesUiState error(ReportType type, ApiError error) {
        return new ReportesUiState(type, Status.ERROR, null, error);
    }

    public ReportType getReportType() { return reportType; }
    public Status getStatus() { return status; }
    public ReportePageDto<?> getPage() { return page; }
    public ApiError getError() { return error; }
}